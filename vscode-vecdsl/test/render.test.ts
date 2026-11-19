import * as assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import * as path from 'node:path';
import { test } from 'node:test';
import type { PreviewKey } from '../src/previewUri';
import type { GenerateResult } from '../src/protocol';
import { render } from '../src/render';

/** Results of vecdsl/generate on pinecone_demo.vecDSL, recorded from vecdsl-lsp (files reduced to those with sections). */
function fixture(name: string): GenerateResult {
  return JSON.parse(readFileSync(path.join(__dirname, '..', '..', 'test', 'fixtures', name), 'utf8')) as GenerateResult;
}

const OUTLINE = fixture('outline.json');
const SOURCE = 'file:///c%3A/check/pinecone_demo.vecDSL';

function key(index: number, target: string, language: PreviewKey['language']): PreviewKey {
  return { source: SOURCE, index, target, language };
}

void test('a statement: header, then each section with its file and lines', () => {
  const text = render({ key: key(4, 'pinecone', 'python'), sourceName: 'pinecone_demo.vecDSL', outline: OUTLINE,
    result: fixture('similar-pinecone-py.json') }).split('\n');
  assert.equal(text[0], "# vecDSL preview of statement 'similar' (5 of 14, line 36) in pinecone_demo.vecDSL");
  assert.equal(text[1], '# target pinecone, Python: the code vecdsl gen writes, from vecdsl-lsp');
  assert.equal(text[3], '# pinecone/python/product_catalog.py, lines 110 to 121');
  assert.equal(text[4], '    def similar(self, query_vector, max_price):');
  assert.equal(text[15], '        )');
  assert.equal(text[17], '# pinecone/python/product_catalog.py, lines 254 to 254');
  assert.match(text[18] ?? '', /"similar": program\.similar\(/);
});

void test('unsupported constructs come first, with the warnings\' text, in TypeScript comments', () => {
  const text = render({ key: key(2, 'pinecone', 'typescript'), sourceName: 'pinecone_demo.vecDSL', outline: OUTLINE,
    result: fixture('create-pinecone-ts.json') });
  const lines = text.split('\n');
  assert.equal(lines[2], '// Unsupported on pinecone (the editor\'s warnings):');
  assert.match(lines[3] ?? '', /^\/\/ {3}line 15: unsupported on pinecone: Pinecone configures text analysis/);
  assert.equal(lines.filter((line) => line.startsWith('//   line ')).length, 4);
  assert.match(text, /async createProducts\(\) \{/);
});

void test('an unlabeled statement is named by its position', () => {
  const outline: GenerateResult = { ...OUTLINE, statements: OUTLINE.statements.map(({ range }) => ({ range })) };
  assert.match(render({ key: key(4, 'pinecone', 'python'), sourceName: 'pinecone_demo.vecDSL', outline,
    result: fixture('similar-pinecone-py.json') }), /^# vecDSL preview of the unlabeled statement \(5 of 14, line 36\)/);
});

void test('a target outside the program\'s targets, a program with errors, a statement that is gone', () => {
  assert.match(render({ key: key(4, 'milvus', 'python'), sourceName: 'pinecone_demo.vecDSL', outline: OUTLINE,
    result: fixture('similar-milvus-py.json') }), /# milvus is not in the program's targets \[pinecone\], so vecdsl gen writes nothing for it\./);
  const errors = render({ key: key(4, 'pinecone', 'java'), sourceName: 'pinecone_demo.vecDSL', outline: OUTLINE,
    result: fixture('errors-pinecone-py.json') });
  assert.match(errors, /\/\/ The program has errors, so vecdsl gen writes nothing:\n\/\/ {3}36:27: Couldn't resolve reference/);
  assert.match(render({ key: key(20, 'pinecone', 'python'), sourceName: 'pinecone_demo.vecDSL', outline: OUTLINE,
    result: undefined }), /has 14 statements; statement 21 no longer exists/);
});
