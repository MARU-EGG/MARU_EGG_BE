# Transaction Boundary Load Test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Release the MySQL connection before every 5-10 second RAG wait and prove the lifecycle under a deterministic 60% existing-answer and 40% new-question load that reaches 3,000 VU.

**Architecture:** `ProcessQuestionService` and `ProcessAnswerService` become non-transactional orchestrators. Qualified read and write `TransactionTemplate` instances create short database phases around projection reads and writes, while similarity computation and the cold `Mono` subscription run outside a transaction. A separate mock RAG process, dedicated MySQL load-test database, k6 scenario, and metric sampler produce comparable baseline and refactored evidence.

**Tech Stack:** Java 17, Spring Boot 3.3, Spring TransactionTemplate, JPA/Hibernate, MySQL 8, HikariCP, MockWebServer, Node.js HTTP mock, k6, Micrometer Actuator, shell automation.

---

### Task 1: Establish a Reproducible Test Baseline

**Files:**
- Create: `docs/performance/transaction-boundary/baseline-test-status.md`
- Inspect: `src/test/java/mju/iphak/maru_egg/TestcontainersConfiguration.java`
- Inspect: `src/test/java/mju/iphak/maru_egg/question/application/find/FindAllQuestionsServiceTest.java`

- [ ] **Step 1: Capture the current failure classes**

Run:

```bash
./gradlew test --stacktrace
```

Record the exact test count, Docker provider exception, and the two existing `FindAllQuestionsServiceTest` null failures. Do not change production code.

- [ ] **Step 2: Diagnose Testcontainers at the Docker boundary**

Run:

```bash
docker info
docker context show
docker context inspect
ls -l /var/run/docker.sock "$HOME/.docker/run/docker.sock" 2>/dev/null
./gradlew test --tests mju.iphak.maru_egg.MaruEggApplicationTests --info
```

Expected: identify the exact socket or provider-strategy mismatch. Apply only the confirmed environment setting to the test command; do not commit a machine-specific Docker socket.

- [ ] **Step 3: Re-run the focused context test**

Run the corrected environment command against `MaruEggApplicationTests`.

Expected: the context test reaches MySQL startup. If it fails for a new reason, record that reason before continuing.

- [ ] **Step 4: Write the baseline record**

The document must contain:

```markdown
# Baseline Test Status

- Commit: c7e1268
- Command: `./gradlew test`
- Total/passed/failed: `155 total, 98 passed, 57 failed`
- Environment failure: `Testcontainers DockerClientProviderStrategy failed while Docker CLI itself reported server 29.4.0`
- Pre-existing code failures: `FindAllQuestionsServiceTest lines 72 and 92 raised NullPointerException`
- Decision: transaction-boundary tests are evaluated separately and the full suite is re-run after environment recovery.
```

- [ ] **Step 5: Commit the baseline record**

```bash
git add docs/performance/transaction-boundary/baseline-test-status.md
git commit -m "test: record transaction refactor baseline"
```

### Task 2: Drive Transaction Boundaries with Failing Unit Tests

**Files:**
- Modify: `src/test/java/mju/iphak/maru_egg/question/application/process/ProcessQuestionServiceTest.java`
- Modify: `src/test/java/mju/iphak/maru_egg/answer/application/command/process/ProcessAnswerServiceTest.java`
- Create: `src/main/java/mju/iphak/maru_egg/common/config/TransactionTemplateConfig.java`
- Modify: `src/main/java/mju/iphak/maru_egg/question/application/query/process/ProcessQuestionService.java`
- Modify: `src/main/java/mju/iphak/maru_egg/answer/application/command/process/ProcessAnswerService.java`
- Modify: `src/main/java/mju/iphak/maru_egg/answer/application/query/rag/RagAnswerService.java`
- Modify: `src/main/java/mju/iphak/maru_egg/answer/application/command/create/CreateRAGAnswerService.java`

- [ ] **Step 1: Write the failing existing-answer transaction-order test**

Construct real `TransactionTemplate` objects with a recording `PlatformTransactionManager`. Assert the event order:

```text
read-begin -> candidate-query -> read-commit -> similarity
-> write-begin -> existing-answer-load -> write-commit
```

