#!/usr/bin/env python3
"""
adb-bridge.py — TCP proxy to bypass corporate network monitors (e.g. SentinelOne).

Problem:
  Corporate endpoint security blocks Homebrew's `adb` from making outbound TCP
  connections to Android devices on the local network.  System binaries
  (/usr/bin/python3, /usr/bin/nc, etc.) are Apple-signed and bypass these rules.

Solution:
  This proxy listens on localhost and forwards all traffic to the Android device.
  When run via the system Python (/usr/bin/python3), outbound traffic is
  permitted by macOS.

Usage:
  # Start the bridge (auto-discovers the device port via mDNS)
  /usr/bin/python3 adb-bridge.py start

  # Run in background (detached)
  /usr/bin/python3 adb-bridge.py start --daemon

  # Check status
  /usr/bin/python3 adb-bridge.py status

  # Stop a running daemon
  /usr/bin/python3 adb-bridge.py stop

  # Discover current connect port
  /usr/bin/python3 adb-bridge.py discover

  # Pair + connect in one step
  /usr/bin/python3 adb-bridge.py pair --pair-port 37851

  # Connect through the bridge
  adb connect 127.0.0.1:5038
  adb devices -l

Workflow:
  1. On Android TV: Settings -> Developer options -> Wireless debugging
  2. Tap "Pair device with pairing code" -> note the pairing port + 6-digit code
  3. Pair (auto-discovers connect port after pairing):
       /usr/bin/python3 adb-bridge.py pair --pair-port <PAIRING_PORT>
  4. Connect through the bridge:
       adb connect 127.0.0.1:5038

  Next time (after reboot / port change):
     /usr/bin/python3 adb-bridge.py start --daemon   # auto-discovers port
     adb connect 127.0.0.1:5038

IMPORTANT:
  - MUST be run with /usr/bin/python3 (system Python, Apple-signed).
    Homebrew Python will be blocked by the same network monitor.
  - The connect port on Android 11+ changes when Wireless debugging is toggled.
    Check the device screen for the current port.
"""

from __future__ import annotations

import argparse
import os
import signal
import socket
import sys
import threading
import time
from pathlib import Path

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

DEFAULT_DEVICE = "192.168.1.152"
DEFAULT_LISTEN_PORT = 5038
DEFAULT_DEVICE_PORT = 45991
PID_DIR = Path.home() / ".local" / "run"
PID_FILE = PID_DIR / "adb-bridge.pid"
CONNECT_TIMEOUT = 10
BACKLOG = 5
RECV_BUF = 65536

# ---------------------------------------------------------------------------
# Logging (simple, thread-safe via print with flush)
# ---------------------------------------------------------------------------

_log_lock = threading.Lock()

def log(msg: str) -> None:
    with _log_lock:
        ts = time.strftime("%H:%M:%S")
        print(f"[{ts}] {msg}", flush=True)

# ---------------------------------------------------------------------------
# PID file management
# ---------------------------------------------------------------------------

def discover_adb_port(device_ip=DEFAULT_DEVICE, timeout=10):
    # type: (str, float) -> int | None
    """Auto-discover the ADB connect port via mDNS/Bonjour.

    Android 11+ advertises _adb-tls-connect._tcp.local. with the current
    connect port.  This changes every time Wireless debugging is toggled.
    """
    import subprocess as _sub
    import tempfile
    try:
        tmp1 = tempfile.mktemp(suffix=".dnsd")
        tmp2 = tempfile.mktemp(suffix=".dnsd")

        # Browse (dns-sd needs shell redirect for stderr on macOS)
        proc = _sub.Popen(
            f"dns-sd -B _adb-tls-connect._tcp local. >{tmp1} 2>&1",
            shell=True,
        )
        time.sleep(3)
        proc.terminate()
        proc.wait()

        with open(tmp1) as f:
            stderr = f.read()
        os.unlink(tmp1)

        instance = None
        for line in stderr.splitlines():
            if "Add" in line and "_adb-tls-connect._tcp" in line:
                instance = line.strip().split()[-1]
                break

        if not instance:
            return None

        # Resolve instance to get port
        proc2 = _sub.Popen(
            f"dns-sd -L '{instance}' _adb-tls-connect._tcp local. >{tmp2} 2>&1",
            shell=True,
        )
        time.sleep(3)
        proc2.terminate()
        proc2.wait()

        with open(tmp2) as f:
            stderr2 = f.read()
        os.unlink(tmp2)

        for line in stderr2.splitlines():
            if ".local.:" in line:
                port_str = line.split(":")[-1].split()[0]
                return int(port_str)
    except Exception:
        pass
    return None


