const smokeStages = Object.freeze([
  { duration: '10s', target: 10 },
  { duration: '30s', target: 10 },
  { duration: '5s', target: 0 },
]);

const comparisonStages = Object.freeze([
  { duration: '10s', target: 100 },
  { duration: '90s', target: 100 },
  { duration: '10s', target: 0 },
]);

const fullStages = Object.freeze([
  { duration: '30s', target: 100 },
  { duration: '60s', target: 500 },
  { duration: '60s', target: 1_000 },
  { duration: '60s', target: 2_000 },
  { duration: '120s', target: 3_000 },
  { duration: '30s', target: 0 },
]);

export function scenarioFor({ smoke, loadProfile }) {
  if (loadProfile === 'vu-3000-minute') {
    return {
      executor: 'per-vu-iterations',
      vus: 3_000,
      iterations: 1,
      maxDuration: '180s',
      gracefulStop: '30s',
    };
  }

  if (loadProfile === 'minute-3000') {
    return {
      executor: 'constant-arrival-rate',
      rate: 50,
      timeUnit: '1s',
      duration: '60s',
      preAllocatedVUs: 200,
      maxVUs: 1_000,
      gracefulStop: '30s',
    };
  }

  const stages = smoke
    ? smokeStages
    : (loadProfile === 'comparison' ? comparisonStages : fullStages);

  return {
    executor: 'ramping-vus',
    startVUs: 0,
    stages,
    gracefulRampDown: '30s',
    gracefulStop: '30s',
  };
}

export function staggerDelaySeconds(vuId, totalVUs, windowSeconds) {
  return ((vuId - 1) * windowSeconds) / totalVUs;
}
