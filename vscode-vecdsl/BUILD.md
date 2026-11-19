# Building vscode-vecdsl

The extension is built with Node.js and npm from this folder; everything it needs from the rest of the repository is the
grammar (`../org.ftn.vecDSL/src/org/ftn/vecDSL/VecDSL.xtext`), the samples for the grammar test
(`../org.ftn.vecDSL.tests/samples`) and the server jar (`../vecdsl-lsp/target/vecdsl-lsp.jar`, built by Maven, see
vecdsl-lsp/BUILD.md). The commands are the same on Windows (PowerShell), Linux and macOS.

## Requirements

- Node.js 22.13 or later (`node -v`): `@vscode/vsce` 4.1.1 and `@vscode/test-electron` 3.1.0 declare `"node": ">= 22"`,
  ESLint 10.15.0 `^20.19.0 || ^22.13.0 || >=24`.
- Python 3 as `python` on the PATH (`npm run check-grammar` calls `python tools/tmgrammar.py --check`).
- For packaging, the server jar: `../vecdsl-lsp/target/vecdsl-lsp.jar`, or any jar named by the environment variable
  `VECDSL_LSP_JAR`.

## Commands

| command | does | expected | time |
| --- | --- | --- | --- |
| `npm ci` | installs the locked dependencies (package-lock.json) | exit 0, `added 253 packages` | 6 s (22 s with an empty npm cache) |
| `npm run check-grammar` | `tools/tmgrammar.py --check`: the grammar files equal what VecDSL.xtext gives | `checked: 584 keywords (13 vendor ids), brackets {} [] ()`, exit 0 | 0.2 s |
| `npm run grammar` | writes `syntaxes/vecdsl.tmLanguage.json` and `language-configuration.json` from the grammar | `wrote: ...` | under 1 s |
| `npm run compile` | `tsc -p .` (type check, strict), then esbuild bundles `src/extension.ts` into `out/extension.js` | exit 0, no output | 2 s |
| `npm run lint` | ESLint 10 with typescript-eslint `strictTypeChecked` over src, test, integration, tools | exit 0, no output | 5 s |
| `npm test` | compiles src, test and integration to `out-test/` and runs `node --test` on the pure tests | `# tests 19`, `# pass 19`, `# fail 0` | 3 s |
| `npm run notices` / `npm run check-notices` | `tools/notices.py`: writes / checks `THIRD-PARTY-NOTICES.txt` from `out/extension.js`, `node_modules` and the jar table in the script; after `npm run compile` | `wrote: THIRD-PARTY-NOTICES.txt (9 npm packages, 15 server components)` / `notices: up to date` | under 1 s |
| `npm run server` | `tools/copy-server.mjs`: copies the jar into `server/` and prints its size and md5 | `copy-server: ... md5 ...` | under 1 s |
| `npx @vscode/vsce package` | runs `vscode:prepublish` (check-grammar, compile, check-notices, server), then writes `vscode-vecdsl-0.1.2.vsix` | `DONE  Packaged: ... (12 files, ...)` | 4 to 7 s |
| `npm run test:integration` | the VS Code test of `integration/` in two windows (a folder and a single file); with `$env:VSCODE_EXECUTABLE` set to the installed `Code.exe` it uses that VS Code (close every VS Code window first), otherwise it downloads VS Code 1.91.0 into `.vscode-test/`; needs `server/vecdsl-lsp.jar` (`npm run server`) | `window with the folder ...`, lines 1 to 7, `window with the file ...`, line 8, exit 0 | about a minute after the download |

`vsce` prints one warning it is told to accept in package.json (`"vsce"`): no `repository` field. The vsix carries
`LICENSE` (Apache License 2.0) and `NOTICE`, the same files as the repository's. The extension has no Marketplace publisher; `vecdsl` is a
placeholder and the vsix is installed from file.

## Server process check

`node out-test/integration/killcheck.js <vecdsl-lsp.jar>` (after `npm test`, which compiles it) starts the server as
the extension does (Java probed from `JAVA_HOME` or the PATH, `<java.home>/bin/java`, the same arguments) three ways:
shutdown and exit (status 0), a client process killed (the server must end by itself), and, on Linux, whether the
spawned process is the JVM of `java.home` (`/proc/<pid>/exe`).

## Files

| path | what |
| --- | --- |
| `package.json`, `package-lock.json` | manifest (contributions, settings, commands), locked dependencies |
| `src/extension.ts` | activation, Java probe, client start and stop, commands |
| `src/preview.ts` | preview content provider, code lenses, the preview command |
| `src/java.ts`, `src/settings.ts`, `src/previewUri.ts`, `src/render.ts`, `src/protocol.ts` | logic without `vscode` imports, tested by `node --test` |
| `test/*.test.ts`, `test/fixtures/*.json` | unit tests; fixtures are results of `vecdsl/generate` on pinecone_demo.vecDSL recorded from vecdsl-lsp |
| `integration/` | VS Code test (`run.ts`, `suite.ts`) and the server process check (`killcheck.ts`) |
| `tools/notices.py`, `tools/licenses/`, `THIRD-PARTY-NOTICES.txt` | the notices generator, the license texts it appends, its output (in the vsix) |
| `tools/tmgrammar.py`, `syntaxes/vecdsl.tmLanguage.json`, `language-configuration.json` | the generator and the files it writes |
| `tools/copy-server.mjs`, `esbuild.mjs`, `tsconfig*.json`, `eslint.config.mjs`, `.vscodeignore` | build |

`node_modules/`, `out/`, `out-test/`, `server/` and `.vscode-test/` are build output.
