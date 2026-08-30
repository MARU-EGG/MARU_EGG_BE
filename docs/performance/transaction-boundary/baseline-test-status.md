# Baseline test status

## Scope

- Baseline commit: `c7e1268`
- Test command: `./gradlew test`
- Local Docker engine: OrbStack / Docker 29.4.0

## Initial result

- Total: 155
- Passed: 98
- Failed: 57
- Most integration failures happened before application assertions because Testcontainers could not start MySQL.
- Two unrelated unit tests also failed with pre-existing `NullPointerException`s in `FindAllQuestionsServiceTest` lines 72 and 92.

## Root cause

1. The test runtime resolved most Testcontainers modules to 1.19.8, but `testcontainers-jdbc` was pinned to 1.19.7.
2. The embedded docker-java client requested Docker API 1.32. Docker 29.4.0 rejects versions below 1.40, so provider discovery failed with HTTP 400.
3. After forcing API 1.40 for diagnosis, Docker discovery succeeded. The `mysql:latest` image then resolved to MySQL 26.7.0 and failed because the Testcontainers MySQL configuration passed the removed `innodb_log_file_size` option.

## Test infrastructure correction

- Align all Testcontainers modules with BOM 1.21.3 instead of mixing 1.19.7 and 1.19.8.
- Pin the integration-test database to `mysql:8.0` instead of the moving `latest` tag.
- Set the test JVM's docker-java `api.version` to 1.40. Docker Engine 29 rejects the client's 1.32 default, while 1.40 is the engine's documented minimum.

These changes only stabilize the local test infrastructure. They do not change the application transaction behavior being measured.

## Result after infrastructure correction

- `MaruEggApplicationTests.contextLoads`: passed against MySQL 8.0.
- Full suite: 155 total, 102 passed, 53 failed.
- Remaining failures are pre-existing application-test issues: unresolved controller-test properties, schema/Full-Text SQL initialization errors, and the two `FindAllQuestionsServiceTest` null dereferences.
- The transaction-boundary work therefore uses focused unit and integration suites, and reports the unrelated full-suite failures separately instead of presenting the repository as fully green.