def _find_bridge_pid():
    # type: () -> int | None
    """Find bridge server PID via process table, skipping launcher processes.

    The daemonized server does NOT have --daemon in its argv (the launcher
    does), so we prefer processes without --daemon.
    """
    import subprocess
    try:
        result = subprocess.run(
            ["ps", "-xo", "pid=,command="],
            capture_output=True, text=True, timeout=5,
        )
        script = os.path.basename(__file__)
        my_pid = os.getpid()
        no_daemon = []
        with_daemon = []
        for line in result.stdout.splitlines():
            line = line.strip()
            if script not in line:
                continue
            parts = line.split(None, 1)
            if len(parts) < 2:
                continue
            pid = int(parts[0])
            cmd = parts[1]
            if pid == my_pid:
                continue
            if "--daemon" in cmd:
                with_daemon.append(pid)
            else:
                no_daemon.append(pid)
        # Prefer actual server (no --daemon) over launcher
        target = no_daemon if no_daemon else with_daemon
        return min(target) if target else None
    except Exception:
        return None
    return None

def write_pid():
    """Write our PID to file (best effort, not used for status checks)."""
    try:
        PID_DIR.mkdir(parents=True, exist_ok=True)
        PID_FILE.write_text(str(os.getpid()))
    except OSError:
        pass

def read_pid():
    """Read PID from file (may be stale; prefer _find_bridge_pid)."""
    if not PID_FILE.exists():
        return None
    try:
        return int(PID_FILE.read_text().strip())
    except (ValueError, OSError):
        return None

def is_running(pid):
    if pid is None:
        return False
    try:
        os.kill(pid, 0)
        return True
    except OSError:
        return False

def remove_pid():
    try:
        PID_FILE.unlink()
    except OSError:
        pass

# ---------------------------------------------------------------------------
# Connection bridging
# ---------------------------------------------------------------------------

def bridge(local_sock: socket.socket, remote_sock: socket.socket, name: str) -> None:
    """Forward all data from `local_sock` to `remote_sock` (one direction)."""
    total = 0
    try:
        while True:
            data = local_sock.recv(RECV_BUF)
            if not data:
                log(f"{name}: EOF after {total:,} bytes")
                break
            total += len(data)
            remote_sock.sendall(data)
    except ConnectionResetError:
        log(f"{name}: connection reset after {total:,} bytes")
    except OSError as e:
        log(f"{name}: OSError({e.errno}) after {total:,} bytes")
    finally:
        # Signal shutdown to the other end
        try:
            remote_sock.shutdown(socket.SHUT_WR)
        except OSError:
            pass

def handle_client(local_sock: socket.socket, device_addr: tuple[str, int]) -> None:
    """Accept a local connection, open remote, then bridge both directions."""
    peer = local_sock.getpeername()
    log(f"Accepted connection from {peer[0]}:{peer[1]}")

    try:
        remote_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        remote_sock.settimeout(CONNECT_TIMEOUT)
        remote_sock.connect(device_addr)
        remote_sock.settimeout(None)  # blocking after connect
    except OSError as e:
        log(f"Failed to connect to {device_addr[0]}:{device_addr[1]}: {e}")
        local_sock.close()
        return

    log(f"Connected to device at {device_addr[0]}:{device_addr[1]}")

    # Bidirectional bridge (two threads, one per direction)
    t_up = threading.Thread(target=bridge, args=(local_sock, remote_sock, "client→device"), daemon=True)
    t_dn = threading.Thread(target=bridge, args=(remote_sock, local_sock, "device→client"), daemon=True)
    t_up.start()
    t_dn.start()

    # Keep handle alive until both directions close
    t_up.join()
    t_dn.join()

    # Clean up
    for s in (local_sock, remote_sock):
        try:
            s.close()
        except OSError:
            pass

