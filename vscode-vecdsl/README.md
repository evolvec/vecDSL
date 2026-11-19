# vscode-vecdsl

VS Code support for vecDSL 1.0 files (`.vecDSL`) through the vecDSL Language Server, vecdsl-lsp: the vendor
warnings `vecdsl.coverage.unsupported` and the language's errors, Markdown hovers with each target's status and calls,
completion with the vendor status of every proposal, quick fixes, formatting, and a preview of the code `vecdsl gen`
writes for each statement. The extension starts the server and adds the preview; everything else comes from the server
unchanged. Highlighting comes from a TextMate grammar generated from `VecDSL.xtext`.

CI badge: `coverage-site/site/badges/ci.svg` of the repository, written by the CI job `site`. It is not linked here:
`vsce package` rejects relative images and links in this README while package.json has no `repository`.

## Requirements

- VS Code 1.91 or later (`engines.vscode` `^1.91.0`). Check yours with `code --version` (first line) or Help > About.
- Java 21 or later. The extension finds it in this order: the setting `vecdsl.java.home`, the environment variable
  `JAVA_HOME`, the `java` on the PATH; it asks that java for its `java.home` and starts `<java.home>\bin\java.exe`
  itself, so a launcher on the PATH (Oracle's `javapath`) never stands between VS Code and the server.

## Install from the vsix

```
code --install-extension vscode-vecdsl-0.1.2.vsix
```

or Extensions view > `...` > Install from VSIX. The server jar is inside the vsix (`server/vecdsl-lsp.jar`). There is
no Marketplace release. `THIRD-PARTY-NOTICES.txt` in the vsix lists the components it ships and their licenses.

## Using it

| Action | How |
| --- | --- |
| Warnings and errors | squiggles in the editor and the Problems panel (Ctrl+Shift+M) |
| Hover | rest the mouse on a keyword, a field, a collection, a `$parameter` or an `env(...)` value |
| Content assist | Ctrl+Space; each proposal shows its status on the program's targets, unsupported ones last |
| Quick fix | Ctrl+. on a squiggle, or the light bulb |
| Format | Shift+Alt+F (format on save stays off, as in Eclipse; VS Code's `editor.formatOnSave` can turn it on) |
| Preview generated code | the code lens "Preview: <target> <language>" above each statement, or "vecDSL: Preview Generated Code" with the cursor in a statement |

The preview opens to the side as a read-only document in the host language: a header, the constructs of the statement
the target does not support (the text of the editor warnings), and the lines of the statement's code in each generated
file, for example the method and its call in `main`. It follows the source as you type. A target outside the program's
`targets [...]` gets no code, because `vecdsl gen` writes none for it.

## Settings

| Setting | Default | Meaning |
| --- | --- | --- |
| `vecdsl.java.home` | empty | Java home whose `bin/java` runs the server; empty: `JAVA_HOME`, else the `java.home` of the `java` on the PATH |
| `vecdsl.lsp.jar` | empty | the server jar; empty: the jar in the extension's `server` folder |
| `vecdsl.lsp.log` | empty | log file passed to the server as `--log`; relative to the first workspace folder; empty: the server's standard error goes to the output channel |
| `vecdsl.trace.server` | `off` | protocol trace (`messages`, `verbose`) in the output channel "vecDSL Language Server" |
| `vecdsl.preview.codeLens` | `true` | the preview code lenses |
| `vecdsl.preview.target` | empty | the lens target; empty: the program's first target, else the server's first |
| `vecdsl.preview.language` | `python` | the lens language: `python`, `typescript` or `java` |

The Java, jar and log settings are read when the server starts: at activation and on "vecDSL: Restart Language Server".

## Commands

- **vecDSL: Restart Language Server**: stops the server (shutdown and exit; the JVM ends), reads the settings, finds
  Java again and starts a new server.
- **vecDSL: Show Server Log**: the log file of `vecdsl.lsp.log`, or the output channel "vecDSL Language Server"
  (the Java found, the command line, the server's standard error, the trace).
- **vecDSL: Preview Generated Code**: picks a target (the program's targets, or all 14 when it declares none) and a
  host language, then opens the preview of the statement at the cursor.

Build: BUILD.md.

## CI

The job `build-extension` of the repository's `.github/workflows/ci.yml` runs `ci/build-extension.sh`: `npm ci`,
`check-grammar` (584 keywords), `compile`, `lint`, `npm test` (19 tests), `check-notices` and `vsce package` with the
server jar of the job `build-lsp`, writing the vsix into `ci/.work/` so that the vsix in this folder stays as it is. The
optional job `extension-integration` runs `npm run test:integration` under xvfb-run (8 numbered checks); its failure does
not fail the workflow. Locally, from the repository root in Git Bash or on Linux: `bash ci/run_all.sh build-extension`,
and `CI_INTEGRATION=1 bash ci/run_all.sh extension-integration` with `VSCODE_EXECUTABLE` set. Jobs and expected numbers:
`ci/README.md` and `ci/expected.env` of the repository.
