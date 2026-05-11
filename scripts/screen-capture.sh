#!/usr/bin/env bash
# Capture screenshot from Android TV
# Usage: ./scripts/screen-capture.sh [output_path]
set -euo pipefail
OUTPUT="${1:-screenshot-$(date +%Y%m%d-%H%M%S).png}"
echo "📸 Capturing screenshot..."
adb exec-out screencap -p > "$OUTPUT"
echo "✅ Saved to ${OUTPUT}"
open "$OUTPUT" 2>/dev/null || echo "   Open: ${OUTPUT}"