# ---------------------------------------------------------------------------
# Server loop
# ---------------------------------------------------------------------------

def run_server(listen_host: str, listen_port: int, device_host: str, device_port: int) -> None:
    """Start the proxy server and accept connections."""

    # Handle shutdown signals gracefully
    stop_event = threading.Event()

    def on_signal(signum, frame):
        log(f"Received signal {signum}, shutting down…")
        stop_event.set()

    signal.signal(signal.SIGINT, on_signal)
    signal.signal(signal.SIGTERM, on_signal)

    # Create listening socket
    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
    srv.bind((listen_host, listen_port))
    srv.settimeout(1.0)  # allow periodic stop_event check
    srv.listen(BACKLOG)

    log(f"Listening on {listen_host}:{listen_port}")
    log(f"Forwarding to {device_host}:{device_port}")
    log("Press Ctrl-C to stop")

    try:
        while not stop_event.is_set():
            try:
                local_sock, _ = srv.accept()
                local_sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
            except socket.timeout:
                continue
            except OSError:
                break

            # Handle each connection in its own thread
            t = threading.Thread(
                target=handle_client,
                args=(local_sock, (device_host, device_port)),
                daemon=True,
            )
            t.start()
    finally:
        srv.close()
        log("Server stopped")

# ---------------------------------------------------------------------------
# Daemon mode
# ---------------------------------------------------------------------------

def daemonize(listen_host, listen_port, device_host, device_port):
    """Fork into background and return child PID."""
    import subprocess

    log_path = Path.home() / ".local" / "run" / "adb-bridge.log"
    log_path.parent.mkdir(parents=True, exist_ok=True)

    # Re-launch ourselves in background, detached from terminal
    cmd = [
        sys.executable, __file__, "start",
        "--device", device_host,
        "--listen-port", str(listen_port),
        "--device-port", str(device_port),
        # NOT --daemon: subprocess runs foreground (detached via start_new_session)
    ]
    # Launch detached, log to file
    proc = subprocess.Popen(
        cmd,
        stdout=open(str(log_path), "a"),
        stderr=subprocess.STDOUT,
        stdin=subprocess.DEVNULL,
        start_new_session=True,
    )
    return proc.pid

# ---------------------------------------------------------------------------
# CLI commands
# ---------------------------------------------------------------------------

def cmd_start(args) -> None:
    # Auto-discover device port if not specified
    if args.device_port is None:
        print("Auto-discovering ADB connect port via mDNS...")
        port = discover_adb_port(args.device)
        if port:
            args.device_port = port
            print(f"Found device port: {port}")
        else:
            print(f"Discovery failed. Using default port {DEFAULT_DEVICE_PORT}.")
            args.device_port = DEFAULT_DEVICE_PORT

    if not args.daemon:
        # Foreground mode: just run the server
        write_pid()
        try:
            run_server(args.listen_host, args.listen_port, args.device, args.device_port)
        finally:
            remove_pid()
    else:
        # Daemon mode: fork child, write its PID, then exit
        pid = daemonize(args.listen_host, args.listen_port, args.device, args.device_port)
        time.sleep(1.0)
        if is_running(pid):
            write_pid()
            print(f"Started adb-bridge (PID {pid})")
            print(f"  Listen:   {args.listen_host}:{args.listen_port}")
            print(f"  Forward:  {args.device}:{args.device_port}")
            print(f"  Connect:  adb connect {args.listen_host}:{args.listen_port}")
        else:
            log_path = Path.home() / ".local" / "run" / "adb-bridge.log"
            if log_path.exists():
                print(f"Failed to start. See: {log_path}")
            else:
                print("Failed to start daemon. Unknown error.")

