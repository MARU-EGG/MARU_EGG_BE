# Transaction Boundary and Load Test Design

## Objective

Refactor the question processing flow so that a database connection is not held while the RAG HTTP request waits for 5 to 10 seconds. Verify the behavior with repeatable unit, integration, and staged load tests in which 60% of requests reuse an existing answer and 40% call an external mock RAG service with a new question.

The final stage reaches 3,000 virtual users. The report must separate observed results from design expectations and must include connection-pool and MySQL connection metrics.

## Current Problem

`ProcessQuestionService` opens a class-level transaction, queries MySQL, and then calls `ProcessAnswerService`. `RagAnswerService` returns a cold `Mono`, so its `NOT_SUPPORTED` proxy boundary ends before `block()` subscribes and starts the HTTP request. The outer transaction therefore remains active while the application waits for RAG. `CreateRAGAnswerService` then opens a `REQUIRES_NEW` transaction, which can require another connection while the outer transaction is still suspended.

OSIV remains disabled. Its responsibility is to prevent a request-scoped persistence context and web-layer lazy loading; it is not used as evidence that the RAG wait releases a connection.

## Chosen Architecture

Use a non-transactional orchestration service with explicit `TransactionTemplate` blocks.

1. A read-only transaction loads candidate question projections.
2. The transaction commits and returns its connection.
3. Cosine similarity is calculated outside a transaction.
4. An existing-answer hit is loaded and its view count is incremented in a short write transaction.
5. A miss calls the external RAG service outside any database transaction.
6. A valid RAG response is stored in a short write transaction.

`TransactionTemplate` is chosen because the read, remote I/O, and write phases occur sequentially in one orchestration method. The code must show the exact commit boundaries without depending on self-invocation through Spring proxies.

## Transaction and Connection Flow

```text
Request
  -> read TransactionTemplate
       -> first SQL borrows connection C1
       -> candidate DTO query
       -> commit
       -> C1 returned to HikariCP
  -> cosine similarity outside transaction
  -> existing hit (60%)
       -> write TransactionTemplate
       -> atomic view-count update and response DTO load
       -> commit and return connection
  -> new question (40%)
       -> external mock RAG call, 5-10 seconds, no DB transaction
       -> write TransactionTemplate
       -> save question, answer, and references
       -> commit and return connection
```

The orchestration service, answer-processing service, and RAG client must not open an enclosing transaction. The answer writer must join the write template rather than create `REQUIRES_NEW`.

## Existing-Answer Path

The candidate query returns `QuestionCoreResponse` projections, not managed entities. Similarity computation occurs after the read transaction commits.

The current `FindQuestionService` increments `viewCount` while declared read-only. This is corrected by an atomic SQL update:

```sql
update questions
set view_count = view_count + 1
where id = ?
```

The answer, references, and response DTO are loaded inside the same short write transaction so OSIV-disabled serialization cannot trigger lazy loading.

## New-Question Path

The mock RAG service is a separate process. It accepts the same form-encoded request as the real service and returns the same JSON structure. Its delay is deterministically mapped from the question text into the inclusive range from 5,000 to 10,000 milliseconds. Deterministic delay keeps baseline and refactored runs comparable while still exercising the full latency range.

Each new-question request contains a run identifier, VU identifier, and iteration identifier. This prevents a question created early in a run from becoming an existing-answer hit later in that run.

The RAG `Mono` is subscribed with `block()` outside both transaction templates. The 20-second application timeout remains larger than the mock delay.

## Duplicate and Failure Semantics

The transaction-boundary change does not by itself prevent simultaneous identical new questions from being stored twice. The load test avoids accidental reuse with unique request contents. The final report records duplicate-miss coalescing or a database fingerprint constraint as a separate improvement, not as a completed part of this change.

If RAG succeeds and the write transaction fails, the HTTP request follows the existing failure behavior. The report must not claim an atomic boundary across MySQL and the external RAG system.

`block()` still occupies a servlet request thread. The change targets database connection occupancy, not servlet-thread occupancy. Tomcat active/busy threads and queued work are collected so a 3,000-VU failure is not misclassified as a database-pool failure.

## Test Harness

### External Mock

