import { isLanguage, type Language } from './protocol';

export const SCHEME = 'vecdsl-preview';

/** One preview: the source document's URI, the statement's index among the script's top-level statements, the target. */
export interface PreviewKey {
  readonly source: string;
  readonly index: number;
  readonly target: string;
  readonly language: Language;
}

/** The path of the preview's URI: /<source uri, percent-encoded>/<statement index>/<target>/<language>. */
export function previewPath(key: PreviewKey): string {
  return `/${encodeURIComponent(key.source)}/${String(key.index)}/${key.target}/${key.language}`;
}

export function previewKey(previewPath: string): PreviewKey | undefined {
  const parts = previewPath.split('/');
  const [empty, source, index, target, language] = parts;
  if (parts.length !== 5 || empty !== '' || !source || !index || !/^\d+$/.test(index) || !target
    || !/^[a-z]+$/.test(target) || language === undefined || !isLanguage(language)) {
    return undefined;
  }
  return { source: decodeURIComponent(source), index: Number(index), target, language };
}