def cmd_discover(args):
    """Discover and print the current ADB connect port for the device."""
    port = discover_adb_port(args.device)
    if port:
        print(f"Device {args.device}: connect port {port}")
    else:
        print(f"Could not discover ADB port for {args.device}.")
        print("Ensure:")
        print("  - Device is on the same network")
        print("  - Wireless debugging is enabled on the device")
        sys.exit(1)

def cmd_status(args) -> None:
    pid = _find_bridge_pid()
    if is_running(pid):
        print(f"adb-bridge is running (PID {pid})")
        print(f"  Connect:  adb connect 127.0.0.1:{DEFAULT_LISTEN_PORT}")
    else:
        print("adb-bridge is not running")

def cmd_pair(args):
    """Pair with device: bridge pairing port -> adb pair -> reconnect on connect port."""
    import subprocess as _sub
    if not args.pair_port:
        print("Error: --pair-port is required.")
        print("Find it on your TV: Wireless debugging -> Pair device with pairing code")
        sys.exit(1)

    # Step 1: Start bridge on pairing port
    print(f"Starting bridge on localhost:{args.listen_port} -> {args.device}:{args.pair_port}")
    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind((args.listen_host, args.listen_port))
    srv.listen(1)
    srv.settimeout(60)
    print("Bridge listening. Awaiting adb connection...")

    # Step 2: Spawn adb pair in background thread
    pair_done = threading.Event()
    pair_result = [None, None]

    def run_pair():
        cmd = ["adb", "pair", f"127.0.0.1:{args.listen_port}"]
        try:
            inp = (args.code + "\n") if args.code else None
            proc = _sub.run(cmd, input=inp, capture_output=True, text=True, timeout=30)
            if proc.returncode == 0:
                pair_result[0] = True
                pair_result[1] = proc.stdout.strip()
            else:
                pair_result[0] = False
                pair_result[1] = proc.stderr.strip() or proc.stdout.strip()
        except _sub.TimeoutExpired:
            pair_result[0] = False
            pair_result[1] = "Pairing timed out (30s)"
        except Exception as e:
            pair_result[0] = False
            pair_result[1] = str(e)
        finally:
            pair_done.set()

    t = threading.Thread(target=run_pair, daemon=True)
    t.start()

    # Step 3: Accept and bridge the single connection
    print("Waiting for pairing connection (60s timeout)...")
    try:
        local, _ = srv.accept()
        print("Connection accepted from adb")
    except socket.timeout:
        print("Timeout waiting for pairing connection.")
        srv.close()
        sys.exit(1)

    try:
        remote = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        remote.settimeout(10)
        remote.connect((args.device, args.pair_port))
        print(f"Connected to TV on pairing port {args.pair_port}")
    except OSError as e:
        print(f"Failed to connect to TV: {e}")
        local.close()
        srv.close()
        sys.exit(1)

    # Bridge both directions
    def fwd(src, dst):
        try:
            while True:
                data = src.recv(65536)
                if not data:
                    break
                dst.sendall(data)
        except Exception:
            pass

    threading.Thread(target=fwd, args=(local, remote), daemon=True).start()
    threading.Thread(target=fwd, args=(remote, local), daemon=True).start()

    # Wait for pairing result
    pair_done.wait(timeout=30)
    srv.close()
    try:
        local.close()
    except Exception:
        pass
    try:
        remote.close()
    except Exception:
        pass

    if pair_result[0]:
        print(f"Pairing succeeded: {pair_result[1]}")
        # Auto-discover the current connect port (it changes after pairing)
        print("Discovering connect port via mDNS...")
        connect_port = discover_adb_port(args.device)
        if connect_port:
            print(f"Found connect port: {connect_port}")
        else:
            connect_port = args.connect_port
            print(f"Discovery failed, falling back to port {connect_port}")
        print(f"Reconnecting on port {connect_port}...")
        os.system(f"adb connect {args.device}:{connect_port}")
    else:
        print(f"Pairing failed: {pair_result[1]}")
        sys.exit(1)

