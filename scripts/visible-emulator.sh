#!/usr/bin/env bash
# Kill headless emulator (if running) and launch a visible one you can interact with.

set -e

AVD="Android_TV_API34"

echo "🔍 Checking for running emulator..."

# Kill existing emulator if found
if adb devices | grep -q "emulator-5554.*device"; then
  echo "🛑 Killing running emulator..."
  adb -s emulator-5554 emu kill 2>/dev/null || true
  sleep 3
else
  echo "✅ No running emulator found."
fi

echo "🚀 Launching visible emulator: $AVD"
emulator -avd "$AVD" &

echo "⏳ Waiting for emulator to boot..."
adb wait-for-device
adb shell getprop sys.boot_completed 2>/dev/null || true

echo "✅ Emulator window should now be visible. Use arrow keys to navigate, Enter to select."
