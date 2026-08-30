import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

const resultDirectory = process.argv[2];
const appBaseUrl = process.env.APP_BASE_URL ?? 'http://127.0.0.1:8080';
const mockBaseUrl = process.env.MOCK_BASE_URL ?? 'http://127.0.0.1:18080';
const mysqlContainer = process.env.MYSQL_CONTAINER ?? 'maru-egg-load-test-mysql';
const intervalMs = Number(process.env.METRIC_INTERVAL_MS ?? 1_000);

if (!resultDirectory) {
  process.stderr.write('Usage: node collector.mjs RESULT_DIRECTORY\n');
  process.exit(2);
}

let running = true;
process.on('SIGTERM', () => { running = false; });
process.on('SIGINT', () => { running = false; });

for (const directory of ['prometheus', 'mysql-status', 'mysql-processlist', 'mock', 'health']) {
  fs.mkdirSync(path.join(resultDirectory, 'raw', directory), { recursive: true });
}

function prometheusValues(text, metricName) {
  const values = [];
  for (const line of text.split('\n')) {
    if (line.startsWith('#')) {
      continue;
    }
    const match = line.match(new RegExp(`^${metricName}(?:\\{[^}]*\\})?\\s+([^\\s]+)$`));
    if (match) {
      const value = Number(match[1]);
      if (!Number.isNaN(value)) {
        values.push(value);
      }
    }
  }
  return values;
}

function firstMetric(text, name) {
  return prometheusValues(text, name)[0] ?? null;
}

function sumMetric(text, name) {
  const values = prometheusValues(text, name);
  return values.length === 0 ? null : values.reduce((sum, value) => sum + value, 0);
}

async function fetchText(url) {
  try {
    const response = await fetch(url, { signal: AbortSignal.timeout(2_000) });
    return await response.text();
  } catch (error) {
    return JSON.stringify({ collectionError: error.message });
  }
}

async function fetchJson(url) {
  const text = await fetchText(url);
  try {
    return JSON.parse(text);
  } catch (_) {
    return { collectionError: text };
  }
}

function mysqlQuery(sql) {
  try {
    return execFileSync('docker', [
      'exec', mysqlContainer,
      'mysql', '-N', '-B', '-uloadtest', '-ploadtest',
      '-e', sql,
    ], { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] });
  } catch (error) {
    return `collection_error\t${String(error.stderr || error.message).trim()}`;
  }
}

function parseMysqlStatus(text) {
  const values = {};
  for (const line of text.split('\n')) {
    const [name, value] = line.split('\t');
    if (name && value !== undefined) {
      values[name] = Number(value);
    }
  }
  return {
    threadsConnected: values.Threads_connected ?? null,
    threadsRunning: values.Threads_running ?? null,
    maxUsedConnections: values.Max_used_connections ?? null,
    connections: values.Connections ?? null,
    abortedConnects: values.Aborted_connects ?? null,
    connectionErrorsMaxConnections: values.Connection_errors_max_connections ?? null,
  };
}

async function sample() {
  const timestamp = new Date().toISOString();
  const fileTimestamp = Date.now();
  const prometheus = await fetchText(`${appBaseUrl}/actuator/prometheus`);
  const health = await fetchJson(`${appBaseUrl}/actuator/health`);
  const mock = await fetchJson(`${mockBaseUrl}/metrics`);
  const mysqlStatus = mysqlQuery(
    "SHOW GLOBAL STATUS WHERE Variable_name IN ('Threads_connected','Threads_running','Max_used_connections','Connections','Aborted_connects','Connection_errors_max_connections')",
  );
  const processList = mysqlQuery(
    "SELECT ID,USER,HOST,DB,COMMAND,TIME,STATE,LEFT(COALESCE(INFO,''),200) FROM information_schema.PROCESSLIST WHERE DB='maru_egg_load_test'",
  );

  fs.writeFileSync(path.join(resultDirectory, 'raw', 'prometheus', `${fileTimestamp}.prom`), prometheus);
  fs.writeFileSync(path.join(resultDirectory, 'raw', 'mysql-status', `${fileTimestamp}.tsv`), mysqlStatus);
  fs.writeFileSync(path.join(resultDirectory, 'raw', 'mysql-processlist', `${fileTimestamp}.tsv`), processList);
  fs.writeFileSync(path.join(resultDirectory, 'raw', 'mock', `${fileTimestamp}.json`), `${JSON.stringify(mock)}\n`);
  fs.writeFileSync(path.join(resultDirectory, 'raw', 'health', `${fileTimestamp}.json`), `${JSON.stringify(health)}\n`);

  const parsed = {
    timestamp,
    app: {
      health: health.status ?? null,
      hikari: {
        active: firstMetric(prometheus, 'hikaricp_connections_active'),
        idle: firstMetric(prometheus, 'hikaricp_connections_idle'),
        pending: firstMetric(prometheus, 'hikaricp_connections_pending'),
        max: firstMetric(prometheus, 'hikaricp_connections_max'),
        min: firstMetric(prometheus, 'hikaricp_connections_min'),
        timeoutTotal: firstMetric(prometheus, 'hikaricp_connections_timeout_total'),
        acquireCount: firstMetric(prometheus, 'hikaricp_connections_acquire_seconds_count'),
        acquireSumSeconds: firstMetric(prometheus, 'hikaricp_connections_acquire_seconds_sum'),
      },
      tomcat: {
        busy: firstMetric(prometheus, 'tomcat_threads_busy_threads'),
        max: firstMetric(prometheus, 'tomcat_threads_config_max_threads'),
        current: firstMetric(prometheus, 'tomcat_threads_current_threads'),
      },
      jvm: {
        processCpuUsage: firstMetric(prometheus, 'process_cpu_usage'),
        systemCpuUsage: firstMetric(prometheus, 'system_cpu_usage'),
        memoryUsedBytes: sumMetric(prometheus, 'jvm_memory_used_bytes'),
        liveThreads: firstMetric(prometheus, 'jvm_threads_live_threads'),
        gcPauseCount: sumMetric(prometheus, 'jvm_gc_pause_seconds_count'),
        gcPauseSumSeconds: sumMetric(prometheus, 'jvm_gc_pause_seconds_sum'),
      },
    },
    mysql: parseMysqlStatus(mysqlStatus),
    mysqlProcessListFile: `raw/mysql-processlist/${fileTimestamp}.tsv`,
    mock,
  };
  fs.appendFileSync(path.join(resultDirectory, 'metrics.jsonl'), `${JSON.stringify(parsed)}\n`);
}

while (running) {
  const startedAt = Date.now();
  await sample();
  const remaining = intervalMs - (Date.now() - startedAt);
  if (running && remaining > 0) {
    await new Promise(resolve => setTimeout(resolve, remaining));
  }
}
