#!/usr/bin/env bash
# Kill headless emulator (if running), launch a visible one, build + install + launch latest debug app.

set -e

AVD="Android_TV_API34"
PACKAGE="com.iptvplayer"
ACTIVITY="com.iptvplayer.MainActivity"

echo "🔍 Checking for running emulator..."

# Kill existing emulator if found
if adb devices 2>/dev/null | grep -q "emulator-5554.*device"; then
  echo "🛑 Killing running emulator..."
  adb -s emulator-5554 emu kill 2>/dev/null || true
  sleep 3
else
  echo "✅ No running emulator found."
fi

echo "🚀 Launching visible emulator: $AVD"
$ANDROID_HOME/emulator/emulator -avd "$AVD" &

echo "⏳ Waiting for emulator to boot..."
adb wait-for-device
while [[ -z $(adb shell getprop sys.boot_completed 2>/dev/null) ]]; do sleep 1; done
echo "✅ Emulator booted"

# Unlock screen (some TV images lock on boot)
adb shell input keyevent 82 2>/dev/null || true
sleep 1

# Build + install + launch
echo "🔨 Building debug APK..."
./gradlew assembleDebug --quiet
echo "✅ Build complete"

echo "📦 Installing app..."
adb install -r app/build/outputs/apk/debug/app-debug.apk
echo "✅ Installed"

echo "🚀 Launching app..."
adb shell am start -n "$PACKAGE/.MainActivity"
echo "✅ App launched"

echo ""
echo "💡 Interact with the emulator:"
echo "   • Arrow keys in the emulator window = D-pad"
echo "   • Enter = Select / Center"
echo "   • Escape = Back"
echo "   • Or use adb directly:"
echo "       adb shell input keyevent 19  # UP"
echo "       adb shell input keyevent 20  # DOWN"
echo "       adb shell input keyevent 21  # LEFT"
echo "       adb shell input keyevent 22  # RIGHT"
echo "       adb shell input keyevent 23  # SELECT"
echo "       adb shell input keyevent 4   # BACK"
