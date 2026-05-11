#!/usr/bin/env bash
# Build debug APK and deploy to connected Android TV device
# Usage: ./scripts/deploy.sh [--release]
set -euo pipefail

cd "$(dirname "$0")/.."

BUILD_TYPE="debug"
[[ "${1:-}" == "--release" ]] && BUILD_TYPE="release"

echo "🔨 Building ${BUILD_TYPE} APK..."
./gradlew "assemble${BUILD_TYPE^}" --quiet

APK_PATH=$(find app/build/outputs/apk/${BUILD_TYPE} -name "*.apk" -type f | head -1)
echo "📦 APK: ${APK_PATH}"

DEVICE=$(adb devices | grep -E "device$" | head -1 | awk '{print $1}')
if [ -z "$DEVICE" ]; then
    echo "❌ No device connected. Run: ./scripts/adb-connect.sh"
    exit 1
fi

echo "📺 Installing on ${DEVICE}..."
adb -s "$DEVICE" install -r "$APK_PATH"

echo "🚀 Launching app..."
adb -s "$DEVICE" shell am start -n com.iptvplayer/.MainActivity

echo "✅ Deployed and launched."
