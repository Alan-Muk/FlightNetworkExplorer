#!/usr/bin/env bash
# scripts/verify.sh
#
# Smoke-tests the full FlightNetworkExplorer stack.
#
# Assumes all three services are already running:
#   - graph service: http://localhost:8000
#   - backend:       http://localhost:8080
#   - frontend:      http://localhost:5173  (optional, only checked if reachable)
#
# Usage:
#   ./scripts/verify.sh
#   ./scripts/verify.sh --strict   # also fail if the frontend isn't up
#
# Exit codes:
#   0 = all checks passed
#   1 = at least one check failed

set -uo pipefail

GRAPH_URL="${GRAPH_URL:-http://localhost:8000}"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:5173}"

STRICT=false
if [[ "${1:-}" == "--strict" ]]; then
  STRICT=true
fi

PASS=0
FAIL=0
FAILURES=()

# ─── helpers ────────────────────────────────────────────────────────────────

green()  { printf "\033[32m%s\033[0m" "$1"; }
red()    { printf "\033[31m%s\033[0m" "$1"; }
yellow() { printf "\033[33m%s\033[0m" "$1"; }
grey()   { printf "\033[90m%s\033[0m" "$1"; }

check() {
  local name="$1"
  local url="$2"
  local expect="${3:-200}"

  local status
  status=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$url" 2>/dev/null || echo "000")

  if [[ "$status" == "$expect" ]]; then
    printf "  %s %s\n" "$(green "✅")" "$name"
    PASS=$((PASS+1))
  else
    printf "  %s %s %s\n" "$(red "❌")" "$name" "$(grey "(expected $expect, got $status)")"
    FAIL=$((FAIL+1))
    FAILURES+=("$name ($url)")
  fi
}

section() {
  printf "\n%s\n" "$(yellow "$1")"
}

# ─── graph service ───────────────────────────────────────────────────────────

section "Graph service ($GRAPH_URL)"
check "root endpoint" "$GRAPH_URL/"

# ─── backend — data layer ────────────────────────────────────────────────────

section "Backend — data ($BACKEND_URL)"
check "GET /api/airports/AMS" "$BACKEND_URL/api/airports/AMS"
check "GET /api/airports/XXX (expect 404/400)" "$BACKEND_URL/api/airports/XXX" "404"
check "GET /api/network/AMS" "$BACKEND_URL/api/network/AMS"
check "GET /api/hubs?limit=1" "$BACKEND_URL/api/hubs?limit=1"
check "GET /api/airport/AMS/stats" "$BACKEND_URL/api/airport/AMS/stats"

# ─── backend — graph service integration ─────────────────────────────────────

section "Backend — graph integration ($BACKEND_URL)"
check "GET /api/graph/connections/AMS" "$BACKEND_URL/api/graph/connections/AMS"
check "GET /api/graph/path/AMS/JFK" "$BACKEND_URL/api/graph/path/AMS/JFK"

# ─── frontend ────────────────────────────────────────────────────────────────

section "Frontend ($FRONTEND_URL)"
if curl -sf --max-time 3 "$FRONTEND_URL" > /dev/null 2>&1; then
  printf "  %s dev server reachable\n" "$(green "✅")"
  PASS=$((PASS+1))
elif [[ "$STRICT" == "true" ]]; then
  printf "  %s dev server not reachable (--strict)\n" "$(red "❌")"
  FAIL=$((FAIL+1))
  FAILURES+=("frontend dev server ($FRONTEND_URL)")
else
  printf "  %s dev server not reachable %s\n" "$(yellow "⚠️")" "$(grey "(not in --strict mode)")"
fi

# ─── summary ─────────────────────────────────────────────────────────────────

printf "\n"
if [[ $FAIL -eq 0 ]]; then
  printf "%s  %d passed\n" "$(green "✅ All checks passed.")" "$PASS"
  exit 0
else
  printf "%s  %d passed, %d failed\n" "$(red "❌ Some checks failed.")" "$PASS" "$FAIL"
  printf "\nFailed:\n"
  for f in "${FAILURES[@]}"; do
    printf "  - %s\n" "$f"
  done
  exit 1
fi