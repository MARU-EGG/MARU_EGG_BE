#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
exec node "$repo_root/load-test/scripts/validate-metrics.mjs" "${1:?result directory is required}"
