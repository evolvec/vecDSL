import { execFile } from 'node:child_process';
import { existsSync } from 'node:fs';
import * as vscode from 'vscode';
import { LanguageClient } from 'vscode-languageclient/node';
import {
  PROBE_ARGUMENTS, probeCommand, runtimeOf, serverArguments, serverExecutable, type JavaRuntime,
} from './java';
import { Preview } from './preview';
import { SCHEME } from './previewUri';
import { serverSettings, type ServerSettings } from './settings';

const PROBE_TIMEOUT_MS = 30_000;

let client: LanguageClient | undefined;

export async function activate(context: vscode.ExtensionContext): Promise<void> {
  const output = vscode.window.createOutputChannel('vecDSL Language Server', { log: true });
  const preview = new Preview(() => client);
  context.subscriptions.push(output, preview,
    vscode.workspace.registerTextDocumentContentProvider(SCHEME, preview),
    vscode.languages.registerCodeLensProvider({ language: 'vecdsl' }, preview),
    vscode.commands.registerCommand('vecdsl.previewGeneratedCode', preview.command.bind(preview)),
    vscode.commands.registerCommand('vecdsl.restartServer', async () => {
      await stop();
      await start(context, output, preview);
    }),
    vscode.commands.registerCommand('vecdsl.showServerLog', () => showLog(context, output)));
  await start(context, output, preview);
}

export function deactivate(): Promise<void> {
  return stop();
}

function settings(context: vscode.ExtensionContext): ServerSettings {
  const configuration = vscode.workspace.getConfiguration('vecdsl');
  return serverSettings({
    javaHome: configuration.get<string>('java.home', ''),
    jar: configuration.get<string>('lsp.jar', ''),
    log: configuration.get<string>('lsp.log', ''),
  }, context.extensionPath, vscode.workspace.workspaceFolders?.[0]?.uri.fsPath);
}

/**
 * Resolves the Java runtime and starts the server with stdio: the JVM's own executable under java.home, not a
 * launcher found on the PATH, so that stopping the client ends the JVM.
 */
async function start(context: vscode.ExtensionContext, output: vscode.LogOutputChannel, preview: Preview): Promise<void> {
  const resolved = settings(context);
  const java = await probe(probeCommand(resolved.javaHome, process.env['JAVA_HOME'], process.platform));
  if (typeof java === 'string') {
    output.appendLine(java);
    void vscode.window.showErrorMessage(`vecDSL: ${java} Set vecdsl.java.home and run "vecDSL: Restart Language Server".`);
    return;
  }
  if (!existsSync(resolved.jar)) {
    const message = `vecDSL: the language server jar ${resolved.jar} does not exist; check vecdsl.lsp.jar.`;
    output.appendLine(message);
    void vscode.window.showErrorMessage(message);
    return;
  }
  const args = serverArguments(java.major, resolved.jar, resolved.log);
  output.appendLine(`Java ${java.version} at ${java.home}`);
  output.appendLine(`Starting: ${[java.executable, ...args].map((part) => (part.includes(' ') ? `"${part}"` : part))
    .join(' ')}`);
  client = new LanguageClient('vecdsl', 'vecDSL Language Server',
    serverExecutable(java, args),
    {
      documentSelector: [{ scheme: 'file', language: 'vecdsl' }],
      outputChannel: output,
    });
  try {
    await client.start();
    output.appendLine(`Server process ${String(client.serverProcess?.pid)} started.`);
  } catch (error) {
    output.appendLine(`The server did not start: ${String(error)}`);
  }
  preview.refresh();
}

async function stop(): Promise<void> {
  const running = client;
  client = undefined;
  if (running !== undefined) {
    await running.dispose();
  }
}

/** The runtime of the java to probe, or a sentence saying why it cannot run the server. */
function probe(command: string): Promise<JavaRuntime | string> {
  return new Promise((resolve) => {
    execFile(command, PROBE_ARGUMENTS, { timeout: PROBE_TIMEOUT_MS, windowsHide: true }, (error, stdout, stderr) => {
      const runtime = runtimeOf(`${stderr}\n${stdout}`, process.platform);
      if (typeof runtime !== 'string') {
        resolve(runtime);
      } else {
        resolve(`Java at "${command}" cannot run the server: ${error === null ? runtime : error.message}.`);
      }
    });
  });
}

async function showLog(context: vscode.ExtensionContext, output: vscode.LogOutputChannel): Promise<void> {
  const log = settings(context).log;
  if (log === undefined) {
    output.show(true);
  } else if (existsSync(log)) {
    await vscode.window.showTextDocument(vscode.Uri.file(log), { preview: true });
  } else {
    void vscode.window.showInformationMessage(`vecDSL: the log file ${log} does not exist yet.`);
  }
}
