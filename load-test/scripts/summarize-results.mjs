import fs from 'node:fs';
import { pathToFileURL } from 'node:url';

const NOT_EXPORTED = 'not exported';

function peak(samples, selector) {
  let result = null;
  for (const sample of samples) {
    const value = selector(sample);
    if (value === null || value === undefined || Number.isNaN(value)) {
      continue;
    }
    if (result === null || value > result.value) {
      result = { value, timestamp: sample.timestamp };
    }
  }
  return result ?? NOT_EXPORTED;
}

function counterDelta(samples, selector) {
  const values = samples.map(selector).filter(value => value !== null && value !== undefined);
  if (values.length === 0) {
    return NOT_EXPORTED;
  }
  return Math.max(0, values.at(-1) - values[0]);
}

export function summarizeSamples(samples) {
  return {
    sampleCount: samples.length,
    firstTimestamp: samples[0]?.timestamp ?? null,
    lastTimestamp: samples.at(-1)?.timestamp ?? null,
    hikari: {
      peakActive: peak(samples, sample => sample.app?.hikari?.active),
      peakIdle: peak(samples, sample => sample.app?.hikari?.idle),
      peakPending: peak(samples, sample => sample.app?.hikari?.pending),
      configuredMax: samples.find(sample => sample.app?.hikari?.max != null)?.app.hikari.max ?? NOT_EXPORTED,
      configuredMin: samples.find(sample => sample.app?.hikari?.min != null)?.app.hikari.min ?? NOT_EXPORTED,
      connectionTimeoutDelta: counterDelta(samples, sample => sample.app?.hikari?.timeoutTotal),
    },
    tomcat: {
      peakBusy: peak(samples, sample => sample.app?.tomcat?.busy),
      configuredMax: samples.find(sample => sample.app?.tomcat?.max != null)?.app.tomcat.max ?? NOT_EXPORTED,
    },
    jvm: {
      peakProcessCpuUsage: peak(samples, sample => sample.app?.jvm?.processCpuUsage),
      peakSystemCpuUsage: peak(samples, sample => sample.app?.jvm?.systemCpuUsage),
      peakMemoryUsedBytes: peak(samples, sample => sample.app?.jvm?.memoryUsedBytes),
      peakLiveThreads: peak(samples, sample => sample.app?.jvm?.liveThreads),
      gcPauseCountDelta: counterDelta(samples, sample => sample.app?.jvm?.gcPauseCount),
      gcPauseSecondsDelta: counterDelta(samples, sample => sample.app?.jvm?.gcPauseSumSeconds),
    },
    mysql: {
      peakThreadsConnected: peak(samples, sample => sample.mysql?.threadsConnected),
      peakThreadsRunning: peak(samples, sample => sample.mysql?.threadsRunning),
      peakMaxUsedConnections: peak(samples, sample => sample.mysql?.maxUsedConnections),
      connectionErrorsMaxConnectionsDelta: counterDelta(
        samples,
        sample => sample.mysql?.connectionErrorsMaxConnections,
      ),
    },
    mock: {
      peakInFlight: peak(samples, sample => sample.mock?.peakInFlight),
      finalTotal: samples.at(-1)?.mock?.total ?? NOT_EXPORTED,
      finalCompleted: samples.at(-1)?.mock?.completed ?? NOT_EXPORTED,
      finalErrors: samples.at(-1)?.mock?.errors ?? NOT_EXPORTED,
      minDelayMs: samples.at(-1)?.mock?.delayMs?.min ?? NOT_EXPORTED,
      maxDelayMs: samples.at(-1)?.mock?.delayMs?.max ?? NOT_EXPORTED,
      p95DelayMs: samples.at(-1)?.mock?.delayMs?.p95 ?? NOT_EXPORTED,
      p99DelayMs: samples.at(-1)?.mock?.delayMs?.p99 ?? NOT_EXPORTED,
    },
  };
}

function loadJsonLines(filePath) {
  return fs.readFileSync(filePath, 'utf8')
    .split('\n')
    .filter(Boolean)
    .map(line => JSON.parse(line));
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const resultDirectory = process.argv[2];
  if (!resultDirectory) {
    process.stderr.write('Usage: node summarize-results.mjs RESULT_DIRECTORY\n');
    process.exit(2);
  }
  const samples = loadJsonLines(`${resultDirectory}/metrics.jsonl`);
  const summary = summarizeSamples(samples);
  fs.writeFileSync(`${resultDirectory}/metrics-summary.json`, `${JSON.stringify(summary, null, 2)}\n`);
  process.stdout.write(`${JSON.stringify(summary, null, 2)}\n`);
}
