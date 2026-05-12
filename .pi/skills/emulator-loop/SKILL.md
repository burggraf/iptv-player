# Emulator Loop — Android TV Local Emulator Dev Loop

Use this skill when working on the IPTV Player Android TV app with a local emulator. Provides fast visual feedback without needing a physical TV.

## Environment

- **Package:** `com.iptvplayer`
- **Main Activity:** `com.iptvplayer.MainActivity`
- **AVD Name:** `Android_TV_API34`
- **Min SDK:** 26, **Target SDK:** 34

## Quick Commands

### Start Emulator (headless)
```bash
ANDROID_SDK_ROOT=~/Library/Android/sdk \
~/Library/Android/sdk/emulator/emulator -avd Android_TV_API34 -no-window -no-audio -gpu swiftshader_indirect &
# Or use agent-device:
agent-device boot --headless --platform android --target tv
```

### Wait for emulator ready
```bash
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done'
```

### Build, Install, Launch
```bash
./gradlew assembleDebug && \
adb install -r app/build/outputs/apk/debug/app-debug.apk && \
adb shell am start -n com.iptvplayer/.MainActivity
```

### Snapshot (UI tree via agent-device)
```bash
agent-device snapshot --platform android --target tv -i -c
```

### Screenshot
```bash
adb exec-out screencap -p > screenshot.png
# Opens screenshot:
open screenshot.png
```

### D-Pad Navigation (Android TV focus control)
```bash
adb shell input keyevent 19   # DPAD_UP
adb shell input keyevent 20   # DPAD_DOWN
adb shell input keyevent 21   # DPAD_LEFT
adb shell input keyevent 22   # DPAD_RIGHT
adb shell input keyevent 23   # DPAD_CENTER (Select/OK)
adb shell input keyevent 4    # BACK
adb shell input keyevent 3    # HOME

adb shell input keyevent 166  # CHANNEL_UP (next channel)
adb shell input keyevent 167  # CHANNEL_DOWN (prev channel)

adb shell input keyevent 112  # MEDIA_PLAY_PAUSE
adb shell input keyevent 85   # MEDIA_PLAY
adb shell input keyevent 86   # MEDIA_PAUSE
adb shell input keyevent 127  # MEDIA_STOP
```

### Verify Focus After Navigation
```bash
adb shell input keyevent 20   # move down
agent-device snapshot --platform android --target tv -i -c   # look for isFocused: true
```

### Stream Debugging (ExoPlayer Log Patterns)
```bash
# Verify stream URL loaded
adb logcat -d | grep -i "MediaItem\|source\|uri" | tail -20

# Check buffering state
adb logcat -d | grep -i "buffering\|state\|ExoPlayer" | tail -20

# Detect 403/404 errors
adb logcat -d | grep -iE "403|404|HTTP.*error|IOException" | tail -20

# Verify playback started
adb logcat -d | grep -i "STATE_READY\|STATE_BUFFERING\|playing" | tail -20

# Live stream log tail
adb logcat | grep -iE "com.iptvplayer|ExoPlayer|HttpDataSource"
```

### Playback Validation Workflow
1. **Navigate** to channel → D-pad + snapshot to verify focus
2. **Select** channel → DPAD_CENTER
3. **Wait 5s** for stream to initialize
4. **Check logs** for 403/404 → `adb logcat -d | grep -iE "403|404|error" | grep -i iptv`
5. **Verify state** → `adb logcat -d | grep -i "STATE_READY" | tail -5`

### Scrcpy (Real-Time Visual Mirror)
```bash
scrcpy --always-on-top --window-title "iptv-player-debug"
```
Run alongside pi terminal work. Pi handles adb/snapshots; you watch screen.

### Check Logs
```bash
adb logcat -d | grep -i "iptv\|com.iptvplayer\|ExoPlayer" | tail -50
# Live tail:
adb logcat | grep -i "iptv\|com.iptvplayer"
```

### UI Hierarchy (uiautomator fallback)
```bash
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml
```

### Force Stop
```bash
adb shell am force-stop com.iptvplayer
```

### Clear App Data
```bash
adb shell pm clear com.iptvplayer
```

### Combined Diagnostic (Screenshot + Hierarchy + Stream Errors)
```bash
./scripts/dev-emulator.sh --diagnostic
# Output: ./debug_assets/ui.png, ./debug_assets/hierarchy.txt, stream error check
```

## Workflow

1. **Build + Install:** `./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk`
2. **Launch:** `adb shell monkey -p com.iptvplayer -c android.intent.category.LEANBACK_LAUNCHER 1`
3. **Snapshot:** `agent-device snapshot --platform android --target tv -i -c` — see focus state + element tree
4. **Screenshot:** `adb exec-out screencap -p > screenshot.png` — visual verification
5. **Navigate:** D-pad keyevents to test focus flow
6. **Logs:** `adb logcat | grep com.iptvplayer` — runtime errors

## Key Points for Android TV

- **Focus is everything.** On TV there is no touch. Every interactive element must handle focus state.
- Use `snapshot -i` to see which element has `isFocused: true`.
- Leanback launcher category is required for TV app discovery.
- Test D-pad navigation in all 4 directions + center + back from every screen.
- Video playback requires emulated codec support (use `-gpu swiftshader_indirect`).
- Pi cannot see video but CAN verify playback via logcat — STATE_READY, buffering, HTTP errors.

## agent-device vs Pure ADB

| Task | agent-device | Pure ADB |
|------|-------------|----------|
| UI tree | `agent-device snapshot -i -c` | `adb shell uiautomator dump` + pull |
| Screenshot | `agent-device screenshot` | `adb exec-out screencap -p` |
| Boot | `agent-device boot --headless` | `emulator -avd ... &` |
| Input | — | `adb shell input keyevent N` |
| Logs | — | `adb logcat` |

Use agent-device for snapshots/screenshots (cleaner output). Use ADB for input, logs, install.
