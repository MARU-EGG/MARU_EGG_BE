#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
profile="$repo_root/src/main/resources/application-load-test.yml"
compose="$repo_root/load-test/docker-compose.yml"
seed="$repo_root/load-test/sql/seed-existing-questions.sql"

test -f "$profile"
test -f "$compose"
test -f "$seed"

grep -q 'open-in-view: false' "$profile"
grep -q 'maximum-pool-size:' "$profile"
grep -q 'connection-timeout:' "$profile"
grep -q 'prometheus, health, metrics' "$profile"
grep -q 'LOAD_TEST_MOCK_URL' "$profile"
grep -q 'maru_egg_load_test' "$profile"
grep -q 'mysql:8.0' "$compose"

echo "load-test configuration: valid"
