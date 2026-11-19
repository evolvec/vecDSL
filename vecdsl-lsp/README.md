# vecdsl-lsp, the vecDSL Language Server

Xtext 2.42.0's language server for vecDSL 1.0 with the editor services of org.ftn.vecDSL.ide: vendor-aware
completion, Markdown hovers, quick fixes as code actions, formatting, and the `vecdsl.coverage.unsupported` warnings.
One runnable jar, `target/vecdsl-lsp.jar`, Java 21 or later. Build: BUILD.md.

![vecDSL CI](../coverage-site/site/badges/ci.svg)

## Command line

```
java -jar target/vecdsl-lsp.jar [--log <file>] [--socket <port>]
java -jar target/vecdsl-lsp.jar version
bin\vecdsl-lsp.cmd version          bin\vecdsl-lsp.ps1 version          bin/vecdsl-lsp.sh version
```

- Without options the server speaks the Language Server Protocol (JSON-RPC with `Content-Length` headers) over
  standard input and output. Standard output carries only protocol messages; logs and stack traces go to standard
  error.
- `--log <file>` appends standard error to the file (log4j at WARN, LSP4J's `java.util.logging`, stack traces).
- `--socket <port>` listens on 127.0.0.1:<port> instead and serves each connection as one client.
- `version` prints the four lines of `vecdsl version` (language, IR, grammar namespace, tool version) and the LSP4J
  version.
- Exit status: 0 after `shutdown` and `exit`, 1 after `exit` without `shutdown` or when the client closes the input
  without them, 2 for a usage error.
- On Java 24 and later, add `--sun-misc-unsafe-memory-access=allow` before `-jar`, or Guice 7.0.0 prints a warning
  on standard error. The launchers in `bin/` do that.

## From an editor

Any LSP client that can start a process works: give it the command line above (java, the option on Java 24+, `-jar`,
the jar path) with stdio transport, and the file extension `.vecDSL` (language id `vecdsl`). Start java itself; the
`.ps1` launcher is for the command line, since PowerShell does not pass a native program's standard streams through
byte for byte. The server reads the documents the client opens and the files under the workspace folders (or the root
URI) of `initialize`; it never writes files and its build never runs the generators. A client that sends neither a root URI nor
workspace folders (an editor with single files) gets a server that scans nothing and serves the files it opens.

Capabilities announced in `initialize` (Xtext's `LanguageServerImpl` with the vecDSL bindings):

| capability | value | source |
|---|---|---|
| `textDocumentSync` | 2 (incremental) | Xtext |
| `hoverProvider` | true; Markdown, the keyword's construct, grammar comment, design note and vendor table | `VecDSLHoverService` |
| `completionProvider` | trigger character `.`, no resolve; the vendor status in each item's `detail`, unsupported items last | `VecDSLIdeContentProposalProvider` |
| `codeActionProvider` | true; the quick fixes of `QuickFixes` | `QuickFixCodeActionService`, `VecDSLIdeQuickfixProvider` |
| `documentFormattingProvider`, `documentRangeFormattingProvider` | true; indentation from the client's options, width 100, the document's line separator | `VecDSLFormattingService` |
| `definitionProvider`, `referencesProvider`, `documentSymbolProvider`, `workspaceSymbolProvider`, `documentHighlightProvider`, `renameProvider`, `signatureHelpProvider`, `semanticTokensProvider` | Xtext's defaults | Xtext |
| `experimental` | `{"vecdsl": {"generate": "vecdsl/generate", "generateVersion": 2, "targets": [14 vendor ids]}}` | `GenerateEndpoint` |

Diagnostics are pushed with `textDocument/publishDiagnostics` (no capability is involved): syntax errors, the
language validator's errors and warnings, and one warning `vecdsl.coverage.unsupported` per construct a target of the
script does not support (`message` "unsupported on <vendor>: <reason>", `data` `[vendor, feature]`).

`vecdsl/generate` (version 2) returns the files `vecdsl gen` would write for the
open document, the script's top-level statements and `targets [...]`, and, for the statement at `range`, the lines of
its code in each file (`sections`) and its constructs the target does not support (`unsupported`, the text of the
warnings). Without `target` it generates nothing and returns the outline (`statements`, `targets`, `errors`):

```
-> {"jsonrpc": "2.0", "id": 7, "method": "vecdsl/generate",
    "params": {"uri": "file:///.../pinecone_demo.vecDSL", "target": "pinecone", "language": "py",
               "range": {"start": {"line": 35, "character": 2}, "end": {"line": 35, "character": 2}}}}
<- {"jsonrpc": "2.0", "id": 7, "result": {"statement": "similar", "errors": [],
    "files": [{"path": "pinecone/python/product_catalog.py", "text": "..."}, ...],
    "statements": [{"name": "connectCloud", "range": {...}}, ...], "targets": ["pinecone"],
    "sections": [{"path": "pinecone/python/product_catalog.py", "start": 109, "end": 121},
                 {"path": "pinecone/python/product_catalog.py", "start": 253, "end": 254}],
    "unsupported": []}}
```

## Tests

`mvn -q clean package` runs `MainTest` and `LspClientTest` with the server in the test JVM (surefire), then
`LspClientTest` again against the shaded jar as a subprocess (failsafe), each time with `RootlessClientTest`: 54 test
runs.
`src/test/scripts/lsp_client.py` is a second client in Python (standard library only) that checks the same against the
jar and compares formatting and `vecdsl/generate` with vecdsl-cli's own `fmt` and `gen`; `preview_sweep.py` checks the
statement sections on the samples for every target and language:

```
python -I src/test/scripts/lsp_client.py --workspace .. --fmt "java -jar ../vecdsl-cli/target/vecdsl-cli.jar" -- java -jar target/vecdsl-lsp.jar
python -I src/test/scripts/preview_sweep.py --workspace .. -- java -jar target/vecdsl-lsp.jar
```

## CI

The job `build-lsp` of `.github/workflows/ci.yml` runs `ci/build-lsp.sh`: `mvn -q clean package` with its 54 test runs
(surefire `MainTest` 2, `LspClientTest` 25, `RootlessClientTest` 1; failsafe `LspClientTest` 25, `RootlessClientTest` 1
against the shaded jar), then `lsp_client.py` (25 of 25 checks) and `preview_sweep.py` (9282 requests, 9282 passed, 0
answered with an error) against `target/vecdsl-lsp.jar`; the client formats with vecdsl-cli's jar from the job
`build-cli`. Locally, from the repository root in Git Bash or on Linux: `bash ci/run_all.sh build-cli build-lsp`. Jobs:
`ci/README.md`; expected numbers: `ci/expected.env`. The badge above is `coverage-site/site/badges/ci.svg`, written by
`coverage-site/tools/build_site.py` (it shows the CI run once CI builds the site, and is missing until the site is
built).
