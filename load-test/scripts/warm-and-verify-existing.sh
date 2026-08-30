#!/usr/bin/env bash
set -euo pipefail

app_base_url="${APP_BASE_URL:-http://127.0.0.1:8080}"
mock_base_url="${MOCK_BASE_URL:-http://127.0.0.1:18080}"
result_directory="${1:?result directory is required}"

requests=(
  '{"type":"SUSI","category":"ADMISSION_GUIDELINE","content":"수시 모집 일정은 언제인가요?"}'
  '{"type":"SUSI","category":"ADMISSION_GUIDELINE","content":"수시 지원 자격을 알려주세요."}'
  '{"type":"SUSI","category":"PASSING_RESULT","content":"수시 입시 결과를 알려주세요."}'
  '{"type":"JEONGSI","category":"ADMISSION_GUIDELINE","content":"정시 모집 일정을 알려주세요."}'
  '{"type":"JEONGSI","category":"PASSING_RESULT","content":"정시 입시 결과를 알려주세요."}'
  '{"type":"PYEONIP","category":"ADMISSION_GUIDELINE","content":"편입학 지원 자격을 알려주세요."}'
)

invoke_all() {
  local phase="$1"
  local process_ids=()
  local index=0
  for request in "${requests[@]}"; do
    curl --silent --show-error --fail \
      -H 'Content-Type: application/json' \
      -d "$request" \
      "$app_base_url/api/questions" \
      >"$result_directory/${phase}-${index}.json" &
    process_ids+=("$!")
    index=$((index + 1))
  done
  for process_id in "${process_ids[@]}"; do
    wait "$process_id"
  done
}

curl --silent --fail -X POST "$mock_base_url/reset" >/dev/null
invoke_all warmup

curl --silent --fail -X POST "$mock_base_url/reset" >/dev/null
invoke_all verification
verification_metrics="$(curl --silent --fail "$mock_base_url/metrics")"
echo "$verification_metrics" >"$result_directory/existing-path-verification.json"

if [[ "$(jq -r '.total' <<<"$verification_metrics")" != "0" ]]; then
  echo "existing-question verification called mock RAG: $verification_metrics" >&2
  exit 1
fi

curl --silent --fail -X POST "$mock_base_url/reset" >/dev/null
docker exec maru-egg-load-test-mysql mysql -uloadtest -ploadtest maru_egg_load_test \
  -e 'UPDATE questions SET view_count = 0' >/dev/null 2>&1

echo "existing-question path: verified without mock calls"
