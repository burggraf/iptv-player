#!/usr/bin/env bash
# dev-emulator — Build, install, and launch on Android TV emulator
# Usage: ./scripts/dev-emulator.sh [--snapshot|--screenshot|--logs|--clear]

set -euo pipefail

PACKAGE="com.iptvplayer"
ACTIVITY="com.iptvplayer.MainActivity"
AVD="Android_TV_API34"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log()    { echo -e "${BLUE}▸${NC} $1"; }
ok()     { echo -e "${GREEN}✓${NC} $1"; }
warn()   { echo -e "${YELLOW}⚠${NC} $1"; }
err()    { echo -e "${RED}✗${NC} $1"; }

# Check emulator is running
check_emulator() {
    if ! adb devices 2>/dev/null | grep -q emulator; then
        warn "No emulator detected. Starting $AVD..."
        ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" \
        ANDROID_HOME="$HOME/Library/Android/sdk" \
        ~/Library/Android/sdk/emulator/emulator -avd "$AVD" -no-window -no-audio -gpu swiftshader_indirect &
        log "Waiting for boot..."
        ANDROID_SDK_ROOT="$HOME/Library/Android/sdk" \
        adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; echo "BOOTED"'
        ok "Emulator ready"
    else
        ok "Emulator running"
    fi
}

# Build and install
build_install() {
    log "Building debug APK..."
    ./gradlew assembleDebug --quiet
    ok "Build complete"

    log "Installing to emulator..."
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    ok "Installed"
}

# Launch app
launch() {
    log "Launching app..."
    adb shell am start -n "$PACKAGE/.MainActivity" 2>/dev/null
    sleep 2
    ok "App launched"
}

# Snapshot UI tree
snapshot() {
    log "Capturing UI snapshot..."
    agent-device snapshot --platform android --target tv -i -c
}

# Screenshot
screenshot() {
    local out="screenshot-$(date +%Y%m%d-%H%M%S).png"
    log "Taking screenshot → $out"
    adb exec-out screencap -p > "$out"
    ok "Saved: $out"
    open "$out" 2>/dev/null || true
}

# Logs
logs() {
    log "Showing recent logs..."
    adb logcat -d | grep -i "$PACKAGE\|ExoPlayer\|iptv" | tail -80
}

# Diagnostic: screenshot + UI hierarchy + stream errors
diagnostic() {
    local dir="./debug_assets"
    mkdir -p "$dir"

    log "Capturing screenshot..."
    adb exec-out screencap -p > "$dir/ui.png"
    ok "Screenshot → $dir/ui.png"

    log "Capturing UI hierarchy..."
    agent-device snapshot --platform android --target tv -i -c > "$dir/hierarchy.txt"
    ok "Hierarchy → $dir/hierarchy.txt"

    log "Checking for stream errors..."
    local errors
    errors=$(adb logcat -d 2>/dev/null | grep -iE "403|404|HttpDataSource.*error|IOException|ExoPlayer.*error" | tail -10)
    if [[ -n "$errors" ]]; then
        warn "Stream errors found:"
        echo "$errors" | sed 's/^/  /'
    else
        ok "No stream errors detected"
    fi

    echo ""
    ok "Diagnostic complete → $dir/"
}

# Clear data
clear_data() {
    log "Clearing app data..."
    adb shell pm clear "$PACKAGE"
    ok "Data cleared"
}

# Parse mode
MODE="build"
for arg in "$@"; do
    case "$arg" in
        --snapshot|-s)     MODE="snapshot" ;;
        --screenshot)      MODE="screenshot" ;;
        --logs|-l)         MODE="logs" ;;
        --clear)           MODE="clear" ;;
        --diagnostic|-d)   MODE="diagnostic" ;;
        --help|-h)
            echo "Usage: $0 [--snapshot|--screenshot|--logs|--clear|--diagnostic]"
            echo "  --snapshot, -s     Capture UI tree (interactive elements)"
            echo "  --screenshot       Visual screenshot"
            echo "  --logs, -l         Show recent app logs"
            echo "  --clear            Clear app data"
            echo "  --diagnostic, -d   Screenshot + UI hierarchy + stream error check"
            echo "  (default: build + install + launch)"
            exit 0
            ;;
    esac
done

case "$MODE" in
    snapshot)    check_emulator; snapshot ;;
    screenshot)  check_emulator; screenshot ;;
    logs)        check_emulator; logs ;;
    clear)       check_emulator; clear_data ;;
    diagnostic)  check_emulator; diagnostic ;;
    *)           check_emulator; build_install; launch ;;
esac
