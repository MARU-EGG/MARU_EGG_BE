import assert from 'node:assert/strict';
import test from 'node:test';

import { validateRun } from './validate-run.mjs';

function metric(count) {
  return { values: { count } };
}

test('60:40 요청, 성공 수, 신규 Mock 호출 수를 함께 검증한다', () => {
  const errors = validateRun({
    k6: {
      metrics: {
        selected_existing: metric(600),
        selected_new: metric(400),
        successful_existing: metric(600),
        successful_new: metric(400),
        failed_existing: metric(0),
        failed_new: metric(0),
      },
    },
    metrics: {
      mock: {
        finalTotal: 400,
        finalCompleted: 400,
        finalErrors: 0,
        minDelayMs: 5000,
        maxDelayMs: 10000,
      },
    },
    existingVerification: { total: 0 },
  });

  assert.deepEqual(errors, []);
});

test('기존 경로 Mock 호출, 비율 이탈, 신규 호출 불일치를 모두 실패시킨다', () => {
  const errors = validateRun({
    k6: {
      metrics: {
        selected_existing: metric(500),
        selected_new: metric(500),
        successful_existing: metric(499),
        successful_new: metric(500),
        failed_existing: metric(1),
        failed_new: metric(0),
      },
    },
    metrics: {
      mock: {
        finalTotal: 499,
        finalCompleted: 498,
        finalErrors: 1,
        minDelayMs: 4999,
        maxDelayMs: 10001,
      },
    },
    existingVerification: { total: 1 },
  });

  assert.ok(errors.length >= 7);
});

test('1분 3,000건 프로필은 정확한 요청 수와 dropped iteration 0건을 검증한다', () => {
  const errors = validateRun({
    loadProfile: 'minute-3000',
    k6: {
      metrics: {
        selected_existing: metric(1_800),
        selected_new: metric(1_200),
        successful_existing: metric(1_800),
        successful_new: metric(1_200),
        failed_existing: metric(0),
        failed_new: metric(0),
        dropped_iterations: metric(0),
      },
    },
    metrics: {
      mock: {
        finalTotal: 1_200,
        finalCompleted: 1_200,
        finalErrors: 0,
        minDelayMs: 5000,
        maxDelayMs: 10000,
      },
    },
    existingVerification: { total: 0 },
  });

  assert.deepEqual(errors, []);
});

test('1분 3,000건 프로필의 요청 누락과 dropped iteration을 실패시킨다', () => {
  const errors = validateRun({
    loadProfile: 'minute-3000',
    k6: {
      metrics: {
        selected_existing: metric(1_799),
        selected_new: metric(1_200),
        successful_existing: metric(1_799),
        successful_new: metric(1_200),
        failed_existing: metric(0),
        failed_new: metric(0),
        dropped_iterations: metric(1),
      },
    },
    metrics: {
      mock: {
        finalTotal: 1_200,
        finalCompleted: 1_200,
        finalErrors: 0,
        minDelayMs: 5000,
        maxDelayMs: 10000,
      },
    },
    existingVerification: { total: 0 },
  });

  assert.ok(errors.includes('minute-3000 selected request count mismatch: 2999 != 3000'));
  assert.ok(errors.includes('minute-3000 dropped iterations exist: 1'));
});
