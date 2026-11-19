import * as assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import * as path from 'node:path';
import { before, test } from 'node:test';
import * as oniguruma from 'vscode-oniguruma';
import * as textmate from 'vscode-textmate';

const ROOT = path.join(__dirname, '..', '..');
const SAMPLE = path.join(ROOT, '..', 'org.ftn.vecDSL.tests', 'samples', 'pinecone_demo.vecDSL');
let grammar: textmate.IGrammar;

before(async () => {
  const wasm = readFileSync(require.resolve('vscode-oniguruma/release/onig.wasm'));
  await oniguruma.loadWASM(wasm.buffer.slice(wasm.byteOffset, wasm.byteOffset + wasm.byteLength));
  const registry = new textmate.Registry({
    onigLib: Promise.resolve({
      createOnigScanner: (patterns) => new oniguruma.OnigScanner(patterns),
      createOnigString: (text) => new oniguruma.OnigString(text),
    }),
    loadGrammar: (scope) => Promise.resolve(scope === 'source.vecdsl' ? textmate.parseRawGrammar(
      readFileSync(path.join(ROOT, 'syntaxes', 'vecdsl.tmLanguage.json'), 'utf8'), 'vecdsl.tmLanguage.json') : null),
  });
  const loaded = await registry.loadGrammar('source.vecdsl');
  assert.ok(loaded);
  grammar = loaded;
});

/** Text of each token with its innermost scope (source.vecdsl when it has none), over the lines in order. */
function tokens(text: string): [string, string][] {
  let state = textmate.INITIAL;
  const result: [string, string][] = [];
  for (const line of text.split(/\r?\n/)) {
    const tokenized = grammar.tokenizeLine(line, state);
    for (const token of tokenized.tokens) {
      result.push([line.substring(token.startIndex, token.endIndex), token.scopes.at(-1) ?? '']);
    }
    state = tokenized.ruleStack;
  }
  return result;
}

function scopeOf(text: string, token: string): string | undefined {
  return tokens(text).find(([value]) => value.trim() === token.trim())?.[1];
}

void test('pinecone_demo.vecDSL: keywords, vendor ids, parameters, env(...), strings, numbers', () => {
  const sample = readFileSync(SAMPLE, 'utf8');
  const all = tokens(sample);
  const scope = (token: string): string | undefined => all.find(([value]) => value.trim() === token)?.[1];
  assert.equal(scope('script'), 'keyword.other.vecdsl');
  assert.equal(scope('vector-search'), 'keyword.other.vecdsl');
  assert.equal(scope('if-not-exists'), 'keyword.other.vecdsl');
  assert.equal(scope('pinecone'), 'support.constant.vendor.vecdsl');
  assert.equal(scope('$queryVector'), 'variable.parameter.vecdsl');
  assert.equal(scope('env'), 'support.function.env.vecdsl');
  assert.equal(scope('VECDSL_PINECONE_API_KEY'), 'string.quoted.double.vecdsl');
  assert.equal(scope('0.9'), 'constant.numeric.vecdsl');
  assert.equal(scope('productCatalog'), 'source.vecdsl');
});

void test('comments, escaped names, single quotes and literals', () => {
  assert.equal(scopeOf('// a note', '// a note'), 'comment.line.double-slash.vecdsl');
  assert.equal(scopeOf('/* first\nsecond */ count', 'second '), 'comment.block.vecdsl');
  assert.equal(scopeOf('field ^title type varchar', '^title'), 'variable.other.escaped.vecdsl');
  assert.equal(scopeOf("description 'it''s'", 'description'), 'keyword.other.vecdsl');
  assert.equal(scopeOf('x: count in c where flag eq true', 'true'), 'constant.language.vecdsl');
  assert.equal(scopeOf('top 3', '3'), 'constant.numeric.vecdsl');
  assert.equal(scopeOf('vector-searches', 'vector-searches'), 'source.vecdsl');
});

void test('every keyword of the grammar is highlighted as a whole word', () => {
  const raw = JSON.parse(readFileSync(path.join(ROOT, 'syntaxes', 'vecdsl.tmLanguage.json'), 'utf8')) as {
    repository: Record<string, { match?: string; name?: string }>;
  };
  for (const rule of ['keywords', 'vendors', 'literals']) {
    const entry = raw.repository[rule];
    const words = /\(\?:(.*)\)\(\?!/.exec(entry?.match ?? '')?.[1]?.split('|').map((word) => word.replace(/\\(.)/g, '$1'));
    assert.ok(words && words.length > 0, rule);
    for (const word of words) {
      assert.deepEqual(tokens(word), [[word, entry?.name]], word);
    }
  }
});
