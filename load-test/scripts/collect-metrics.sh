#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
result_directory="${1:?result directory is required}"

mkdir -p "$result_directory"
exec node "$repo_root/load-test/scripts/collector.mjs" "$result_directory"
