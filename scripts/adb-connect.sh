#!/usr/bin/env bash
# Connect to Android TV device over WiFi
# Usage: ./scripts/adb-connect.sh [IP_ADDRESS]
set -euo pipefail

TV_IP="${1:-}"

if [ -z "$TV_IP" ]; then
    echo "🔍 Scanning for Android TV devices on local network..."
    SUBNET=$(ipconfig getifaddr en0 2>/dev/null || ip route get 1 2>/dev/null | awk '{print $7}' | cut -d. -f1-3)
    if [ -z "$SUBNET" ]; then
        echo "❌ Could not determine local subnet. Provide IP: $0 192.168.1.100"
        exit 1
    fi
    echo "   Subnet: ${SUBNET}.0/24 — probing ${SUBNET}.100–120"
    for i in $(seq 100 120); do
        IP="${SUBNET}.${i}"
        if nc -z -w1 "$IP" 5555 2>/dev/null; then
            TV_IP="$IP"
            echo "✅ Found Android TV at ${TV_IP}:5555"
            break
        fi
    done
    if [ -z "$TV_IP" ]; then
        echo "❌ No device found. Enable Developer Options → USB/Network debugging on TV."
        exit 1
    fi
fi

if adb devices | grep -q "$TV_IP:5555"; then
    echo "✅ Already connected to ${TV_IP}:5555"
else
    echo "📺 Connecting to ${TV_IP}:5555..."
    adb connect "$TV_IP:5555" || {
        echo "❌ Connection failed. Check developer options and network."
        exit 1
    }
fi

adb devices
echo "✅ Ready for deployment."
