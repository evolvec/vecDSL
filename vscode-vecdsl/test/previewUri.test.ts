import * as assert from 'node:assert/strict';
import { test } from 'node:test';
import { previewKey, previewPath, type PreviewKey } from '../src/previewUri';

const WINDOWS: PreviewKey = {
  source: 'file:///c%3A/work/temp/vecdsl-vscode-check/pinecone_demo.vecDSL',
  index: 4, target: 'milvus', language: 'python',
};

void test('the path is /<source uri>/<statement index>/<target>/<language>, the source percent-encoded', () => {
  assert.equal(previewPath(WINDOWS),
    '/file%3A%2F%2F%2Fc%253A%2Fwork%2Ftemp%2Fvecdsl-vscode-check%2Fpinecone_demo.vecDSL'
    + '/4/milvus/python');
});

void test('round trip, also with spaces, non-ASCII and untitled documents', () => {
  for (const key of [WINDOWS,
    { source: 'file:///srv/my%20scripts/%C5%A1ema.vecDSL', index: 0, target: 'duckdb', language: 'java' },
    { source: 'untitled:Untitled-1', index: 12, target: 'pgvector', language: 'typescript' }] as const) {
    assert.deepEqual(previewKey(previewPath(key)), key);
  }
});

void test('other paths are not previews', () => {
  for (const path of ['', '/', '/x/1/milvus', '/x/one/milvus/python', '/x/1/milvus/rust', '/x/1/Milvus/python',
    'x/1/milvus/python', '/x/1/milvus/python/more', '//1/milvus/python']) {
    assert.equal(previewKey(path), undefined, path);
  }
});
