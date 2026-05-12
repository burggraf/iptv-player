# AGENTS.md — AI Agent Instructions for IPTV Player

## Project Overview

**TiviMate clone** — IPTV player for Android TV. M3U/Xtream playlist loading, XMLTV EPG parsing, synced EPG grid, video preview, fullscreen playback, favorites, and catchup.

**Tech Stack:**
- **Language:** Kotlin
- **UI:** Jetpack Compose for TV (Material 3 for TV)
- **Video:** Media3 (ExoPlayer)
- **Architecture:** MVVM + Repository pattern
- **Async:** Kotlin Coroutines + Flow
- **DI:** Koin
- **Database:** Room (SQLite) — EPG cache + playlist metadata
- **Networking:** Ktor Client (OkHttp engine)
- **Package:** `com.iptvplayer`

**Full plan:** [`PLAN.md`](PLAN.md) — 7 phases from scaffold → testing.
**Project structure:** `app/src/main/java/com/iptvplayer/` — `core/`, `data/`, `di/`, `domain/`, `presentation/`.

**Current status:** Phase 0 (scaffold) → Phase 3 (video player) — app builds, runs on emulator, shows home screen with nav buttons (Open EPG, Favorites, Settings, Add Playlist).

---

## Emulator Dev Loop

Use the local Android TV emulator for fast iteration. No physical TV needed.

### Quick Commands

```bash
# Full cycle: build → install → launch
./scripts/dev-emulator.sh

# Snapshot UI tree (text-based, shows focus + elements)
./scripts/dev-emulator.sh --snapshot

# Screenshot (visual verification, opens in Preview)
./scripts/dev-emulator.sh --screenshot

# Check app logs
./scripts/dev-emulator.sh --logs

# Clear app data
./scripts/dev-emulator.sh --clear
```

### Workflow

1. **Build + Install + Launch** — `./scripts/dev-emulator.sh`
2. **Evaluate** — screenshot (visual) + snapshot (UI tree with focus state)
3. **Navigate** — send D-pad events to test focus flow
4. **Iterate** — fix code, rebuild, relaunch

### D-Pad Navigation (TV has no touch — focus is everything)

```bash
adb shell input keyevent 19   # UP
adb shell input keyevent 20   # DOWN
adb shell input keyevent 21   # LEFT
adb shell input keyevent 22   # RIGHT
adb shell input keyevent 23   # CENTER (Select/OK)
adb shell input keyevent 4    # BACK
adb shell input keyevent 3    # HOME

adb shell input keyevent 166  # CHANNEL_UP (next channel)
adb shell input keyevent 167  # CHANNEL_DOWN (prev channel)

adb shell input keyevent 112  # PLAY/PAUSE
adb shell input keyevent 85   # PLAY
adb shell input keyevent 86   # PAUSE
adb shell input keyevent 127  # STOP
```

### Verify Focus After Navigation

```bash
adb shell input keyevent 20   # move down
agent-device snapshot --platform android --target tv -i -c   # check isFocused: true
```

### Stream Debugging (ExoPlayer Log Patterns)

Pi can verify playback state via logcat — no video display needed.

| Goal | Command |
|------|---------|
| Verify stream URL loaded | `adb logcat -d | grep -i "MediaItem\|source\|uri" | tail -20` |
| Check buffering state | `adb logcat -d | grep -i "buffering\|state\|ExoPlayer" | tail -20` |
| Detect 403/404 errors | `adb logcat -d | grep -iE "403|404|HTTP.*error|IOException" | tail -20` |
| Verify playback started | `adb logcat -d | grep -i "STATE_READY\|STATE_BUFFERING\|playing" | tail -20` |
| Live stream log tail | `adb logcat | grep -iE "com.iptvplayer|ExoPlayer|HttpDataSource"` |

### Playback Validation Workflow

1. **Navigate** to channel → D-pad + snapshot to verify focus
2. **Select** channel → DPAD_CENTER
3. **Wait 5s** for stream to initialize
4. **Check logs** for 403/404 → `adb logcat -d | grep -iE "403|404|error" | grep -i iptv`
5. **Verify state** → `adb logcat -d | grep -i "STATE_READY" | tail -5`

### Scrcpy (Real-Time Visual Mirror)

While pi works via adb/agent-device in terminal, run scrcpy for instant visual feedback:

```bash
scrcpy --always-on-top --window-title "iptv-player-debug"
```

No physical TV needed. No network-install lag. Pi handles terminal; you watch the screen.

### Other Useful Commands

```bash
adb shell am force-stop com.iptvplayer   # Force stop
adb shell pm clear com.iptvplayer         # Clear app data
adb logcat | grep com.iptvplayer          # Live logs
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml  # UI hierarchy fallback
```

### Emulator Details

- **AVD:** `Android_TV_API34` — Android 14, TV 1080p, arm64-v8a
- **Main Activity:** `com.iptvplayer.MainActivity`
- **Launch:** `adb shell am start -n com.iptvplayer/.MainActivity`

### agent-device CLI

```bash
agent-device snapshot --platform android --target tv -i -c   # UI tree, interactive elements only, compact
agent-device boot --headless --platform android --target tv  # Headless boot
```

Use `-i -c` snapshot to check which element has `isFocused: true`.

### Skill Reference

Full reference: `.pi/skills/emulator-loop/SKILL.md` (Pi auto-discovers).

---

## Build & Test

```bash
./gradlew assembleDebug     # Build debug APK
./gradlew test              # Unit tests (no emulator)
./gradlew connectedAndroidTest  # Instrumentation tests (requires emulator)
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`
