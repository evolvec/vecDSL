/**
 * The server process as the extension starts it, outside VS Code: the java of JAVA_HOME (else the PATH) probed and
 * resolved to <java.home>/bin/java as in extension.ts, the arguments of serverArguments. Three runs: the client stops
 * the server with shutdown and exit (status 0); the client process is killed (the JVM must end by itself, since its
 * standard input closes); and the process the client spawned is the JVM itself, not a launcher (Linux: /proc/<pid>/exe).
 *
 *     node out-test/integration/killcheck.js <vecdsl-lsp.jar>
 */
import { execFile, spawn, type ChildProcess } from 'node:child_process';
import { readlinkSync, realpathSync } from 'node:fs';
import { PROBE_ARGUMENTS, probeCommand, runtimeOf, serverArguments, type JavaRuntime } from '../src/java';

const TIMEOUT_MS = 60_000;

function frame(message: object): string {
  const body = JSON.stringify({ jsonrpc: '2.0', ...message });
  return `Content-Length: ${String(Buffer.byteLength(body))}\r\n\r\n${body}`;
}

const INITIALIZE = frame({ id: 1, method: 'initialize', params: { processId: null, rootUri: null, capabilities: {} } });

/** A client in its own Node process: starts the server, initializes it, prints the server's pid, waits to be killed. */
const CLIENT = `
const { spawn } = require('node:child_process');
const [command, ...args] = process.argv.slice(1);
const server = spawn(command, args, { stdio: ['pipe', 'pipe', 'ignore'] });
server.stdout.once('data', () => { console.log(String(server.pid)); });
server.stdin.write(${JSON.stringify(INITIALIZE)});
setInterval(() => {}, 1000);
`;

function probe(): Promise<JavaRuntime> {
  const command = probeCommand('', process.env['JAVA_HOME'], process.platform);
  return new Promise((resolve, reject) => {
    execFile(command, PROBE_ARGUMENTS, { timeout: TIMEOUT_MS }, (_error, stdout, stderr) => {
      const runtime = runtimeOf(`${stderr}\n${stdout}`, process.platform);
      if (typeof runtime === 'string') {
        reject(new Error(`${command}: ${runtime}`));
      } else {
        resolve(runtime);
      }
    });
  });
}

function alive(pid: number): boolean {
  try {
    process.kill(pid, 0);
    return true;
  } catch {
    return false;
  }
}

async function until(condition: () => boolean, label: string): Promise<number> {
  const started = Date.now();
  while (!condition()) {
    if (Date.now() - started > TIMEOUT_MS) {
      throw new Error(`timed out: ${label}`);
    }
    await new Promise((resolve) => setTimeout(resolve, 100));
  }
  return (Date.now() - started) / 1000;
}

function exitStatus(process: ChildProcess): Promise<number | null> {
  return new Promise((resolve) => process.once('exit', (code) => { resolve(code); }));
}

async function main(): Promise<void> {
  const jar = process.argv[2];
  if (jar === undefined) {
    throw new Error('usage: killcheck.js <vecdsl-lsp.jar>');
  }
  const java = await probe();
  const args = serverArguments(java.major, jar, undefined);
  console.log(`java ${java.version}, home ${java.home}; command: ${[java.executable, ...args].join(' ')}`);

  const server = spawn(java.executable, args, { stdio: ['pipe', 'pipe', 'ignore'] });
  const exited = exitStatus(server);
  const answered = new Promise((resolve) => server.stdout.once('data', resolve));
  server.stdin.write(INITIALIZE);
  await answered;
  if (process.platform === 'linux' && server.pid !== undefined) {
    const exe = readlinkSync(`/proc/${String(server.pid)}/exe`);
    console.log(`spawned process ${String(server.pid)} is ${exe}: the JVM of java.home: ${String(
      realpathSync(exe) === realpathSync(java.executable))}`);
  }
  server.stdin.write(frame({ id: 2, method: 'shutdown', params: null }) + frame({ method: 'exit', params: null }));
  console.log(`shutdown and exit: exit status ${String(await exited)}`);

  const client = spawn(process.execPath, ['-e', CLIENT, java.executable, ...args], { stdio: ['ignore', 'pipe', 'ignore'] });
  const pid = Number(await new Promise<string>((resolve) => client.stdout.once('data', (data: Buffer) => {
    resolve(data.toString().trim());
  })));
  console.log(`client ${String(client.pid)} started server ${String(pid)}; killing the client`);
  client.kill('SIGKILL');
  const seconds = await until(() => !alive(pid), `server ${String(pid)} still running after the client was killed`);
  console.log(`server ${String(pid)} ended ${seconds.toFixed(1)} s after the client was killed: no JVM left`);
}

main().catch((error: unknown) => {
  console.error(String(error));
  process.exitCode = 1;
});
