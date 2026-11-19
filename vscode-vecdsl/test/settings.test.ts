import * as assert from 'node:assert/strict';
import * as path from 'node:path';
import { test } from 'node:test';
import { serverSettings } from '../src/settings';

const EXTENSION = path.resolve('/ext/vecdsl.vscode-vecdsl-0.1.0');
const WORKSPACE = path.resolve('/work/vecdsl-vscode-check');

void test('empty settings: the bundled jar, no log', () => {
  assert.deepEqual(serverSettings({ javaHome: ' ', jar: '', log: '' }, EXTENSION, WORKSPACE), {
    javaHome: '', jar: path.join(EXTENSION, 'server', 'vecdsl-lsp.jar'), log: undefined,
  });
});

void test('given settings are kept; a relative log is taken from the workspace, else from the extension', () => {
  const jar = path.resolve('/work/vecdsl-lsp/target/vecdsl-lsp.jar');
  assert.deepEqual(serverSettings({ javaHome: ' /opt/jdk-25 ', jar, log: 'lsp.log' }, EXTENSION, WORKSPACE), {
    javaHome: '/opt/jdk-25', jar, log: path.join(WORKSPACE, 'lsp.log'),
  });
  assert.equal(serverSettings({ javaHome: '', jar: '', log: 'lsp.log' }, EXTENSION, undefined).log,
    path.join(EXTENSION, 'lsp.log'));
  const absolute = path.resolve('/tmp/vecdsl/lsp.log');
  assert.equal(serverSettings({ javaHome: '', jar: '', log: absolute }, EXTENSION, WORKSPACE).log, absolute);
});
