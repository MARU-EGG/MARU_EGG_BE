import http from 'node:http';
import { pathToFileURL } from 'node:url';

const MIN_DELAY_MS = 5_000;
const MAX_DELAY_MS = 10_000;

export function deterministicDelayMs(value, min = MIN_DELAY_MS, max = MAX_DELAY_MS) {
  let hash = 0x811c9dc5;
  for (const character of value) {
    hash ^= character.codePointAt(0);
    hash = Math.imul(hash, 0x01000193);
  }
  const span = max - min + 1;
  return min + ((hash >>> 0) % span);
}

export function createRagResponse({ questionType, questionCategory, question }) {
  return {
    questionType,
    questionCategory,
    answer: `Mock RAG answer for: ${question}`,
    references: [],
  };
}

export class MetricsStore {
  constructor() {
    this.reset();
  }

  started() {
    this.total += 1;
    this.currentInFlight += 1;
    this.peakInFlight = Math.max(this.peakInFlight, this.currentInFlight);
  }

  completed(delayMs, failed) {
    this.completedCount += 1;
    this.currentInFlight = Math.max(0, this.currentInFlight - 1);
    this.errors += failed ? 1 : 0;
    this.delays.push(delayMs);
  }

  reset() {
    this.total = 0;
    this.completedCount = 0;
    this.errors = 0;
    this.currentInFlight = 0;
    this.peakInFlight = 0;
    this.delays = [];
  }

  snapshot() {
    const sorted = [...this.delays].sort((left, right) => left - right);
    const sum = sorted.reduce((total, delay) => total + delay, 0);
    return {
      total: this.total,
      completed: this.completedCount,
      errors: this.errors,
      currentInFlight: this.currentInFlight,
      peakInFlight: this.peakInFlight,
      delayMs: {
        min: sorted.length === 0 ? null : sorted[0],
        max: sorted.length === 0 ? null : sorted.at(-1),
        average: sorted.length === 0 ? null : Number((sum / sorted.length).toFixed(2)),
        p95: percentile(sorted, 0.95),
        p99: percentile(sorted, 0.99),
      },
    };
  }
}

function percentile(sortedValues, percentileValue) {
  if (sortedValues.length === 0) {
    return null;
  }
  const index = Math.max(0, Math.ceil(sortedValues.length * percentileValue) - 1);
  return sortedValues[index];
}

function readRequestBody(request) {
  return new Promise((resolve, reject) => {
    let body = '';
    request.setEncoding('utf8');
    request.on('data', chunk => {
      body += chunk;
      if (body.length > 64 * 1024) {
        reject(new Error('request body too large'));
        request.destroy();
      }
    });
    request.on('end', () => resolve(body));
    request.on('error', reject);
  });
}

function sendJson(response, statusCode, value) {
  response.writeHead(statusCode, { 'content-type': 'application/json; charset=utf-8' });
  response.end(JSON.stringify(value));
}

export function createServer(metrics = new MetricsStore()) {
  return http.createServer(async (request, response) => {
    if (request.method === 'GET' && request.url === '/health') {
      sendJson(response, 200, { status: 'UP' });
      return;
    }
    if (request.method === 'GET' && request.url === '/metrics') {
      sendJson(response, 200, metrics.snapshot());
      return;
    }
    if (request.method === 'POST' && request.url === '/reset') {
      metrics.reset();
      sendJson(response, 200, metrics.snapshot());
      return;
    }
    if (request.method !== 'POST' || request.url !== '/maruegg/ask_question_api/') {
      sendJson(response, 404, { error: 'not found' });
      return;
    }

    let delayMs = 0;
    let failed = false;
    metrics.started();
    try {
      const form = new URLSearchParams(await readRequestBody(request));
      const payload = {
        questionType: form.get('questionType') ?? '',
        questionCategory: form.get('questionCategory') ?? '',
        question: form.get('question') ?? '',
      };
      delayMs = deterministicDelayMs(payload.question);
      await new Promise(resolve => setTimeout(resolve, delayMs));
      sendJson(response, 200, createRagResponse(payload));
    } catch (error) {
      failed = true;
      if (!response.headersSent) {
        sendJson(response, 500, { error: error.message });
      } else {
        response.destroy(error);
      }
    } finally {
      metrics.completed(delayMs, failed);
    }
  });
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const port = Number(process.env.MOCK_RAG_PORT ?? 18_080);
  const server = createServer();
  server.listen(port, '127.0.0.1', () => {
    process.stdout.write(`mock RAG listening on http://127.0.0.1:${port}\n`);
  });
}
