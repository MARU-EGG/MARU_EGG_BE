# Minute 3,000 Capacity Tuning Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a reproducible 50 RPS for 60 seconds load profile and find the smallest Tomcat/Hikari setting that processes all 3,000 mixed requests without drops, failures, or connection timeouts.

**Architecture:** Keep the application flow unchanged and extend only the load-test harness. A Node-testable profile module defines the constant-arrival scenario, while run validation enforces the exact scheduled request count and dropped-iteration contract. Execute one configuration change at a time and stop at the first passing setting.

**Tech Stack:** k6, Node.js test runner, Bash, Spring Boot Actuator, HikariCP, Tomcat, MySQL 8.

## Global Constraints

- The minute profile schedules exactly 50 iterations per second for 60 seconds.
- Existing/new request mix remains 60/40 and Mock RAG delay remains 5,000–10,000ms.
- A valid minute run requires 3,000 selected requests, zero dropped iterations, zero request failures, zero Hikari timeout, and zero MySQL max-connection rejection.
- Do not describe this profile as 3,000 concurrent VU.
- Change one capacity variable at a time and retain every raw result directory.

---

### Task 1: Add the minute-3,000 workload profile

**Files:**
- Create: `load-test/k6/lib/profiles.mjs`
- Modify: `load-test/k6/question-flow.js`
- Modify: `load-test/k6/question-flow.test.mjs`

**Interfaces:**
- Produces: `scenarioFor({ smoke, loadProfile })` returning the k6 scenario configuration.
- Consumes: `LOAD_PROFILE=minute-3000` from the existing runner.

- [ ] **Step 1: Write failing Node tests**

Assert that `minute-3000` uses `constant-arrival-rate`, rate 50, time unit 1 second, duration 60 seconds, 200 preallocated VUs, and 1,000 maximum VUs. Also preserve the existing smoke, comparison, and full profiles.

- [ ] **Step 2: Run the test and verify RED**

Run: `node --test load-test/k6/question-flow.test.mjs`

Expected: FAIL because `lib/profiles.mjs` does not exist.

- [ ] **Step 3: Implement the profile module and wire it into k6**

Move the existing stage arrays into `profiles.mjs`, add the minute profile, and replace the inline scenario selection in `question-flow.js` with `scenarioFor`.

- [ ] **Step 4: Run tests and k6 inspection GREEN**

Run: `node --test load-test/k6/question-flow.test.mjs`

Run: `LOAD_PROFILE=minute-3000 k6 inspect load-test/k6/question-flow.js`

Expected: all Node tests pass and k6 reports a valid constant-arrival-rate scenario.

### Task 2: Enforce the exact minute-run contract

**Files:**
- Modify: `load-test/scripts/validate-run.mjs`
- Modify: `load-test/scripts/validate-run.test.mjs`
- Modify: `load-test/scripts/run-load-test.sh`

**Interfaces:**
- Consumes: `load_profile` from `environment.txt` and k6 `dropped_iterations`.
- Produces: validation errors when the minute profile selects anything other than 3,000 requests or drops an iteration.

- [ ] **Step 1: Write failing validation tests**

Add positive coverage for 3,000 selected requests and zero drops, then negative coverage for 2,999 selected requests and one dropped iteration under `minute-3000`.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `node --test load-test/scripts/validate-run.test.mjs`

Expected: FAIL because profile-aware exact-count validation is absent.

- [ ] **Step 3: Implement profile-aware validation**

Parse `environment.txt` in the CLI path, pass `loadProfile` into `validateRun`, and require exactly 3,000 selections plus zero dropped iterations for `minute-3000`.

- [ ] **Step 4: Run all harness tests GREEN**

Run: `node --test load-test/mock-rag/server.test.mjs load-test/k6/question-flow.test.mjs load-test/scripts/summarize-results.test.mjs load-test/scripts/validate-run.test.mjs`

Expected: all tests pass.

### Task 3: Execute the capacity ladder and report the minimum stable setting

**Files:**
- Create: `docs/performance/transaction-boundary/minute-3000-capacity-report.md`
- Preserve: `load-test/results/20260830-minute3000-*`

**Interfaces:**
- Consumes: raw k6, Actuator, MySQL, Mock, and environment outputs from each run.
- Produces: a comparison table and one selected minimum stable setting, or an evidence-backed asynchronous alternative.

- [ ] **Step 1: Run 200/20**

Run with `LOAD_PROFILE=minute-3000`, `LOAD_TEST_TOMCAT_MAX_THREADS=200`, `LOAD_TEST_HIKARI_MAX_POOL_SIZE=20`, and `LOAD_TEST_HIKARI_MIN_IDLE=20`.

- [ ] **Step 2: Classify failure before changing a setting**

Use Tomcat busy/max, Hikari pending/timeout, MySQL running/error, JVM CPU/memory/live-thread, route p95, and dropped-iteration metrics. Change only the resource proven saturated.

- [ ] **Step 3: Continue the approved ladder only as needed**

Try 250/20 and 300/20 only for Tomcat saturation. Try 300/30 and 300/40 only for Hikari saturation. Stop at the first configuration satisfying every success criterion.

- [ ] **Step 4: Verify the selected run**

Run the metric validator, run-contract validator, Node harness tests, `git diff --check`, and inspect the selected raw summaries.

- [ ] **Step 5: Write the report**

Record every attempted configuration, why the next setting was selected, all mandatory metrics, the selected minimum stable configuration, test limitations, and resume-safe wording. If all configurations fail, document why parameter tuning is exhausted and specify the asynchronous queue design.

