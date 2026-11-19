# Changelog

## 0.1.2 (19 November 2026)

- A window without a folder: the extension no longer makes the folder of the first `.vecDSL` file the server's root,
  so the server reads only the files VS Code opens. vecdsl-lsp starts without a root itself;
  older jars start too, because the client announces workspace folders.
- Licensed under the Apache License 2.0: `LICENSE` in the vsix and `"license": "Apache-2.0"` in package.json.
- `THIRD-PARTY-NOTICES.txt` in the vsix: the bundled npm packages and the components of the server jar, with their
  licenses (`npm run notices`, checked when packaging).
- The VS Code test runs in two windows (a folder, a single file) and with an installed VS Code
  (`VSCODE_EXECUTABLE`); it also opens the preview from a code lens.
- Bundles a vecdsl-lsp jar that starts without a root (`vecdsl/generate` version 2 unchanged; the Vespa generator reads
  a connection uri without a scheme as http).

## 0.1.1 (18 November 2026)

- Fix: the server did not start. The client was configured with `TransportKind.stdio`, for which
  vscode-languageclient appends `--stdio` to the command line; vecdsl-lsp refuses unknown arguments (exit 2). The
  server is now started over plain stdio with only the arguments vecdsl-lsp accepts: the Unsafe option on Java 24 and later, `-jar`, the jar path and, when set, `--log`.
- Bundles a vecdsl-lsp jar that answers `vecdsl/generate` version 2, so the preview works without the
  `vecdsl.lsp.jar` setting.

## 0.1.0 (18 November 2026)

- Language `vecdsl` for `.vecDSL` files: TextMate grammar and language configuration generated from vecDSL 1.0's
  `VecDSL.xtext` by `tools/tmgrammar.py`.
- Client of vecdsl-lsp (vscode-languageclient 11.0.0, stdio), started with the JVM of `java.home`, with
  `--sun-misc-unsafe-memory-access=allow` from Java 24 on; diagnostics, hover, completion, code actions and formatting
  from the server.
- Settings `vecdsl.java.home`, `vecdsl.lsp.jar`, `vecdsl.lsp.log`, `vecdsl.trace.server`, `vecdsl.preview.*`.
- Commands "vecDSL: Restart Language Server", "vecDSL: Show Server Log", "vecDSL: Preview Generated Code".
- Generated-code preview per statement, target and language (scheme `vecdsl-preview`), with code lenses; needs
  `vecdsl/generate` version 2.
- Bundles a vecdsl-lsp jar that answers `vecdsl/generate` version 1; see README for the setting that points to a newer
  jar.
