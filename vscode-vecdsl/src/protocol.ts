/**
 * vecdsl/generate of vecdsl-lsp; version 2 adds the outline, sections and unsupported. The
 * server's JSON leaves out null values, so a statement without a label has no name.
 */
export const GENERATE = 'vecdsl/generate';

/** The version of vecdsl/generate the preview needs: statements, targets, sections and unsupported in the result. */
export const GENERATE_VERSION = 2;

export interface Position {
  readonly line: number;
  readonly character: number;
}

export interface Range {
  readonly start: Position;
  readonly end: Position;
}

export interface GenerateParams {
  readonly uri: string;
  readonly range?: Range;
  readonly target?: string;
  readonly language?: string;
}

export interface Statement {
  readonly name?: string;
  readonly range: Range;
}

export interface Section {
  readonly path: string;
  readonly start: number;
  readonly end: number;
}

export interface Unsupported {
  readonly vendor: string;
  readonly feature: string;
  readonly message: string;
  readonly range: Range;
}

export interface GenerateResult {
  readonly statement?: string;
  readonly files: readonly { readonly path: string; readonly text: string }[];
  readonly errors: readonly string[];
  readonly statements: readonly Statement[];
  readonly targets: readonly string[];
  readonly sections: readonly Section[];
  readonly unsupported: readonly Unsupported[];
}

/** What initialize announces under capabilities.experimental.vecdsl. */
export interface GenerateCapability {
  readonly version: number;
  readonly targets: readonly string[];
}

export function generateCapability(experimental: unknown): GenerateCapability {
  const vecdsl = field(experimental, 'vecdsl');
  const version = field(vecdsl, 'generateVersion');
  const targets = field(vecdsl, 'targets');
  return {
    version: field(vecdsl, 'generate') !== GENERATE ? 0 : typeof version === 'number' ? version : 1,
    targets: Array.isArray(targets) ? targets.filter((target): target is string => typeof target === 'string') : [],
  };
}

function field(value: unknown, name: string): unknown {
  return typeof value === 'object' && value !== null ? (value as Record<string, unknown>)[name] : undefined;
}

/** The host languages of vecdsl gen --lang, with the VS Code language and the line comment of each. */
export const LANGUAGES = {
  python: { request: 'py', label: 'Python', comment: '#' },
  typescript: { request: 'ts', label: 'TypeScript', comment: '//' },
  java: { request: 'java', label: 'Java', comment: '//' },
} as const;

export type Language = keyof typeof LANGUAGES;

export function isLanguage(value: string): value is Language {
  return Object.hasOwn(LANGUAGES, value);
}
