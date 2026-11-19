import * as path from 'node:path';

export interface ServerSettings {
  readonly javaHome: string;
  readonly jar: string;
  readonly log: string | undefined;
}

/**
 * The settings as the server is started with them: the bundled jar when vecdsl.lsp.jar is empty, no log when
 * vecdsl.lsp.log is empty, a relative log path taken from the first workspace folder (else from the extension).
 */
export function serverSettings(raw: { javaHome: string; jar: string; log: string }, extension: string,
  workspace: string | undefined): ServerSettings {
  const log = raw.log.trim();
  return {
    javaHome: raw.javaHome.trim(),
    jar: raw.jar.trim() || path.join(extension, 'server', 'vecdsl-lsp.jar'),
    log: log ? path.resolve(workspace ?? extension, log) : undefined,
  };
}
