import assert from 'node:assert/strict';
import test from 'node:test';

import { scenarioFor, staggerDelaySeconds } from './lib/profiles.mjs';
import { EXISTING_QUESTIONS, selectWorkload } from './lib/workload.mjs';

test('1분 3,000건 프로필은 초당 50건을 60초 동안 예약한다', () => {
  assert.deepEqual(scenarioFor({ smoke: false, loadProfile: 'minute-3000' }), {
    executor: 'constant-arrival-rate',
    rate: 50,
    timeUnit: '1s',
    duration: '60s',
    preAllocatedVUs: 200,
    maxVUs: 1_000,
    gracefulStop: '30s',
  });
});

test('기존 smoke, comparison, full 프로필의 단계를 보존한다', () => {
  assert.equal(scenarioFor({ smoke: true, loadProfile: 'full' }).stages.at(-2).target, 10);
  assert.equal(scenarioFor({ smoke: false, loadProfile: 'comparison' }).stages.at(-2).target, 100);
  assert.equal(scenarioFor({ smoke: false, loadProfile: 'full' }).stages.at(-2).target, 3_000);
});

test('3,000 VU 프로필은 각 VU가 한 번씩 180초 안에 실행된다', () => {
  assert.deepEqual(scenarioFor({ smoke: false, loadProfile: 'vu-3000-minute' }), {
    executor: 'per-vu-iterations',
    vus: 3_000,
    iterations: 1,
    maxDuration: '180s',
    gracefulStop: '30s',
  });
});

test('3,000 VU 요청 시작은 60초 동안 균등 분산된다', () => {
  assert.equal(staggerDelaySeconds(1, 3_000, 60), 0);
  assert.equal(staggerDelaySeconds(1_501, 3_000, 60), 30);
  assert.equal(staggerDelaySeconds(3_000, 3_000, 60), 59.98);
});

test('10,000개 선택은 정확히 기존 60%, 신규 40%다', () => {
  const counts = { existing: 0, new: 0 };
  for (let iteration = 0; iteration < 10_000; iteration += 1) {
    counts[selectWorkload({ runId: 'test', vu: 1, iteration }).kind] += 1;
  }
  assert.deepEqual(counts, { existing: 6_000, new: 4_000 });
});

test('신규 질문은 실행, VU, 반복 식별자를 포함하고 seed와 겹치지 않는다', () => {
  const selected = selectWorkload({ runId: 'run-42', vu: 17, iteration: 9 });

  assert.equal(selected.kind, 'new');
  assert.match(selected.request.content, /run-42/);
  assert.match(selected.request.content, /vu-17/);
  assert.match(selected.request.content, /iter-9/);
  assert.ok(!EXISTING_QUESTIONS.some(question => question.content === selected.request.content));
});

test('기존 경로는 고정 seed 질문만 순환한다', () => {
  for (let iteration = 0; iteration < 6; iteration += 1) {
    const selected = selectWorkload({ runId: 'test', vu: 1, iteration });
    assert.equal(selected.kind, 'existing');
    assert.deepEqual(selected.request, EXISTING_QUESTIONS[iteration]);
  }
});
