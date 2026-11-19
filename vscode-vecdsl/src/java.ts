import * as path from 'node:path';

/** The oldest Java vecdsl-lsp runs on (vecdsl-lsp/README.md). */
export const MINIMUM_MAJOR = 21;

/** The first major version that warns about sun.misc.Unsafe unless the option allows it (JEP 498). */
export const UNSAFE_OPTION_MAJOR = 24;

export const UNSAFE_OPTION = '--sun-misc-unsafe-memory-access=allow';

/** The arguments that make a java print its properties (java.home among them) and its version on standard error. */
export const PROBE_ARGUMENTS = ['-XshowSettings:properties', '-version'];

export interface JavaRuntime {
  readonly home: string;
  readonly version: string;
  readonly major: number;
  readonly executable: string;
}

/** The java to probe: bin/java of the setting, else of JAVA_HOME, else the java of the PATH. */
export function probeCommand(setting: string, javaHome: string | undefined, platform: NodeJS.Platform): string {
  const home = setting.trim() || javaHome?.trim();
  return home ? executableIn(home, platform) : 'java';
}

export function executableIn(home: string, platform: NodeJS.Platform): string {
  return platform === 'win32' ? path.win32.join(home, 'bin', 'java.exe') : path.posix.join(home, 'bin', 'java');
}

/** The runtime described by the output of `java -XshowSettings:properties -version`, or why there is none. */
export function runtimeOf(output: string, platform: NodeJS.Platform): JavaRuntime | string {
  const home = /^\s*java\.home = (.+?)\s*$/m.exec(output)?.[1];
  const version = /\bversion "([^"]+)"/.exec(output)?.[1];
  if (home === undefined || version === undefined) {
    return 'its output has no java.home property or no version line';
  }
  const major = majorOf(version);
  if (major < MINIMUM_MAJOR) {
    return `it is Java ${version}; vecdsl-lsp needs Java ${String(MINIMUM_MAJOR)} or later`;
  }
  return { home, version, major, executable: executableIn(home, platform) };
}

/** 25 for "25.0.2", 21 for "21.0.12.1", 26 for "26-ea", 8 for "1.8.0_402". */
export function majorOf(version: string): number {
  const parts = version.split(/[.+_-]/);
  return Number(parts[0] === '1' ? parts[1] : parts[0]);
}

/** The arguments of the server's JVM: the Unsafe option from Java 24 on, the jar, the log file if any. */
export function serverArguments(major: number, jar: string, log: string | undefined): string[] {
  return [...(major >= UNSAFE_OPTION_MAJOR ? [UNSAFE_OPTION] : []), '-jar', jar, ...(log ? ['--log', log] : [])];
}

/**
 * The server's executable for vscode-languageclient: the command and arguments, and no transport. With
 * TransportKind.stdio the client appends --stdio to the arguments, which vecdsl-lsp refuses (exit 2); without a
 * transport it talks over the process's standard input and output as they are.
 */
export function serverExecutable(java: JavaRuntime, args: readonly string[]): { command: string; args: string[] } {
  return { command: java.executable, args: [...args] };
}
