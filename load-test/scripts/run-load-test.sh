#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
run_id="${RUN_ID:-$(date -u +%Y%m%dT%H%M%SZ)-${VARIANT:-refactored}}"
result_directory="${RESULT_DIR:-$repo_root/load-test/results/$run_id}"
app_base_url="${APP_BASE_URL:-http://127.0.0.1:8080}"
mock_base_url="${MOCK_BASE_URL:-http://127.0.0.1:18080}"
app_pid=""
mock_pid=""
collector_pid=""

for command in java node docker curl jq k6; do
  command -v "$command" >/dev/null || { echo "required command missing: $command" >&2; exit 2; }
done

mkdir -p "$result_directory"

stop_process() {
  local process_id="$1"
  if [[ -n "$process_id" ]] && kill -0 "$process_id" 2>/dev/null; then
    kill "$process_id" 2>/dev/null || true
    wait "$process_id" 2>/dev/null || true
  fi
}

cleanup() {
  stop_process "$collector_pid"
  stop_process "$app_pid"
  stop_process "$mock_pid"
}
trap cleanup EXIT INT TERM

wait_for_url() {
  local url="$1"
  local label="$2"
  for attempt in $(seq 1 180); do
    if curl --silent --fail "$url" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  echo "$label did not become ready: $url" >&2
  return 1
}

bash "$repo_root/load-test/scripts/validate-config.sh"
bash "$repo_root/load-test/scripts/reset-and-seed.sh" reset

node "$repo_root/load-test/mock-rag/server.mjs" >"$result_directory/mock.log" 2>&1 &
mock_pid=$!
wait_for_url "$mock_base_url/health" "mock RAG"

if [[ -z "${APP_JAR:-}" ]]; then
  "$repo_root/gradlew" --no-daemon bootJar >"$result_directory/build.log" 2>&1
  app_jar="$(find "$repo_root/build/libs" -maxdepth 1 -name '*.jar' -type f | head -n 1)"
else
  app_jar="$APP_JAR"
fi

java -jar "$app_jar" \
  --profile=load-test \
  --spring.profiles.active=load-test \
  --spring.config.additional-location="optional:file:$repo_root/src/main/resources/application-load-test.yml" \
  >"$result_directory/application.log" 2>&1 &
app_pid=$!
wait_for_url "$app_base_url/actuator/health" "application"

bash "$repo_root/load-test/scripts/reset-and-seed.sh" seed
APP_BASE_URL="$app_base_url" MOCK_BASE_URL="$mock_base_url" \
  bash "$repo_root/load-test/scripts/warm-and-verify-existing.sh" "$result_directory" \
  >"$result_directory/existing-path-verification.log"
curl --silent --fail -X POST "$mock_base_url/reset" >"$result_directory/mock-reset.json"

APP_BASE_URL="$app_base_url" MOCK_BASE_URL="$mock_base_url" \
  bash "$repo_root/load-test/scripts/collect-metrics.sh" "$result_directory" \
  >"$result_directory/collector.log" 2>&1 &
collector_pid=$!

{
  echo "run_id=$run_id"
  echo "variant=${VARIANT:-refactored}"
  echo "commit=$(git -C "$repo_root" rev-parse HEAD)"
  echo "started_at=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "java=$(java -version 2>&1 | head -n 1)"
  echo "k6=$(k6 version | head -n 1)"
  echo "docker=$(docker version --format '{{.Server.Version}}')"
  echo "hikari_max=${LOAD_TEST_HIKARI_MAX_POOL_SIZE:-20}"
  echo "tomcat_max=${LOAD_TEST_TOMCAT_MAX_THREADS:-200}"
  echo "smoke=${SMOKE:-false}"
} >"$result_directory/environment.txt"

set +e
BASE_URL="$app_base_url" RUN_ID="$run_id" RESULT_DIR="$result_directory" SMOKE="${SMOKE:-false}" \
  k6 run "$repo_root/load-test/k6/question-flow.js" \
  >"$result_directory/k6.log" 2>&1
k6_status=$?
set -e

sleep 2
stop_process "$collector_pid"
collector_pid=""
node "$repo_root/load-test/scripts/summarize-results.mjs" "$result_directory" \
  >"$result_directory/metrics-summary.stdout.json"

set +e
bash "$repo_root/load-test/scripts/validate-metrics.sh" "$result_directory" \
  >"$result_directory/metrics-validation.txt" 2>&1
metrics_status=$?
node "$repo_root/load-test/scripts/validate-run.mjs" "$result_directory" \
  >"$result_directory/run-validation.txt" 2>&1
run_status=$?
set -e

echo "finished_at=$(date -u +%Y-%m-%dT%H:%M:%SZ)" >>"$result_directory/environment.txt"
echo "k6_exit_code=$k6_status" >>"$result_directory/environment.txt"
echo "metrics_validation_exit_code=$metrics_status" >>"$result_directory/environment.txt"
echo "run_validation_exit_code=$run_status" >>"$result_directory/environment.txt"

if [[ $k6_status -ne 0 || $metrics_status -ne 0 || $run_status -ne 0 ]]; then
  echo "load test completed with failures; inspect $result_directory" >&2
  exit 1
fi

echo "load test completed: $result_directory"
