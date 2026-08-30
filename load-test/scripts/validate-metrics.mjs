import fs from 'node:fs';

const resultDirectory = process.argv[2];
if (!resultDirectory) {
  process.stderr.write('Usage: node validate-metrics.mjs RESULT_DIRECTORY\n');
  process.exit(2);
}

const metricsFile = `${resultDirectory}/metrics.jsonl`;
if (!fs.existsSync(metricsFile) || fs.statSync(metricsFile).size === 0) {
  throw new Error('metrics.jsonl is missing or empty');
}

const samples = fs.readFileSync(metricsFile, 'utf8').split('\n').filter(Boolean).map(JSON.parse);
const required = [
  ['Hikari active', sample => sample.app?.hikari?.active],
  ['Hikari idle', sample => sample.app?.hikari?.idle],
  ['Hikari pending', sample => sample.app?.hikari?.pending],
  ['Hikari max', sample => sample.app?.hikari?.max],
  ['Hikari min', sample => sample.app?.hikari?.min],
  ['Tomcat busy', sample => sample.app?.tomcat?.busy],
  ['Tomcat max', sample => sample.app?.tomcat?.max],
  ['MySQL connected', sample => sample.mysql?.threadsConnected],
  ['MySQL running', sample => sample.mysql?.threadsRunning],
  ['Mock total', sample => sample.mock?.total],
  ['Mock delay min', sample => sample.mock?.delayMs?.min],
  ['Mock delay max', sample => sample.mock?.delayMs?.max],
];

const missing = required
  .filter(([, selector]) => !samples.some(sample => selector(sample) !== null && selector(sample) !== undefined))
  .map(([name]) => name);

if (missing.length > 0) {
  throw new Error(`required metrics not exported: ${missing.join(', ')}`);
}

const finalMock = samples.at(-1).mock;
if (finalMock.delayMs.min < 5_000 || finalMock.delayMs.max > 10_000) {
  throw new Error(`mock delay outside 5-10 seconds: ${JSON.stringify(finalMock.delayMs)}`);
}

process.stdout.write(`metrics contract: valid (${samples.length} samples)\n`);
