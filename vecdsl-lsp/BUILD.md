# Building vecdsl-lsp

The build reads org.ftn.vecDSL (`src`, `src-gen`, `xtend-gen`) and org.ftn.vecDSL.ide (`src`, `src-gen`) of the
repository in place, and its tests read `org.ftn.vecDSL.tests/samples` and `spec/conformance`, so run it from a clone
of the repository. The commands are for Windows PowerShell; on Linux and macOS use the same Maven command and
`bin/vecdsl-lsp.sh`.

## 1. Prerequisites

```powershell
java -version      # 21 or later
mvn -v             # Maven 3.9 or later
```

As for vecdsl-cli (its BUILD.md, step 1): release 21, and on Java 24 and later Maven prints four `WARNING:` lines for
its own Guice, which change nothing.

## 2. Build

```powershell
Set-Location vecdsl-lsp
mvn -q clean package
```

Expected: `$LASTEXITCODE` 0, and no output besides, at most, the line each run of the client test prints
(`LspClientTest: server in-process over piped streams`, `RootlessClientTest: server in-process over piped streams`, then
the same two lines with `server subprocess of ...\target\vecdsl-lsp.jar`); `target\surefire-reports` and
`target\failsafe-reports` show that both ran. Maven compiles the bundles' sources with vecdsl-lsp's, runs `MainTest`
(2), `LspClientTest` (25) and `RootlessClientTest` (1) with surefire, shades **`vecdsl-lsp\target\vecdsl-lsp.jar`**,
then runs `LspClientTest` and `RootlessClientTest` again with failsafe against that jar (26): 54 test runs, so
the tests also cover the shaded jar. The first build downloads the dependencies of `pom.xml` from Maven Central.
`mvn -q package -DskipTests` builds the jar without tests (failsafe is skipped too).

## 3. Jar listing

The jar's `org/ftn/vecDSL` entries other than `org/ftn/vecDSL/lsp` must be exactly the files of
`org.ftn.vecDSL\bin\org` and `org.ftn.vecDSL.ide\bin\org` (without the Xtend builder's `.xtendbin` and `._trace`
files), which Eclipse writes; the listing of vecdsl-cli's BUILD.md, step 3, applies with both `bin` folders.
`META-INF/services/org.eclipse.xtext.ISetup` holds the single line `org.ftn.vecDSL.lsp.VecDSLServerSetup`, so
Xtext finds the server's setup and no other.

## 4. Smoke test

```powershell
bin\vecdsl-lsp.cmd version
```

Expected:

```
language: vecDSL 1.0 (1.0.1 documentation patch)
IR: 1.0
grammar namespace: http://www.ftn.org/vecDSL/VecDSL/7
vecdsl-lsp: 0.1.0 (implements vecDSL >=1.0,<2.0)
LSP4J: 1.0.0
```

and nothing on standard error. `bin\vecdsl-lsp.ps1 version` prints the same. Without arguments the jar waits for an
LSP client on standard input; Ctrl+C ends it.

## 5. A session from the command line

`src\test\scripts\lsp_client.py` (Python 3, standard library) runs a scripted session against the jar. With `--quick`
it opens `pinecone_demo.vecDSL`, prints its diagnostics, one hover, one completion list, one code action and whether
formatting equals `vecdsl fmt` (7 checks); without it every check runs, formatting compared with `vecdsl fmt` on all 28
samples and `vecdsl/generate` with `vecdsl gen` (25 checks, about four minutes):

```powershell
python -I src\test\scripts\lsp_client.py --workspace .. --fmt "java --sun-misc-unsafe-memory-access=allow -jar ..\vecdsl-cli\target\vecdsl-cli.jar" -- java --sun-misc-unsafe-memory-access=allow -jar target\vecdsl-lsp.jar
```

(On Java 21, leave out both `--sun-misc-unsafe-memory-access=allow`.) The `--fmt` command needs vecdsl-cli's jar
(vecdsl-cli/BUILD.md).

## 6. Troubleshooting

- `mvn clean` fails to delete a file under `target`: another process holds it (vecdsl-cli BUILD.md, step 6), for example
  a server still running from an editor. End it, then `Remove-Item -Recurse -Force target` and build again.
- `LspClientTest` fails on `pinecone_demo.vecDSL` only (diagnostics, hover, completion, code action): the sample was
  edited; the expected values belong to the sample as the repository holds it.
- A failsafe failure with "no diagnostics ... within 60 s": the server's standard error of that run is in
  `%TEMP%\vecdsl-lsp-test*\server.stderr`.