Assert that `TransactionSynchronizationManager.isActualTransactionActive()` is true during repository work and false during similarity calculation.

- [ ] **Step 2: Run the existing-answer test and verify RED**

```bash
./gradlew test --tests '*ProcessQuestionServiceTest*'
```

Expected: FAIL because the current class-level transaction keeps similarity work inside the outer transaction and the constructor does not accept explicit templates.

- [ ] **Step 3: Write the failing new-question transaction-order test**

Assert this event order:

```text
read-begin -> candidate-query -> read-commit
-> rag-subscribe-without-transaction
-> write-begin -> answer-save -> write-commit
```

The `Mono.defer` test double must capture transaction activity at subscription time, not at publisher construction time.

- [ ] **Step 4: Run the new-question test and verify RED**

```bash
./gradlew test --tests '*ProcessAnswerServiceTest*'
```

Expected: FAIL because the current `ProcessAnswerService` is transactional and saves through `REQUIRES_NEW`.

- [ ] **Step 5: Add qualified templates**

Implement fixed configuration:

```java
@Configuration
public class TransactionTemplateConfig {
    @Bean("readTransactionTemplate")
    TransactionTemplate readTransactionTemplate(PlatformTransactionManager manager) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.setReadOnly(true);
        return template;
    }

    @Bean("writeTransactionTemplate")
    TransactionTemplate writeTransactionTemplate(PlatformTransactionManager manager) {
        return new TransactionTemplate(manager);
    }
}
```

- [ ] **Step 6: Refactor `ProcessQuestionService` minimally**

Remove the class-level transaction. Inject both qualified templates. Put only `questionRepository.searchQuestions(...)` inside the read template, run similarity after it returns, and call `findQuestion` inside the write template for a hit.

- [ ] **Step 7: Refactor `ProcessAnswerService` minimally**

Remove its class-level transaction. Subscribe to and block on the RAG `Mono` before opening the write template. Put only `createRAGAnswer.invoke(...)` inside `writeTransactionTemplate.executeWithoutResult(...)`.

- [ ] **Step 8: Remove ineffective propagation annotations**

Remove `NOT_SUPPORTED` from `RagAnswerService`. Replace `REQUIRES_NEW` on `CreateRAGAnswerService` with ordinary `@Transactional` so standalone calls are protected and template calls join the current write transaction.

- [ ] **Step 9: Run focused GREEN tests**

```bash
./gradlew test --tests '*ProcessQuestionServiceTest*' --tests '*ProcessAnswerServiceTest*' --tests '*RagAnswerServiceTest*' --tests '*CreateRAGAnswerServiceTest*'
```

Expected: all focused tests pass and the event-order assertions prove subscription occurs without a transaction.

- [ ] **Step 10: Commit transaction boundaries**

```bash
git add src/main/java src/test/java
git commit -m "refactor: isolate database transactions from rag calls"
```

### Task 3: Make View Count Updates Correct Under Concurrency

**Files:**
- Modify: `src/main/java/mju/iphak/maru_egg/question/repository/QuestionRepository.java`
- Modify: `src/main/java/mju/iphak/maru_egg/question/application/query/find/FindQuestionService.java`
- Create: `src/test/java/mju/iphak/maru_egg/question/application/query/find/FindQuestionServiceTest.java`
- Create: `src/test/java/mju/iphak/maru_egg/question/repository/QuestionViewCountRepositoryTest.java`

- [ ] **Step 1: Write a failing service test**

Assert that loading an answer calls `questionRepository.incrementViewCount(questionId)` once and does not mutate a managed `Question` with `incrementViewCount()`.

- [ ] **Step 2: Write a failing repository concurrency test**

Create one question, launch a bounded set of concurrent atomic increments, and assert the final count equals the number of successful calls.

- [ ] **Step 3: Run both tests and verify RED**

```bash
./gradlew test --tests '*FindQuestionServiceTest*' --tests '*QuestionViewCountRepositoryTest*'
```

Expected: the method is missing and the current dirty-checking update loses or cannot prove concurrent increments.

- [ ] **Step 4: Implement the atomic update**

