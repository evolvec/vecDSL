import * as assert from 'node:assert/strict';
import { test } from 'node:test';
import { generateCapability } from '../src/protocol';

void test('version 2 with targets, version 1, no request at all', () => {
  assert.deepEqual(generateCapability({ vecdsl: { generate: 'vecdsl/generate', generateVersion: 2,
    targets: ['milvus', 'duckdb'] } }), { version: 2, targets: ['milvus', 'duckdb'] });
  assert.deepEqual(generateCapability({ vecdsl: { generate: 'vecdsl/generate' } }), { version: 1, targets: [] });
  assert.deepEqual(generateCapability(undefined), { version: 0, targets: [] });
  assert.deepEqual(generateCapability({ vecdsl: { generate: 'other' } }), { version: 0, targets: [] });
});
