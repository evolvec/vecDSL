import * as path from 'node:path';
import * as vscode from 'vscode';
import type { LanguageClient } from 'vscode-languageclient/node';
import { previewKey, previewPath, SCHEME, type PreviewKey } from './previewUri';
import {
  GENERATE, GENERATE_VERSION, generateCapability, isLanguage, LANGUAGES, type GenerateParams, type GenerateResult,
  type Language,
} from './protocol';
import { render } from './render';

const DEBOUNCE_MS = 400;

/**
 * The generated-code preview: a virtual document per statement, target and language (scheme vecdsl-preview), its code
 * lenses, and the command that opens it. Content comes from vecdsl/generate; a change of the source refreshes the
 * open previews of that source after a pause in typing.
 */
export class Preview implements vscode.TextDocumentContentProvider, vscode.CodeLensProvider, vscode.Disposable {
  private readonly changed = new vscode.EventEmitter<vscode.Uri>();
  private readonly lensesChanged = new vscode.EventEmitter<void>();
  private readonly timers = new Map<string, NodeJS.Timeout>();
  private readonly subscriptions: vscode.Disposable[];

  readonly onDidChange = this.changed.event;
  readonly onDidChangeCodeLenses = this.lensesChanged.event;

  constructor(private readonly client: () => LanguageClient | undefined) {
    this.subscriptions = [
      vscode.workspace.onDidChangeTextDocument((event) => {
        if (event.document.languageId === 'vecdsl') {
          this.schedule(event.document.uri.toString());
        }
      }),
      vscode.workspace.onDidChangeConfiguration((event) => {
        if (event.affectsConfiguration('vecdsl.preview')) {
          this.lensesChanged.fire();
        }
      }),
    ];
  }

  /** Called when a client has started: lenses and open previews ask the new server. */
  refresh(): void {
    this.lensesChanged.fire();
    vscode.workspace.textDocuments.filter((document) => document.uri.scheme === SCHEME)
      .forEach((document) => { this.changed.fire(document.uri); });
  }

  dispose(): void {
    this.timers.forEach((timer) => { clearTimeout(timer); });
    this.subscriptions.forEach((subscription) => { subscription.dispose(); });
    this.changed.dispose();
    this.lensesChanged.dispose();
  }

  async provideTextDocumentContent(uri: vscode.Uri): Promise<string> {
    const key = previewKey(uri.path);
    if (key === undefined) {
      return `Not a vecDSL preview: ${uri.toString()}\n`;
    }
    const client = this.client();
    const unavailable = this.unavailable(client);
    if (unavailable !== undefined || client === undefined) {
      return `${LANGUAGES[key.language].comment} ${unavailable ?? ''}\n`;
    }
    try {
      const outline = await request(client, { uri: key.source });
      const statement = outline.statements[key.index];
      const result = statement === undefined ? undefined : await request(client, {
        uri: key.source, range: statement.range, target: key.target, language: LANGUAGES[key.language].request,
      });
      return render({ key, sourceName: path.posix.basename(vscode.Uri.parse(key.source).path), outline, result });
    } catch (error) {
      return `${LANGUAGES[key.language].comment} vecdsl/generate failed: ${String(error)}\n`;
    }
  }

  async provideCodeLenses(document: vscode.TextDocument): Promise<vscode.CodeLens[]> {
    const client = this.client();
    const configuration = vscode.workspace.getConfiguration('vecdsl.preview', document);
    if (!configuration.get<boolean>('codeLens', true) || client === undefined
      || this.unavailable(client) !== undefined) {
      return [];
    }
    const outline = await request(client, { uri: document.uri.toString() });
    const target = configuration.get<string>('target', '') || outline.targets[0] || this.targets(client)[0];
    const setting = configuration.get<string>('language', 'python');
    const language: Language = isLanguage(setting) ? setting : 'python';
    if (target === undefined) {
      return [];
    }
    return outline.statements.map((statement, index) => {
      const line = statement.range.start.line;
      return new vscode.CodeLens(new vscode.Range(line, 0, line, 0), {
        title: `Preview: ${target} ${language}`,
        command: 'vecdsl.previewGeneratedCode',
        arguments: [document.uri.toString(), index, target, language],
      });
    });
  }

  /** The command: with arguments from a lens, or for the statement at the cursor after picking target and language. */
  async command(source?: string, index?: number, target?: string, language?: Language): Promise<void> {
    if (source !== undefined && index !== undefined && target !== undefined && language !== undefined) {
      await open({ source, index, target, language });
      return;
    }
    const editor = vscode.window.activeTextEditor;
    const client = this.client();
    const unavailable = this.unavailable(client);
    if (unavailable !== undefined || client === undefined) {
      void vscode.window.showWarningMessage(unavailable ?? '');
      return;
    }
    if (editor?.document.languageId !== 'vecdsl') {
      void vscode.window.showInformationMessage('Open a .vecDSL file and place the cursor in a statement.');
      return;
    }
    const outline = await request(client, { uri: editor.document.uri.toString() });
    if (outline.statements.length === 0) {
      void vscode.window.showInformationMessage('The program has no statements.');
      return;
    }
    const cursor = editor.selection.active;
    const at = outline.statements.findLastIndex((statement) => statement.range.start.line < cursor.line
      || (statement.range.start.line === cursor.line && statement.range.start.character <= cursor.character));
    const picked = await vscode.window.showQuickPick(outline.targets.length > 0 ? outline.targets : this.targets(client),
      { title: outline.targets.length > 0 ? 'Target (the program\'s targets)' : 'Target' });
    if (picked === undefined) {
      return;
    }
    const host = await vscode.window.showQuickPick(Object.keys(LANGUAGES), { title: 'Host language' });
    if (host === undefined || !isLanguage(host)) {
      return;
    }
    await open({ source: editor.document.uri.toString(), index: Math.max(at, 0), target: picked, language: host });
  }

  private targets(client: LanguageClient): readonly string[] {
    return generateCapability(client.initializeResult?.capabilities.experimental).targets;
  }

  /** Why the preview cannot ask the server, or undefined when it can. */
  private unavailable(client: LanguageClient | undefined): string | undefined {
    if (client?.isRunning() !== true) {
      return 'The vecDSL language server is not running; see "vecDSL: Show Server Log".';
    }
    const version = generateCapability(client.initializeResult?.capabilities.experimental).version;
    return version >= GENERATE_VERSION ? undefined : `This vecdsl-lsp answers vecdsl/generate version ${String(version)};`
      + ` the preview needs version ${String(GENERATE_VERSION)}. Set vecdsl.lsp.jar to a jar built from this`
      + ' repository and run "vecDSL: Restart Language Server".';
  }

  private schedule(source: string): void {
    clearTimeout(this.timers.get(source));
    this.timers.set(source, setTimeout(() => {
      this.timers.delete(source);
      vscode.workspace.textDocuments.filter((document) => document.uri.scheme === SCHEME
        && previewKey(document.uri.path)?.source === source).forEach((document) => { this.changed.fire(document.uri); });
    }, DEBOUNCE_MS));
  }
}

async function request(client: LanguageClient, params: GenerateParams): Promise<GenerateResult> {
  return client.sendRequest<GenerateResult>(GENERATE, params);
}

async function open(key: PreviewKey): Promise<void> {
  const document = await vscode.workspace.openTextDocument(vscode.Uri.from({ scheme: SCHEME, path: previewPath(key) }));
  await vscode.languages.setTextDocumentLanguage(document, key.language);
  await vscode.window.showTextDocument(document, { viewColumn: vscode.ViewColumn.Beside, preserveFocus: true });
}
