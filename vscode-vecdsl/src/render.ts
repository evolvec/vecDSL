import type { PreviewKey } from './previewUri';
import { LANGUAGES, type GenerateResult } from './protocol';

/** What the preview of one statement shows: the request's result, or the outline when the statement is gone. */
export interface PreviewInput {
  readonly key: PreviewKey;
  readonly sourceName: string;
  readonly outline: GenerateResult;
  readonly result: GenerateResult | undefined;
}

/**
 * The preview's text: a commented header, then the program's errors, the statement's unsupported constructs on the
 * target (the text of the editor warnings) and the lines of the statement's code in each generated file. Lines are
 * split as Java's String.lines() splits them, which is how the server counts them.
 */
export function render(input: PreviewInput): string {
  const { key, outline, result } = input;
  const comment = LANGUAGES[key.language].comment;
  const statement = outline.statements[key.index];
  const out: string[] = [];
  const note = (text: string): void => {
    out.push(`${comment} ${text}`);
  };
  if (statement === undefined || result === undefined) {
    note(`vecDSL preview: ${input.sourceName} has ${String(outline.statements.length)} statements; statement ${
      String(key.index + 1)} no longer exists.`);
    return out.join('\n') + '\n';
  }
  note(`vecDSL preview of ${statement.name === undefined ? 'the unlabeled statement' : `statement '${statement.name}'`}`
    + ` (${String(key.index + 1)} of ${String(outline.statements.length)}, line ${
      String(statement.range.start.line + 1)}) in ${input.sourceName}`);
  note(`target ${key.target}, ${LANGUAGES[key.language].label}: the code vecdsl gen writes, from vecdsl-lsp`);
  if (result.errors.length > 0) {
    note('The program has errors, so vecdsl gen writes nothing:');
    result.errors.forEach((error) => {
      note(`  ${error}`);
    });
  }
  const unsupported = result.unsupported.filter((entry) => entry.vendor === key.target);
  if (unsupported.length > 0) {
    note(`Unsupported on ${key.target} (the editor's warnings):`);
    unsupported.forEach((entry) => {
      note(`  line ${String(entry.range.start.line + 1)}: ${entry.message}`);
    });
  }
  if (result.errors.length > 0) {
    return out.join('\n') + '\n';
  }
  if (result.targets.length > 0 && !result.targets.includes(key.target)) {
    note(`${key.target} is not in the program's targets [${result.targets.join(', ')}], so vecdsl gen writes nothing`
      + ' for it.');
    return out.join('\n') + '\n';
  }
  if (result.sections.length === 0) {
    note(result.files.length === 0 ? `vecdsl gen writes no ${LANGUAGES[key.language].label} files for ${key.target}.`
      : `No code of this statement was found in the ${String(result.files.length)} generated files.`);
    return out.join('\n') + '\n';
  }
  const files = new Map(result.files.map((file) => [file.path, file.text.split(/\r\n|\r|\n/)]));
  for (const section of result.sections) {
    out.push('');
    note(`${section.path}, lines ${String(section.start + 1)} to ${String(section.end)}`);
    out.push(...(files.get(section.path) ?? []).slice(section.start, section.end));
  }
  return out.join('\n') + '\n';
}
