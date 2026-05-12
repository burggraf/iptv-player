#!/usr/bin/env bash
# Build release APK and install on connected Android TV device.
# Usage: ./scripts/build-release.sh [--launch] [--logs] [--keystore PATH] [--key-alias ALIAS]
#
# Requires a signing keystore. Set env vars or use flags:
#   RELEASE_KEYSTORE_PATH  — path to .jks keystore (default: ~/.android/release.jks)
#   RELEASE_KEYSTORE_PASS  — keystore password (reads from env or prompts)
#   RELEASE_KEY_ALIAS      — key alias within keystore
#   RELEASE_KEY_PASS       — key password (reads from env or prompts)
set -euo pipefail

cd "$(dirname "$0")/.."

LAUNCH=false
LOGS=false
for arg in "$@"; do
    case "$arg" in
        --launch)     LAUNCH=true ;;
        --logs)       LOGS=true ;;
        --keystore)   shift; RELEASE_KEYSTORE_PATH="$1" ;;
        --key-alias)  shift; RELEASE_KEY_ALIAS="$1" ;;
    esac
done

KEYSTORE_PATH="${RELEASE_KEYSTORE_PATH:-${RELEASE_KEYSTORE_PATH:-~/.android/release.jks}}"
KEY_ALIAS="${RELEASE_KEY_ALIAS:-${RELEASE_KEY_ALIAS:-iptvplayer}}"

# Resolve keystore path
KEYSTORE_PATH=$(eval echo "$KEYSTORE_PATH")

if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "🔑 No keystore found at: ${KEYSTORE_PATH}"
    echo ""
    echo "   Create one with:"
    echo "   keytool -genkeypair -v -keystore ~/.android/release.jks \\"
    echo "     -alias iptvplayer -keyalg RSA -keysize 2048 -validity 10000"
    echo ""
    read -rp "Create a debug keystore now? [y/N] " confirm
    if [[ "$confirm" =~ ^[Yy] ]]; then
        mkdir -p "$(dirname "$KEYSTORE_PATH")"
        keytool -genkeypair -v \
            -keystore "$KEYSTORE_PATH" \
            -alias "$KEY_ALIAS" \
            -keyalg RSA -keysize 2048 -validity 10000 \
            -storepass android -keypass android \
            -dname "CN=IPTV Player, OU=Dev, O=Local, C=US"
        echo "✅ Keystore created at ${KEYSTORE_PATH} (password: android)"
    else
        echo "❌ Keystore required for release build."
        exit 1
    fi
fi

# Read passwords (env var or prompt)
STORE_PASS="${RELEASE_KEYSTORE_PASS:-}"
KEY_PASS="${RELEASE_KEY_PASS:-}"

if [ -z "$STORE_PASS" ]; then
    read -rsp "   Keystore password: " STORE_PASS
    echo ""
fi
if [ -z "$KEY_PASS" ]; then
    read -rsp "   Key password: " KEY_PASS
    echo ""
fi

echo "🔨 Building release APK (ProGuard + signed)..."

./gradlew :app:assembleRelease \
    -Pandroid.injected.signing.store.file="$KEYSTORE_PATH" \
    -Pandroid.injected.signing.store.password="$STORE_PASS" \
    -Pandroid.injected.signing.key.alias="$KEY_ALIAS" \
    -Pandroid.injected.signing.key.password="$KEY_PASS" \
    --quiet

APK_PATH=$(find app/build/outputs/apk/release -name "*.apk" -type f | head -1)
if [ -z "$APK_PATH" ]; then
    echo "❌ No release APK found after build."
    exit 1
fi
echo "📦 APK: ${APK_PATH} ($(du -h "$APK_PATH" | cut -f1))"

# Check for connected device (|| true prevents pipefail from killing script when grep finds nothing)
DEVICE_LIST=$(adb devices 2>&1 | grep -E "\tdevice$" || true)
if [ -z "$DEVICE_LIST" ]; then
    echo "❌ No device connected. Run: ./scripts/adb-connect.sh [TV_IP]"
    exit 1
fi

DEVICE=$(echo "$DEVICE_LIST" | head -1 | awk '{print $1}')
echo "📺 Installing on ${DEVICE}..."
adb -s "$DEVICE" install -r "$APK_PATH"

if $LAUNCH; then
    echo "🚀 Launching app..."
    adb -s "$DEVICE" shell am start -n com.iptvplayer/.MainActivity
fi

echo "✅ Release build deployed."

if $LOGS; then
    echo "📋 Streaming logs (Ctrl+C to stop)..."
    adb logcat -c
    adb logcat | grep -E "com.iptvplayer|ExoPlayer|IptvPlayer|AndroidRuntime|FATAL"
fi
