/**
 * The VS Code test of suite.ts, twice: a window with a copy of org.ftn.vecDSL.tests/samples as its folder (the five
 * manual checks of the editor and the preview), and a window with one copied file and no folder. VS Code is
 * the installed one when VSCODE_EXECUTABLE names its executable (Windows: ...\Microsoft VS Code\Code.exe; no other VS
 * Code window may be open, VS Code refuses a second instance for tests), otherwise the one @vscode/test-electron
 * downloads into .vscode-test (version VSCODE_VERSION, default the minimum engine 1.91.0). The extension runs from this
 * folder, so server/vecdsl-lsp.jar must exist (npm run server).
 *
 *     npm run test:integration
 */
import { copyFileSync, cpSync, mkdtempSync } from 'node:fs';
import { tmpdir } from 'node:os';
import * as path from 'node:path';
import { runTests } from '@vscode/test-electron';

const root = path.join(__dirname, '..', '..');
const source = path.join(root, '..', 'org.ftn.vecDSL.tests', 'samples');
const executable = process.env['VSCODE_EXECUTABLE'];
const vscode = executable === undefined || executable === ''
  ? { version: process.env['VSCODE_VERSION'] ?? '1.91.0' }
  : { vscodeExecutablePath: executable };

async function main(): Promise<void> {
  const samples = mkdtempSync(path.join(tmpdir(), 'vecdsl-vscode-it-'));
  cpSync(source, samples, { recursive: true });
  const single = path.join(mkdtempSync(path.join(tmpdir(), 'vecdsl-vscode-it-file-')), 'chroma_demo.vecDSL');
  copyFileSync(path.join(source, 'chroma_demo.vecDSL'), single);
  for (const [mode, opened] of [['folder', samples], ['file', single]] as const) {
    console.log(`window with ${mode === 'folder' ? `the folder ${samples}` : `the file ${single} and no folder`}`);
    await runTests({
      ...vscode,
      extensionDevelopmentPath: root,
      extensionTestsPath: path.join(__dirname, 'suite.js'),
      extensionTestsEnv: { VECDSL_IT_WINDOW: mode },
      launchArgs: [opened, '--disable-extensions', '--disable-workspace-trust'],
    });
  }
}

main().catch((error: unknown) => {
  console.error(String(error));
  process.exitCode = 1;
});
