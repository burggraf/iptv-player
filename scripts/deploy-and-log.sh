#!/usr/bin/env bash
# Deploy + stream logcat filtered to app
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/deploy.sh
echo ""
echo "📋 Streaming logs (Ctrl+C to stop)..."
adb logcat -c
adb logcat | grep -E "com.iptvplayer|ExoPlayer|IptvPlayer"
