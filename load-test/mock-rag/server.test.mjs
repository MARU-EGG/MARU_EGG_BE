import assert from 'node:assert/strict';
import test from 'node:test';

import {
  MetricsStore,
  createRagResponse,
  deterministicDelayMs,
} from './server.mjs';

test('같은 질문은 항상 같은 5~10초 지연을 만든다', () => {
  const first = deterministicDelayMs('새 질문 1');
  const second = deterministicDelayMs('새 질문 1');

  assert.equal(first, second);
  assert.ok(first >= 5_000);
  assert.ok(first <= 10_000);

  for (let index = 0; index < 10_000; index += 1) {
    const delay = deterministicDelayMs(`질문-${index}`);
    assert.ok(delay >= 5_000 && delay <= 10_000);
  }
});

test('응답 JSON은 LLMAnswerResponse 계약을 따른다', () => {
  assert.deepEqual(createRagResponse({
    questionType: 'SUSI',
    questionCategory: 'ADMISSION_GUIDELINE',
    question: '새 질문',
  }), {
    questionType: 'SUSI',
    questionCategory: 'ADMISSION_GUIDELINE',
    answer: 'Mock RAG answer for: 새 질문',
    references: [],
  });
});

test('지표는 요청, 동시 실행, 지연 분위수를 계산하고 reset된다', () => {
  const metrics = new MetricsStore();
  metrics.started();
  metrics.started();
  metrics.completed(5_000, false);
  metrics.completed(7_000, false);
  metrics.started();
  metrics.completed(10_000, true);

  assert.deepEqual(metrics.snapshot(), {
    total: 3,
    completed: 3,
    errors: 1,
    currentInFlight: 0,
    peakInFlight: 2,
    delayMs: {
      min: 5_000,
      max: 10_000,
      average: 7_333.33,
      p95: 10_000,
      p99: 10_000,
    },
  });

  metrics.reset();
  assert.equal(metrics.snapshot().total, 0);
  assert.equal(metrics.snapshot().delayMs.min, null);
});
