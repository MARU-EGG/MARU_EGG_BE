import http from 'k6/http';
import { check } from 'k6';
import execution from 'k6/execution';
import { Counter, Trend } from 'k6/metrics';

import { selectWorkload } from './lib/workload.mjs';

const selectedExisting = new Counter('selected_existing');
const selectedNew = new Counter('selected_new');
const successfulExisting = new Counter('successful_existing');
const successfulNew = new Counter('successful_new');
const failedExisting = new Counter('failed_existing');
const failedNew = new Counter('failed_new');
const existingDuration = new Trend('existing_duration', true);
const newDuration = new Trend('new_duration', true);

const smokeStages = [
  { duration: '10s', target: 10 },
  { duration: '30s', target: 10 },
  { duration: '5s', target: 0 },
];

const comparisonStages = [
  { duration: '10s', target: 100 },
  { duration: '90s', target: 100 },
  { duration: '10s', target: 0 },
];

const fullStages = [
  { duration: '30s', target: 100 },
  { duration: '60s', target: 500 },
  { duration: '60s', target: 1_000 },
  { duration: '60s', target: 2_000 },
  { duration: '120s', target: 3_000 },
  { duration: '30s', target: 0 },
];

export const options = {
  scenarios: {
    mixed_questions: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: __ENV.SMOKE === 'true'
        ? smokeStages
        : (__ENV.LOAD_PROFILE === 'comparison' ? comparisonStages : fullStages),
      gracefulRampDown: '30s',
      gracefulStop: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
  },
  discardResponseBodies: false,
};

export default function () {
  const runId = __ENV.RUN_ID || 'manual';
  const selection = selectWorkload({
    runId,
    vu: execution.vu.idInTest,
    iteration: execution.scenario.iterationInTest,
  });
  const isExisting = selection.kind === 'existing';
  const tags = { scenario: selection.kind };
  const requestOptions = {
    headers: { 'Content-Type': 'application/json' },
    tags,
    timeout: __ENV.REQUEST_TIMEOUT || '30s',
  };

  (isExisting ? selectedExisting : selectedNew).add(1, tags);
  const response = http.post(
    `${__ENV.BASE_URL || 'http://127.0.0.1:8080'}/api/questions`,
    JSON.stringify(selection.request),
    requestOptions,
  );
  (isExisting ? existingDuration : newDuration).add(response.timings.duration, tags);

  const succeeded = check(response, {
    [`${selection.kind}: status is 200`]: current => current.status === 200,
    [`${selection.kind}: answer exists`]: current => {
      try {
        return Boolean(current.json('answer.content'));
      } catch (_) {
        return false;
      }
    },
  }, tags);

  if (succeeded) {
    (isExisting ? successfulExisting : successfulNew).add(1, tags);
  } else {
    (isExisting ? failedExisting : failedNew).add(1, tags);
  }
}

export function handleSummary(data) {
  const output = JSON.stringify(data, null, 2);
  const resultFile = `${__ENV.RESULT_DIR || 'load-test/results/manual'}/k6-summary.json`;
  return {
    stdout: `${output}\n`,
    [resultFile]: output,
  };
}
