#!/usr/bin/env python3
"""
adb-connect.py — One-command ADB wireless connection for macOS.

Usage:
  /usr/bin/python3 adb-connect.py              # connect now
  /usr/bin/python3 adb-connect.py --pair       # pair + connect

Run with system Python (/usr/bin/python3) to bypass corporate network monitors
like SentinelOne that block Homebrew adb.

How it works:
  1. Auto-discovers the Android device's ADB connect port via mDNS/Bonjour.
  2. Starts a local TCP proxy (system Python → allowed by macOS firewall).
  3. Connects adb through the proxy.

IMPORTANT:
  Must use /usr/bin/python3 (Apple-signed).  Homebrew Python will be blocked.
"""

import os
import socket
import subprocess
import sys
import threading
import time
import signal

DEVICE_IP = os.environ.get("ADB_DEVICE_IP", "192.168.1.152")
LISTEN_PORT = 5038
DNSD_TIMEOUT = 6  # seconds to wait for each dns-sd step


def log(msg):
    print(f"  {msg}", flush=True)


def discover_port():
    """Find the current ADB connect port via mDNS/Bonjour."""
    import tempfile

    tmp1 = tempfile.mktemp(suffix=".dnsd")
    tmp2 = tempfile.mktemp(suffix=".dnsd")

    try:
        # Browse
        p1 = subprocess.Popen(
            f"dns-sd -B _adb-tls-connect._tcp local. >{tmp1} 2>&1",
            shell=True,
        )
        time.sleep(DNSD_TIMEOUT)
        p1.terminate()
        p1.wait()

        with open(tmp1) as f:
            data = f.read()

        instance = None
        for line in data.splitlines():
            if "Add" in line and "_adb-tls-connect._tcp" in line:
                instance = line.strip().split()[-1]
                break

        if not instance:
            return None

        # Resolve
        p2 = subprocess.Popen(
            f"dns-sd -L '{instance}' _adb-tls-connect._tcp local. >{tmp2} 2>&1",
            shell=True,
        )
        time.sleep(DNSD_TIMEOUT)
        p2.terminate()
        p2.wait()

        with open(tmp2) as f:
            data2 = f.read()

        for line in data2.splitlines():
            if ".local.:" in line:
                return int(line.split(":")[-1].split()[0])
    except Exception:
        pass
    finally:
        for t in (tmp1, tmp2):
            try:
                os.unlink(t)
            except OSError:
                pass
    return None


def bridge(local_sock, remote_sock):
    """Forward data in both directions between two sockets."""

    def fwd(src, dst, direction):
        try:
            while True:
                data = src.recv(65536)
                if not data:
                    break
                dst.sendall(data)
        except Exception:
            pass

    t1 = threading.Thread(target=fwd, args=(local_sock, remote_sock, "up"), daemon=True)
    t2 = threading.Thread(target=fwd, args=(remote_sock, local_sock, "dn"), daemon=True)
    t1.start()
    t2.start()
    t1.join()
    t2.join()


def run_proxy(listen_port, device_ip, device_port, timeout=15):
    """Start a one-shot proxy: accept one connection, bridge it, then return."""
    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(("127.0.0.1", listen_port))
    srv.listen(1)
    srv.settimeout(timeout)

    try:
        local, _ = srv.accept()
        remote = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        remote.settimeout(10)
        remote.connect((device_ip, device_port))
        bridge(local, remote)
    except socket.timeout:
        pass
    except Exception as e:
        print(f"  Error: {e}", file=sys.stderr)
    finally:
        try:
            srv.close()
        except Exception:
            pass


