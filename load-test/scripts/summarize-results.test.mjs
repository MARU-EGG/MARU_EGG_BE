import assert from 'node:assert/strict';
import test from 'node:test';

import { summarizeSamples } from './summarize-results.mjs';

test('커넥션, 스레드, mock 지표의 peak와 발생 시각을 계산한다', () => {
  const samples = [
    {
      timestamp: '2026-08-30T00:00:00.000Z',
      app: {
        hikari: { active: 2, idle: 18, pending: 0, max: 20, min: 20, timeoutTotal: 3 },
        tomcat: { busy: 10, max: 200 },
      },
      mysql: { threadsConnected: 22, threadsRunning: 3, maxUsedConnections: 25, connectionErrorsMaxConnections: 1 },
      mock: { currentInFlight: 8, peakInFlight: 8, total: 10, completed: 2, errors: 0, delayMs: { min: 5_000, max: 8_000 } },
    },
    {
      timestamp: '2026-08-30T00:00:01.000Z',
      app: {
        hikari: { active: 20, idle: 0, pending: 12, max: 20, min: 20, timeoutTotal: 7 },
        tomcat: { busy: 190, max: 200 },
      },
      mysql: { threadsConnected: 40, threadsRunning: 14, maxUsedConnections: 42, connectionErrorsMaxConnections: 2 },
      mock: { currentInFlight: 120, peakInFlight: 120, total: 150, completed: 30, errors: 1, delayMs: { min: 5_000, max: 10_000 } },
    },
  ];

  const summary = summarizeSamples(samples);

  assert.deepEqual(summary.hikari.peakActive, { value: 20, timestamp: '2026-08-30T00:00:01.000Z' });
  assert.deepEqual(summary.hikari.peakPending, { value: 12, timestamp: '2026-08-30T00:00:01.000Z' });
  assert.equal(summary.hikari.connectionTimeoutDelta, 4);
  assert.equal(summary.mysql.peakThreadsConnected.value, 40);
  assert.equal(summary.mysql.peakThreadsRunning.value, 14);
  assert.equal(summary.tomcat.peakBusy.value, 190);
  assert.equal(summary.mock.peakInFlight.value, 120);
});

test('없는 선택 지표는 0이 아니라 not exported로 보존한다', () => {
  const summary = summarizeSamples([{
    timestamp: '2026-08-30T00:00:00.000Z',
    app: { hikari: { active: 0, idle: 20, pending: 0, max: 20, min: 20, timeoutTotal: null }, tomcat: { busy: 0, max: 200 } },
    mysql: { threadsConnected: 1, threadsRunning: 1, maxUsedConnections: 1, connectionErrorsMaxConnections: 0 },
    mock: { currentInFlight: 0, peakInFlight: 0, total: 0, completed: 0, errors: 0, delayMs: { min: null, max: null } },
  }]);

  assert.equal(summary.hikari.connectionTimeoutDelta, 'not exported');
});
