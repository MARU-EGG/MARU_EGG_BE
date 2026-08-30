import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

function count(metrics, name) {
  return metrics?.[name]?.values?.count ?? 0;
}

export function validateRun({ k6, metrics, existingVerification }) {
  const errors = [];
  const k6Metrics = k6?.metrics ?? {};
  const selectedExisting = count(k6Metrics, 'selected_existing');
  const selectedNew = count(k6Metrics, 'selected_new');
  const selectedTotal = selectedExisting + selectedNew;
  const successfulExisting = count(k6Metrics, 'successful_existing');
  const successfulNew = count(k6Metrics, 'successful_new');
  const failedExisting = count(k6Metrics, 'failed_existing');
  const failedNew = count(k6Metrics, 'failed_new');
  const existingRatio = selectedTotal === 0 ? 0 : selectedExisting / selectedTotal;
  const mock = metrics?.mock ?? {};

  if (selectedTotal === 0) errors.push('k6 selected no requests');
  if (existingRatio < 0.58 || existingRatio > 0.62) {
    errors.push(`existing request ratio out of range: ${existingRatio}`);
  }
  if (successfulExisting !== selectedExisting || successfulNew !== selectedNew) {
    errors.push('successful request counts do not match selected counts');
  }
  if (failedExisting !== 0 || failedNew !== 0) {
    errors.push(`failed requests exist: existing=${failedExisting}, new=${failedNew}`);
  }
  if (existingVerification?.total !== 0) {
    errors.push(`existing path invoked mock RAG: ${existingVerification?.total}`);
  }
  if (mock.finalTotal !== selectedNew) {
    errors.push(`mock total does not match selected new: ${mock.finalTotal} != ${selectedNew}`);
  }
  if (mock.finalCompleted !== mock.finalTotal) {
    errors.push(`mock completion count mismatch: ${mock.finalCompleted} != ${mock.finalTotal}`);
  }
  if (mock.finalErrors !== 0) errors.push(`mock errors exist: ${mock.finalErrors}`);
  if (mock.minDelayMs < 5000 || mock.maxDelayMs > 10000) {
    errors.push(`mock delay outside 5000..10000ms: ${mock.minDelayMs}..${mock.maxDelayMs}`);
  }

  return errors;
}

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

const isCli = process.argv[1]
  && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url);

if (isCli) {
  const resultDirectory = process.argv[2];
  if (!resultDirectory) {
    console.error('usage: node validate-run.mjs RESULT_DIRECTORY');
    process.exit(2);
  }
  const errors = validateRun({
    k6: readJson(path.join(resultDirectory, 'k6-summary.json')),
    metrics: readJson(path.join(resultDirectory, 'metrics-summary.json')),
    existingVerification: readJson(path.join(resultDirectory, 'existing-path-verification.json')),
  });
  if (errors.length > 0) {
    console.error(errors.join('\n'));
    process.exit(1);
  }
  console.log('run contract: valid');
}