A standalone mock process listens on a dedicated local port and exposes `/maruegg/ask_question_api/`.

It records:

- total calls
- minimum, maximum, average, and percentile delay
- current and peak in-flight requests
- response count and errors

It exposes a lightweight metrics endpoint or writes a machine-readable summary used by the final report.

### Database

A dedicated MySQL load-test database is recreated before each baseline and refactored run. Seed data contains a fixed set of existing questions, answers, and references. The schema includes the Full-Text index required by the production query.

Baseline and refactored runs use identical data, HikariCP configuration, MySQL configuration, mock delay mapping, and k6 input seed.

### k6 Mix

Every iteration selects its route deterministically:

- 60%: exact content from the seeded existing-question set
- 40%: unique new content that cannot match the seeded candidates

Custom counters record selected route, returned status, and unexpected RAG behavior. The accepted mix is 60/40 within one percentage point over the complete run.

### Stages

```text
100 VU    for 30 seconds
500 VU    for 60 seconds
1,000 VU  for 60 seconds
2,000 VU  for 60 seconds
3,000 VU  for 120 seconds
ramp down for 30 seconds
```

The 3,000-VU run is a stress test on one local application instance, one local MySQL instance, and one local mock process. It is not presented as production capacity.

## Required Metrics

### Application and HikariCP

- `hikaricp_connections_active`
- `hikaricp_connections_idle`
- `hikaricp_connections_pending`
- `hikaricp_connections_max`
- `hikaricp_connections_min`
- connection acquisition duration and timeout count when exported by the current Micrometer version
- HTTP request count, status, duration, and error rate
- JVM heap, CPU, GC pause, and live threads
- Tomcat current, busy, and maximum threads

### MySQL

- `Threads_connected`
- `Threads_running`
- `Max_used_connections`
- `Connections`
- `Aborted_connects`
- `Connection_errors_max_connections`
- process-list samples for long-running or waiting statements

### Mock RAG

- total calls and call ratio
- delay minimum, maximum, average, p95, and p99
- current and peak in-flight calls
- error count

### k6

- request count and requests per second
- `http_req_duration` average, p50, p95, and p99 by existing/new scenario
- `http_req_waiting`
- `http_req_failed`
- iterations completed
- VU peak
- selected and completed 60/40 route ratios

## Direct Connection-Release Verification

In addition to load-test graphs, an integration test uses a deliberately blocked fake RAG response and a small HikariCP pool.

The test waits until the remote call is in flight and then asserts:

1. the initial read transaction has completed;
2. the application thread is waiting for RAG outside a transaction;
3. the read connection is no longer active in HikariCP;
4. after releasing the mock response, the write transaction borrows a connection and returns it after commit.

This test is the primary proof of the connection lifecycle. Load-test metrics are system-level corroboration.

## Comparison Method

Run the same scenario twice:

1. baseline commit before the transaction-boundary change;
2. refactored branch after all correctness tests pass.

Reset the database and mock counters between runs. Store raw k6 summaries, sampled Actuator metrics, MySQL status samples, mock metrics, application logs, environment information, and commit hashes under a timestamped results directory that is excluded from production packaging.

## Acceptance Criteria

- Unit tests prove read, remote, and write phases execute in the intended order.
- A red-green integration test proves the HikariCP connection is returned during the blocked RAG call.
- Existing-answer responses still load references correctly with OSIV disabled.
- View-count updates do not lose increments under concurrent requests.
- The mock delay remains between 5 and 10 seconds.
- The complete load-test mix is within one percentage point of 60/40.
- Baseline and refactored runs use identical inputs and configuration.
- No HikariCP acquisition timeout or MySQL max-connection rejection occurs in a run described as successful.
- If 3,000 VU fails, the report identifies the first saturated resource and reports the last successful stage instead of presenting the run as successful.
- The final report includes raw values, peak timestamps, test conditions, limitations, and reproducible commands.

## Deliverables

- TransactionTemplate configuration and refactored question flow
- corrected view-count update
- unit and integration tests
- separate mock RAG server
- deterministic seed/reset tooling
- k6 60/40 staged scenario
- metric sampler for Actuator and MySQL
- baseline and refactored raw result directories
- final Markdown report with comparison tables and conclusions

