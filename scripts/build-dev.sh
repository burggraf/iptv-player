#!/usr/bin/env bash
# Build debug APK and install on connected Android TV device.
# Usage: ./scripts/build-dev.sh [--no-launch] [--no-logs]
set -euo pipefail

cd "$(dirname "$0")/.."

LAUNCH=true
LOGS=true
for arg in "$@"; do
    case "$arg" in
        --no-launch) LAUNCH=false ;;
        --no-logs)   LOGS=false ;;
    esac
done

echo "🔨 Building debug APK..."
./gradlew :app:assembleDebug --quiet

APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" -type f | head -1)
if [ -z "$APK_PATH" ]; then
    echo "❌ No debug APK found after build."
    exit 1
fi
echo "📦 APK: ${APK_PATH} ($(du -h "$APK_PATH" | cut -f1))"

# Check for connected device
DEVICES=$(adb devices | grep -E "device$" | wc -l | tr -d ' ')
if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No device connected. Run: ./scripts/adb-connect.sh [TV_IP]"
    exit 1
fi

DEVICE=$(adb devices | grep -E "device$" | head -1 | awk '{print $1}')
echo "📺 Installing on ${DEVICE}..."
adb -s "$DEVICE" install -r "$APK_PATH"

if $LAUNCH; then
    echo "🚀 Launching app..."
    adb -s "$DEVICE" shell am start -n com.iptvplayer/.MainActivity
fi

echo "✅ Debug build deployed."

if $LOGS; then
    echo "📋 Streaming logs (Ctrl+C to stop)..."
    adb logcat -c
    adb logcat | grep -E "com.iptvplayer|ExoPlayer|IptvPlayer|AndroidRuntime|FATAL"
fi