Add:

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("update Question q set q.viewCount = q.viewCount + 1 where q.id = :questionId")
int incrementViewCount(@Param("questionId") Long questionId);
```

Make `FindQuestionService` a write transaction, require an affected-row count of one, and map the complete response DTO before returning.

- [ ] **Step 5: Run GREEN tests**

Run the focused service and repository tests again.

Expected: exact final count and response mapping pass.

- [ ] **Step 6: Commit the atomic counter change**

```bash
git add src/main/java/mju/iphak/maru_egg/question src/test/java/mju/iphak/maru_egg/question
git commit -m "fix: update question view count atomically"
```

### Task 4: Prove Connection Release During a Blocked RAG Call

**Files:**
- Create: `src/test/java/mju/iphak/maru_egg/question/application/process/QuestionConnectionLifecycleIntegrationTest.java`
- Modify: `src/test/resources/application-integration-test.yml`

- [ ] **Step 1: Write the failing connection-lifecycle integration test**

Configure HikariCP with maximum pool size 2. Seed a question search miss. Use a controllable mock response with latches:

```text
request reaches mock -> hold response -> inspect HikariPoolMXBean
-> release response -> wait for save commit -> inspect again
```

During the held response assert:

```java
assertThat(transactionActiveAtSubscription.get()).isFalse();
assertThat(hikariPoolMXBean.getActiveConnections()).isZero();
assertThat(hikariPoolMXBean.getThreadsAwaitingConnection()).isZero();
```

After save completion assert active connections return to zero and the question, answer, and references exist.

- [ ] **Step 2: Verify the test is RED against the baseline implementation**

Run the test against commit `c7e1268` in a temporary baseline worktree or by applying the test without the refactor.

Expected: while the mock response is blocked, one connection remains active or the transaction-at-subscription assertion fails.

- [ ] **Step 3: Run the same test on the refactored branch**

```bash
./gradlew test --tests '*QuestionConnectionLifecycleIntegrationTest*'
```

Expected: PASS with zero active and zero pending connections during the RAG wait.

- [ ] **Step 4: Commit the lifecycle proof**

```bash
git add src/test/java/mju/iphak/maru_egg/question/application/process/QuestionConnectionLifecycleIntegrationTest.java src/test/resources/application-integration-test.yml
git commit -m "test: prove rag wait releases database connection"
```

### Task 5: Build the Dedicated Load-Test Runtime

**Files:**
- Create: `src/main/resources/application-load-test.yml`
- Create: `load-test/docker-compose.yml`
- Create: `load-test/mysql/init/001-schema-support.sql`
- Create: `load-test/scripts/reset-and-seed.sh`
- Create: `load-test/sql/seed-existing-questions.sql`
- Modify: `.gitignore`

- [ ] **Step 1: Write failing configuration validation**

Create a shell validation that requires the load-test profile to expose Prometheus, disable OSIV, set an explicit Hikari pool size and timeout, point WebClient to the external mock, and use a dedicated database name.

- [ ] **Step 2: Run validation and verify RED**

Expected: fail because the profile and load-test compose files do not exist.

- [ ] **Step 3: Add the load-test profile**

Use environment-backed properties for DB URL, credentials, mock URL, Hikari maximum pool size, connection timeout, and server port. Set `ddl-auto=create` only for the disposable load-test database and expose `prometheus`, `health`, and `metrics`.

- [ ] **Step 4: Add disposable MySQL and deterministic seed tooling**

Start MySQL 8 with `innodb_ft_min_token_size=1`, wait for health, start the application so Hibernate creates tables, create the required Full-Text index, and insert fixed questions, answers, and references. The reset script must drop and recreate only the dedicated load-test database.

- [ ] **Step 5: Run configuration validation GREEN**

Expected: all required properties and files are present and no production profile is modified.

- [ ] **Step 6: Commit the load-test runtime**

```bash
git add .gitignore src/main/resources/application-load-test.yml load-test/docker-compose.yml load-test/mysql load-test/scripts load-test/sql
git commit -m "test: add isolated mysql load test runtime"
```

### Task 6: Implement the External 5-10 Second Mock RAG Server

**Files:**
- Create: `load-test/mock-rag/server.mjs`
- Create: `load-test/mock-rag/server.test.mjs`
- Create: `load-test/mock-rag/package.json`

- [ ] **Step 1: Write failing Node tests**

Test pure functions for:

```text
same question -> same delay
every delay >= 5000 and <= 10000
response JSON matches LLMAnswerResponse
metrics calculate total, current, peak, min, max, average, p95, p99
reset clears counters
```

- [ ] **Step 2: Run tests and verify RED**

```bash
node --test load-test/mock-rag/server.test.mjs
```

Expected: FAIL because the server module is missing.

- [ ] **Step 3: Implement the dependency-free server**

Use Node's `http`, `URLSearchParams`, and a stable 32-bit hash. Provide:

```text
POST /maruegg/ask_question_api/
GET  /health
GET  /metrics
POST /reset
```

The POST handler increments in-flight count, waits the deterministic delay, returns JSON, and records completion in a `finally` block.

- [ ] **Step 4: Run Node tests GREEN**

Expected: all deterministic delay, contract, percentile, and reset tests pass.

- [ ] **Step 5: Commit the mock**

```bash
git add load-test/mock-rag
git commit -m "test: add deterministic delayed rag mock"
```

### Task 7: Implement the Deterministic 60/40 k6 Scenario

**Files:**
- Create: `load-test/k6/question-flow.js`
- Create: `load-test/k6/question-flow.test.mjs`
- Create: `load-test/k6/lib/workload.mjs`

- [ ] **Step 1: Write failing workload-selection tests**

For 10,000 deterministic iteration indexes, assert exactly 6,000 existing selections and 4,000 new selections. Assert new questions include run ID, VU, and iteration and never equal a seed question.

- [ ] **Step 2: Run tests and verify RED**

```bash
node --test load-test/k6/question-flow.test.mjs
```

Expected: FAIL because workload selection does not exist.

- [ ] **Step 3: Implement shared deterministic selection**

Use `iteration % 10 < 6` for existing-answer selection. Cycle through fixed seeded questions for the existing path and generate unique Korean new-question text for the miss path.

- [ ] **Step 4: Implement k6 stages and tagged metrics**

Add the approved stages and tags `scenario=existing` or `scenario=new`. Record selected, successful, and failed counters for both paths. Export a JSON summary through `handleSummary`.

- [ ] **Step 5: Run workload tests GREEN and k6 syntax validation**

```bash
node --test load-test/k6/question-flow.test.mjs
k6 inspect load-test/k6/question-flow.js
```

Expected: exact 60/40 selection and valid k6 configuration.

- [ ] **Step 6: Commit the k6 scenario**

```bash
git add load-test/k6
git commit -m "test: add mixed question k6 workload"
```

### Task 8: Collect HikariCP, Tomcat, JVM, MySQL, and Mock Metrics

**Files:**
- Create: `load-test/scripts/collect-metrics.sh`
- Create: `load-test/scripts/validate-metrics.sh`
- Create: `load-test/scripts/run-load-test.sh`
- Create: `load-test/scripts/summarize-results.mjs`
- Create: `load-test/scripts/summarize-results.test.mjs`

- [ ] **Step 1: Write failing metric-summary tests**

Given fixture samples, assert calculation of peak Hikari active and pending, connection timeout delta, peak MySQL connected/running, peak Tomcat busy threads, mock peak in-flight, and timestamps of each peak.

- [ ] **Step 2: Run summary tests RED**

```bash
node --test load-test/scripts/summarize-results.test.mjs
```

Expected: FAIL because the summarizer is missing.

- [ ] **Step 3: Implement metric sampling**

Every second collect Actuator Prometheus text, MySQL `SHOW GLOBAL STATUS`, selected `SHOW FULL PROCESSLIST` rows, mock `/metrics`, and application health. Store raw timestamped JSON Lines or text without discarding source values.

- [ ] **Step 4: Implement orchestration with process cleanup**

`run-load-test.sh` must:

```text
validate required commands
create a timestamped result directory
reset MySQL and seed data
reset mock counters
start application and wait for health
start metric sampler
run k6
stop sampler and application through traps
write environment and commit metadata
generate summary
```

- [ ] **Step 5: Implement and test summary calculations**

The summarizer must preserve missing metrics as `not exported`, not zero. Run tests GREEN.

- [ ] **Step 6: Add metric-contract validation**

Fail a run marked successful when Hikari active/idle/pending, Tomcat busy/max, MySQL connected/running, or mock delay/call metrics are absent. Treat optional acquisition histograms as `not exported` when Micrometer does not provide them.

- [ ] **Step 7: Commit metric collection**

```bash
git add load-test/scripts
git commit -m "test: collect connection and load metrics"
```

### Task 9: Run Baseline and Refactored Stress Tests

**Files:**
- Generate: `load-test/results/$RUN_ID-baseline/**`
- Generate: `load-test/results/$RUN_ID-refactored/**`
- Modify: `.gitignore`

- [ ] **Step 1: Run a short smoke test on both revisions**

Use 10 VU for at least 30 seconds with the full 60/40 logic. Confirm seeded hits avoid the mock and new questions call it exactly once.

- [ ] **Step 2: Run the baseline staged test**

Run against commit `c7e1268` with identical MySQL, Hikari, mock, and k6 settings. Record the first saturated resource and stop safely if the machine becomes unhealthy.

- [ ] **Step 3: Reset all mutable state**

Drop and recreate only the load-test database, reset mock counters, clear generated application logs, and use a new run ID while preserving the same deterministic selection and delay mapping.

- [ ] **Step 4: Run the refactored staged test**

Run the same stages to 3,000 VU. Do not call the run successful if Hikari acquisition timeout, MySQL max-connection rejection, missing required metrics, or process crash occurs.

- [ ] **Step 5: Validate raw evidence**

Confirm:

```text
route mix within 59-61% existing and 39-41% new
mock delay min >= 5s and max <= 10s
mock call count agrees with completed new-question requests
Hikari and MySQL samples cover the full test window
commit hashes and environment are present
```

### Task 10: Verify the Codebase and Write the Final Report

**Files:**
- Create: `docs/performance/transaction-boundary/final-report.md`
- Modify: `README.md`

- [ ] **Step 1: Run all focused tests**

```bash
./gradlew test --tests '*ProcessQuestionServiceTest*' --tests '*ProcessAnswerServiceTest*' --tests '*FindQuestionServiceTest*' --tests '*QuestionViewCountRepositoryTest*' --tests '*QuestionConnectionLifecycleIntegrationTest*' --tests '*RagAnswerServiceTest*' --tests '*CreateRAGAnswerServiceTest*'
node --test load-test/mock-rag/server.test.mjs load-test/k6/question-flow.test.mjs load-test/scripts/summarize-results.test.mjs
```

Expected: zero focused failures.

- [ ] **Step 2: Run the full Gradle suite**

Run with the confirmed Testcontainers environment.

Expected: report the exact total and failures. Any pre-existing failure must remain separated from new regressions; no new failure may be omitted.

- [ ] **Step 3: Verify the build and diff**

```bash
./gradlew clean build
git diff --check
git status --short
```

- [ ] **Step 4: Write the report from raw artifacts**

Include:

```text
executive conclusion
code-level transaction and connection lifecycle
baseline versus refactored table
Hikari active/idle/pending/timeouts and peak timestamps
MySQL connected/running/max-used and rejection counters
Tomcat busy/max threads
k6 route-specific throughput, error rate, and latency
mock delay and in-flight distribution
last successful stage and first saturated resource
test environment and reproducible commands
limitations: local 3,000 VU, blocking servlet threads, duplicate new-question race
```

- [ ] **Step 5: Link the report from README**

Add one documentation link without changing the existing project claims.

- [ ] **Step 6: Commit the report and documentation**

```bash
git add README.md docs/performance/transaction-boundary/final-report.md
git commit -m "docs: report transaction boundary load test"
```

- [ ] **Step 7: Run fresh final verification**

Repeat focused tests, full tests, build, Node tests, `k6 inspect`, metric validation, `git diff --check`, and inspect the final commit list before making any completion claim.
