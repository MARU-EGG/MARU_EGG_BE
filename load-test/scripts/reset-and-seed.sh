#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
compose_file="$repo_root/load-test/docker-compose.yml"
container_name="maru-egg-load-test-mysql"
action="${1:-reset}"

wait_for_mysql() {
  for attempt in $(seq 1 90); do
    if docker exec "$container_name" mysqladmin ping -h 127.0.0.1 -uloadtest -ploadtest --silent >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  echo "MySQL did not become ready" >&2
  return 1
}

docker compose -f "$compose_file" up -d mysql
wait_for_mysql

case "$action" in
  reset)
    docker exec "$container_name" mysql -uroot -proot -e \
      "DROP DATABASE IF EXISTS maru_egg_load_test; CREATE DATABASE maru_egg_load_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci; GRANT ALL PRIVILEGES ON maru_egg_load_test.* TO 'loadtest'@'%';"
    ;;
  seed)
    docker exec -i "$container_name" mysql -uloadtest -ploadtest < "$repo_root/load-test/sql/seed-existing-questions.sql"
    ;;
  *)
    echo "Usage: $0 reset|seed" >&2
    exit 2
    ;;
esac
