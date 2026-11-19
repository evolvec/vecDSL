import * as assert from 'node:assert/strict';
import * as vscode from 'vscode';

const TIMEOUT_MS = 120_000;

async function until<T>(read: () => T, done: (value: T) => boolean, label: string): Promise<T> {
  const started = Date.now();
  for (let value = read(); ; value = read()) {
    if (done(value)) {
      return value;
    }
    if (Date.now() - started > TIMEOUT_MS) {
      throw new Error(`timed out: ${label}`);
    }
    await new Promise((resolve) => setTimeout(resolve, 250));
  }
}

/** Asks again until the answer is done, as until() does for values read without a command. */
async function poll<T>(ask: () => Thenable<T>, done: (value: T) => boolean, label: string): Promise<T> {
  const started = Date.now();
  for (;;) {
    const value = await ask();
    if (done(value)) {
      return value;
    }
    if (Date.now() - started > TIMEOUT_MS) {
      throw new Error(`timed out: ${label}`);
    }
    await new Promise((resolve) => setTimeout(resolve, 250));
  }
}

function markdown(hover: vscode.Hover): string {
  return hover.contents.map((part) => (part instanceof vscode.MarkdownString ? part.value : JSON.stringify(part))).join('\n');
}

/** Run by @vscode/test-electron inside VS Code (run.ts): the window with a folder, or the one with a single file. */
export async function run(): Promise<void> {
  if (process.env['VECDSL_IT_WINDOW'] === 'file') {
    await singleFile();
  } else {
    await folderWindow();
  }
}

/** The samples' copy as the workspace folder: the five checks, the preview by command and from a code lens. */
async function folderWindow(): Promise<void> {
  const folder = vscode.workspace.workspaceFolders?.[0]?.uri;
  assert.ok(folder);
  const uri = vscode.Uri.joinPath(folder, 'pinecone_demo.vecDSL');
  const document = await vscode.workspace.openTextDocument(uri);
  const editor = await vscode.window.showTextDocument(document);
  const original = document.getText();
  const unsupported = (diagnostics: vscode.Diagnostic[]): vscode.Diagnostic[] => diagnostics.filter((diagnostic) =>
    diagnostic.code === 'vecdsl.coverage.unsupported' && diagnostic.severity === vscode.DiagnosticSeverity.Warning);

  const warnings = await until(() => unsupported(vscode.languages.getDiagnostics(uri)), (found) => found.length === 4,
    '4 warnings on pinecone_demo.vecDSL');
  assert.deepEqual(warnings.map((warning) => warning.range.start.line + 1), [15, 21, 24, 26]);
  console.log('1. markers: 4 warnings on lines 15, 21, 24, 26');

  const hovers = await vscode.commands.executeCommand<vscode.Hover[]>('vscode.executeHoverProvider', uri,
    new vscode.Position(35, 9));
  const text = hovers.map(markdown).join('\n');
  assert.ok(text.includes('**vector-search**') && text.includes('| pinecone | native |'), text);
  console.log('2. hover: vector-search with the pinecone row');

  const completion = await vscode.commands.executeCommand<vscode.CompletionList>(
    'vscode.executeCompletionItemProvider', uri, new vscode.Position(36, 4));
  const detail = (label: string): string | undefined => completion.items.find((item) => item.label === label)?.detail;
  assert.equal(completion.items.length, 27);
  assert.equal(detail('score'), 'pinecone: concealed');
  assert.equal(detail('sort'), 'unsupported on pinecone: Pinecone orders search matches by score');
  console.log('3. completion: 27 items, score "pinecone: concealed"');

  const title = "Remove 'pinecone' from targets";
  const first = warnings[0];
  assert.ok(first);
  await editor.edit((edit) => { edit.replace(document.lineAt(1).range, 'targets [ pinecone, milvus ]'); });
  const actions = await poll(() => vscode.commands.executeCommand<vscode.CodeAction[]>(
    'vscode.executeCodeActionProvider', uri, first.range), (found) => found.some((action) => action.title === title),
  `the quick fix "${title}" after adding milvus`);
  const vecdsl = actions.filter((action) => action.title.startsWith('Remove '));
  assert.deepEqual(vecdsl.map((action) => action.title), [title]);
  const fix = vecdsl[0]?.edit;
  assert.ok(fix);
  await vscode.workspace.applyEdit(fix);
  assert.equal(document.lineAt(1).text, 'targets [ milvus ]');
  await editor.edit((edit) => { edit.replace(new vscode.Range(0, 0, document.lineCount, 0), original); });
  console.log("4. quick fix: Remove 'pinecone' from targets");

  const options = { tabSize: 4, insertSpaces: true };
  const edits = await vscode.commands.executeCommand<vscode.TextEdit[]>('vscode.executeFormatDocumentProvider', uri,
    options);
  const formatting = new vscode.WorkspaceEdit();
  formatting.set(uri, edits);
  await vscode.workspace.applyEdit(formatting);
  assert.equal(document.lineCount - (document.getText().endsWith('\n') ? 1 : 0), 77);
  const again = await vscode.commands.executeCommand<vscode.TextEdit[] | undefined>(
    'vscode.executeFormatDocumentProvider', uri, options);
  assert.equal(again?.length ?? 0, 0);
  console.log('5. format: 60 lines become 77; formatting again changes nothing');

  await vscode.commands.executeCommand('vecdsl.previewGeneratedCode', uri.toString(), 4, 'pinecone', 'python');
  const preview = await until(() => vscode.workspace.textDocuments.find((open) => open.uri.scheme === 'vecdsl-preview'),
    (open) => open !== undefined && open.getText().includes('def similar('), 'preview of similar');
  assert.equal(preview?.languageId, 'python');
  console.log('6. preview: similar on pinecone, Python');

  const lenses = await poll(() => vscode.commands.executeCommand<vscode.CodeLens[]>('vscode.executeCodeLensProvider',
    uri), (found) => found.length === 14, '14 code lenses');
  const lens = lenses[2]?.command;
  assert.ok(lens !== undefined);
  assert.equal(lens.title, 'Preview: pinecone python');
  await vscode.commands.executeCommand(lens.command, ...(lens.arguments ?? []) as unknown[]);
  await until(() => vscode.workspace.textDocuments.find((open) => open.uri.scheme === 'vecdsl-preview'
    && open.getText().includes('def create_products(')), (open) => open !== undefined, 'preview of createProducts');
  console.log('7. code lens: 14 lenses "Preview: pinecone python"; the third opens createProducts');
}

/** One file opened without a folder: the server starts without a root and still serves the file. */
async function singleFile(): Promise<void> {
  assert.equal(vscode.workspace.workspaceFolders, undefined);
  const editor = await until(() => vscode.window.activeTextEditor, (active) => active !== undefined,
    'the opened file in an editor');
  const uri = editor?.document.uri;
  assert.ok(uri !== undefined && uri.path.endsWith('/chroma_demo.vecDSL'));
  const warnings = await until(() => vscode.languages.getDiagnostics(uri), (found) => found.length === 1,
    'the warning of chroma_demo.vecDSL');
  const [warning] = warnings;
  assert.ok(warning !== undefined);
  assert.equal(warning.range.start.line, 18);
  assert.equal(warning.code, 'vecdsl.coverage.unsupported');
  console.log('8. no folder: chroma_demo.vecDSL gets its warning on line 19');
}