def cmd_stop(args) -> None:
    pid = _find_bridge_pid()
    if not is_running(pid):
        pid = read_pid()
    if not is_running(pid):
        print("adb-bridge is not running")
        remove_pid()
        return
    print(f"Stopping adb-bridge (PID {pid})…")
    os.kill(pid, signal.SIGTERM)
    # Wait up to 5 seconds
    for _ in range(50):
        if not is_running(pid):
            break
        time.sleep(0.1)
    if is_running(pid):
        print("Force killing…")
        os.kill(pid, signal.SIGKILL)
    remove_pid()
    print("Stopped")

# ---------------------------------------------------------------------------
# Argument parser
# ---------------------------------------------------------------------------

def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        prog="adb-bridge",
        description="TCP proxy for adb connections — bypasses corporate network monitors.",
        epilog="Examples:\n"
               "  %(prog)s start                          # auto-discover port, run in foreground\n"
               "  %(prog)s start --daemon                 # auto-discover port, run in background\n"
               "  %(prog)s start --device 10.0.0.50       # custom device IP\n"
               "  %(prog)s discover                       # show current connect port\n"
               "  %(prog)s pair --pair-port 37851         # pair via bridge, then auto-reconnect\n"
               "  %(prog)s status                         # check if bridge is running\n"
               "  %(prog)s stop                           # stop the background daemon\n",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sub = p.add_subparsers(dest="command", required=True)

    # start
    sp = sub.add_parser("start", help="Start the proxy bridge")
    sp.add_argument("--device", default=DEFAULT_DEVICE, help=f"Android device IP (default: {DEFAULT_DEVICE})")
    sp.add_argument("--listen-port", type=int, default=DEFAULT_LISTEN_PORT, help=f"Local listen port (default: {DEFAULT_LISTEN_PORT})")
    sp.add_argument("--device-port", type=int, default=None, help="Device port (auto-discovered if omitted)")
    sp.add_argument("--listen-host", default="127.0.0.1", help="Local bind address (default: 127.0.0.1)")
    sp.add_argument("--daemon", action="store_true", help="Run in background")
    sp.set_defaults(func=cmd_start)

    # discover
    sp = sub.add_parser("discover", help="Auto-discover device ADB port via mDNS")
    sp.add_argument("--device", default=DEFAULT_DEVICE)
    sp.set_defaults(func=cmd_discover)

    # status
    sp = sub.add_parser("status", help="Show bridge status")
    sp.set_defaults(func=cmd_status)

    # stop
    sp = sub.add_parser("stop", help="Stop a running daemon")
    sp.set_defaults(func=cmd_stop)

    # pair — start bridge on pairing port, run adb pair, then exit
    sp = sub.add_parser("pair", help="Pair with device via bridge, then reconnect")
    sp.add_argument("--device", default=DEFAULT_DEVICE)
    sp.add_argument("--listen-port", type=int, default=DEFAULT_LISTEN_PORT)
    sp.add_argument("--pair-port", type=int, help="Pairing port (from TV screen)")
    sp.add_argument("--connect-port", type=int, default=DEFAULT_DEVICE_PORT,
                    help="Connect port for after pairing (default: %d)" % DEFAULT_DEVICE_PORT)
    sp.add_argument("--code", type=str, default=None,
                    help="6-digit pairing code (omit to enter interactively)")
    sp.set_defaults(func=cmd_pair)

    return p

# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    parser = build_parser()
    args = parser.parse_args()
    args.func(args)

if __name__ == "__main__":
    main()