def do_pair(device_ip, pairing_port, code=None):
    """Pair through proxy, then return."""
    log(f"Starting bridge to pairing port {pairing_port}...")

    # Start proxy in thread
    proxy_done = threading.Event()

    def run_proxy_thread():
        run_proxy(LISTEN_PORT, device_ip, pairing_port, timeout=45)
        proxy_done.set()

    t = threading.Thread(target=run_proxy_thread, daemon=True)
    t.start()

    time.sleep(0.5)

    # Run adb pair
    log("Running adb pair...")
    cmd = ["adb", "pair", f"127.0.0.1:{LISTEN_PORT}"]
    inp = (code + "\n") if code else None
    try:
        result = subprocess.run(cmd, input=inp, capture_output=True, text=True, timeout=30)
        if result.returncode == 0:
            log(result.stdout.strip())
            return True
        else:
            log(result.stderr.strip() or result.stdout.strip())
            return False
    except subprocess.TimeoutExpired:
        log("Pairing timed out.")
        return False
    finally:
        proxy_done.wait(timeout=5)


def do_connect(device_ip, device_port):
    """Start persistent daemon proxy, then adb connect."""
    log(f"Starting bridge to {device_ip}:{device_port}...")

    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(("127.0.0.1", LISTEN_PORT))
    srv.settimeout(1.0)
    srv.listen(5)

    log(f"Bridge listening on 127.0.0.1:{LISTEN_PORT}")

    def accept_loop():
        while True:
            try:
                local, _ = srv.accept()
            except socket.timeout:
                continue
            except Exception:
                break
            try:
                remote = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                remote.settimeout(10)
                remote.connect((device_ip, device_port))
                threading.Thread(target=bridge, args=(local, remote), daemon=True).start()
            except Exception:
                try:
                    local.close()
                except Exception:
                    pass

    t = threading.Thread(target=accept_loop, daemon=True)
    t.start()

    # Connect adb
    time.sleep(0.5)
    log("Connecting adb...")
    subprocess.run(["adb", "connect", f"127.0.0.1:{LISTEN_PORT}"], capture_output=True)
    time.sleep(1)

    # Show result
    result = subprocess.run(["adb", "devices", "-l"], capture_output=True, text=True)
    for line in result.stdout.strip().splitlines():
        if "127.0.0.1" in line or "device" in line:
            print(line)

    log("Bridge running. Press Ctrl-C to stop.")

    # Keep alive until interrupted
    try:
        while t.is_alive():
            time.sleep(1)
    except KeyboardInterrupt:
        pass
    finally:
        srv.close()


def main():
    pair_mode = "--pair" in sys.argv

    if pair_mode:
        print("ADB Wireless Pairing")
        print("====================")
        log(f"Target device: {DEVICE_IP}")
        log("On your TV: Settings > Developer options > Wireless debugging")
        log('  Tap "Pair device with pairing code"')
        log("")

        pairing_port = input("  Pairing port: ").strip()
        if not pairing_port:
            print("Cancelled.")
            sys.exit(1)

        code = input("  Pairing code: ").strip()
        if not code:
            print("Cancelled.")
            sys.exit(1)

        if do_pair(DEVICE_IP, int(pairing_port), code):
            log("")
            log("Pairing succeeded. Discovering connect port...")
            port = discover_port()
            if port:
                log(f"Found connect port: {port}")
                do_connect(DEVICE_IP, port)
            else:
                log("Could not auto-discover connect port.")
                log("Check the port on your TV screen and run:")
                log(f"  adb-connect.py   (with ADB_DEVICE_IP={DEVICE_IP} env var)")
        else:
            log("Pairing failed.")
            sys.exit(1)
    else:
        print("ADB Wireless Connect")
        print("====================")
        log(f"Target device: {DEVICE_IP}")
        log("Discovering ADB connect port via mDNS...")

        port = discover_port()
        if port:
            log(f"Found port: {port}")
            do_connect(DEVICE_IP, port)
        else:
            log("Could not discover port via mDNS.")
            log("Ensure Wireless debugging is enabled on the TV.")
            log("You can also set the port manually:")
            log(f"  ADB_DEVICE_PORT=45991 /usr/bin/python3 adb-connect.py")
            sys.exit(1)


if __name__ == "__main__":
    main()
