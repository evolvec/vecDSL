import * as assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  executableIn, majorOf, probeCommand, runtimeOf, serverArguments, serverExecutable, UNSAFE_OPTION,
} from '../src/java';

/** `java -XshowSettings:properties -version` of Oracle Java 25.0.2 on Windows, shortened, as it writes to stderr. */
const ORACLE_25 = [
  'Property settings:',
  '    file.encoding = UTF-8',
  '    java.class.version = 69.0',
  '    java.home = C:\\Program Files\\Java\\jdk-25',
  '    java.specification.version = 25',
  '    java.version = 25.0.2',
  '    user.dir = D:\\work',
  '',
  'java version "25.0.2" 2026-01-20 LTS',
  'Java(TM) SE Runtime Environment (build 25.0.2+10-LTS-69)',
  'Java HotSpot(TM) 64-Bit Server VM (build 25.0.2+10-LTS-69, mixed mode, sharing)',
].join('\r\n');

const OPENJDK_21 = [
  'Picked up JAVA_TOOL_OPTIONS: -Dhttps.proxyHost=127.0.0.1',
  'Property settings:',
  '    java.home = /usr/lib/jvm/java-21-openjdk-amd64',
  '    java.version = 21.0.12.1',
  '',
  'openjdk version "21.0.12.1" 2026-08-18',
].join('\n');

void test('java.home and the version come from the probe output, CRLF or LF', () => {
  assert.deepEqual(runtimeOf(ORACLE_25, 'win32'), {
    home: 'C:\\Program Files\\Java\\jdk-25', version: '25.0.2', major: 25,
    executable: 'C:\\Program Files\\Java\\jdk-25\\bin\\java.exe',
  });
  assert.deepEqual(runtimeOf(OPENJDK_21, 'linux'), {
    home: '/usr/lib/jvm/java-21-openjdk-amd64', version: '21.0.12.1', major: 21,
    executable: '/usr/lib/jvm/java-21-openjdk-amd64/bin/java',
  });
});

void test('a Java older than 21 or an output without java.home is refused with the reason', () => {
  const reason = (output: string): string => {
    const runtime = runtimeOf(output, 'win32');
    return typeof runtime === 'string' ? runtime : `a runtime at ${runtime.home}`;
  };
  assert.match(reason('    java.home = C:\\jdk8\\jre\njava version "1.8.0_402"'), /needs Java 21/);
  assert.match(reason('java version "25.0.2"'), /no java\.home/);
  assert.match(reason(''), /no java\.home/);
});

void test('major versions', () => {
  assert.deepEqual(['25.0.2', '21.0.12.1', '1.8.0_402', '26-ea', '24', '17.0.9+9'].map(majorOf), [25, 21, 8, 26, 24, 17]);
});

void test('the java to probe: the setting, else JAVA_HOME, else the PATH', () => {
  assert.equal(probeCommand('D:\\jdk-21', 'C:\\Program Files\\Java\\jdk-25', 'win32'), 'D:\\jdk-21\\bin\\java.exe');
  assert.equal(probeCommand('  ', 'C:\\Program Files\\Java\\jdk-25', 'win32'),
    'C:\\Program Files\\Java\\jdk-25\\bin\\java.exe');
  assert.equal(probeCommand('', undefined, 'win32'), 'java');
  assert.equal(probeCommand('', '', 'linux'), 'java');
  assert.equal(executableIn('/opt/jdk', 'darwin'), '/opt/jdk/bin/java');
});

void test('the Unsafe option from Java 24 on, before -jar; --log only with a file', () => {
  assert.deepEqual(serverArguments(21, 'C:\\x\\vecdsl-lsp.jar', undefined), ['-jar', 'C:\\x\\vecdsl-lsp.jar']);
  assert.deepEqual(serverArguments(24, 'a.jar', undefined), [UNSAFE_OPTION, '-jar', 'a.jar']);
  assert.deepEqual(serverArguments(25, 'a.jar', 'C:\\t\\lsp.log'), [UNSAFE_OPTION, '-jar', 'a.jar', '--log', 'C:\\t\\lsp.log']);
});

void test('the executable has exactly the server arguments and no transport (no --stdio is added)', () => {
  const java = { home: 'C:\\jdk', version: '25.0.2', major: 25, executable: 'C:\\jdk\\bin\\java.exe' };
  const executable = serverExecutable(java, serverArguments(25, 'a.jar', undefined));
  assert.deepEqual(executable, { command: 'C:\\jdk\\bin\\java.exe', args: [UNSAFE_OPTION, '-jar', 'a.jar'] });
  assert.equal('transport' in executable, false);
});
