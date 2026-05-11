#!/usr/bin/env bash
# Start mock IPTV dev server
# Usage: ./scripts/run-dev-server.sh [port]
set -euo pipefail
cd "$(dirname "$0")/../dev-server"

PORT="${1:-8080}"

if ! command -v node &>/dev/null; then
    echo "❌ Node.js required. Install: brew install node"
    exit 1
fi

[ ! -d "node_modules" ] && echo "📦 Installing deps..." && npm install --silent

echo "🌐 Mock IPTV server → http://localhost:${PORT}"
echo "   M3U:    http://localhost:${PORT}/playlist.m3u"
echo "   Xtream: http://localhost:${PORT}/player_api.php?username=demo&password=demo"
echo "   EPG:    http://localhost:${PORT}/epg.xml"
echo ""

node src/server.js --port "$PORT"
