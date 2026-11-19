# Building vecdsl-cli

The build reads the sibling bundle of the repository (org.ftn.vecDSL's `src`, `src-gen`, `xtend-gen`; the coverage
index is part of `src`), so run it from a clone of the repository. Two steps read files that
other tools write: step 3 compares the jar with `org.ftn.vecDSL\bin`, which Eclipse writes (F5, Project > Clean), and
the tests and step 5 read `org.ftn.vecDSL.tests/generated`, which the test project writes (Eclipse JUnit run or
`mvn -f ci/tests/pom.xml verify`). The commands are for Windows PowerShell from the repository root; on Linux and
macOS use the same Maven command and `bin/vecdsl.sh`.

## 1. Prerequisites

```powershell
java -version      # 21 or later
mvn -v             # Maven 3.9 or later
```

The compiler is set to release 21, so any JDK from 21 on builds the same jar. Java 21 is the runtime the jar is built
for. On Java 24 and later Maven prints, for its own copy of Guice, four lines starting with
`WARNING: A terminally deprecated method in sun.misc.Unsafe has been called`; they belong to Maven and change nothing.
The jar's own copy of that warning is handled by the launchers of step 4.

## 2. Build

```powershell
Set-Location vecdsl-cli
mvn -q clean package
```

Expected: no output (on Java 24 and later, only Maven's four `WARNING:` lines) and `$LASTEXITCODE` 0. Maven compiles
org.ftn.vecDSL's sources with the CLI, runs the 21 JUnit tests (`ArgsTest` 8, `VecdslTest` 13) and writes
**`vecdsl-cli\target\vecdsl-cli.jar`** (the shaded, runnable jar; `original-vecdsl-cli.jar` beside it is the unshaded
one). The first build downloads the dependencies of `pom.xml` from Maven Central. If a test fails,
`mvn package` without `-q` shows which; `mvn -q package -DskipTests` still builds the jar.

## 3. Jar listing against bin

The jar must carry every non-Java file of org.ftn.vecDSL's `src` and `src-gen`, which are the non-class files of
`org.ftn.vecDSL\bin` except the Xtend builder's `.xtendbin` and `._trace` files, since the generators read their templates and properties as class resources. The CLI's own
resource (`vecdsl-cli.properties`) sits under `org/ftn/vecDSL/cli/`, so the listing leaves that folder out. The 14
coverage JSONs are files of org.ftn.vecDSL's `src` (`org/ftn/vecDSL/coverage/index/`), so they are counted on both
sides. Run from the repository root:

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$wsRoot = (Get-Location).Path
$zip = [System.IO.Compression.ZipFile]::OpenRead("$wsRoot\vecdsl-cli\target\vecdsl-cli.jar")
$inJar = $zip.Entries | Where-Object { $_.FullName -like 'org/ftn/vecDSL/*' -and $_.FullName -notlike 'org/ftn/vecDSL/cli/*' -and $_.Name -ne '' -and $_.FullName -notlike '*.class' } |
    ForEach-Object { $_.FullName } | Sort-Object
$zip.Dispose()
$binDir = "$wsRoot\org.ftn.vecDSL\bin"
$inBin = Get-ChildItem $binDir -Recurse -File |
    Where-Object { $_.Extension -ne '.class' -and $_.Name -notlike '*.xtendbin' -and $_.Name -notlike '*._trace' } |
    ForEach-Object { $_.FullName.Substring($binDir.Length + 1).Replace('\', '/') } | Sort-Object
"jar: $($inJar.Count), bin: $($inBin.Count)"
Compare-Object -CaseSensitive $inJar $inBin
```

Expected: `jar: 309, bin: 309` (more if files were added to `src`, but equal), and no line from `Compare-Object`. `bin`
holds the coverage JSONs only after Eclipse has rebuilt org.ftn.vecDSL (F5). A line with `=>` is a file of `bin`
missing from the jar: `gen` would fail at run time.

## 4. Smoke test

```powershell
bin\vecdsl.cmd version
bin\vecdsl.cmd check ..\spec\negative\wf_recall_one.vecDSL
```

Expected:

```
language: vecDSL 1.0 (1.0.1 documentation patch)
IR: 1.0
grammar namespace: http://www.ftn.org/vecDSL/VecDSL/7
vecdsl-cli: 0.1.0 (implements vecDSL >=1.0,<2.0)
..\spec\negative\wf_recall_one.vecDSL:14:104: error wf.recall.one target-recall must be less than 1; recall 1 is written 'exact'
1 program, 1 error, 0 warnings
```

The second command exits with 1 (an error was found). If it prints `0 errors` instead, or a `MissingResourceException`
on standard error, the shaded jar lost EMF's `plugin.properties` (the shade configuration in `pom.xml` must keep it). Through the launcher, standard
error stays empty on every Java version; `java -jar target\vecdsl-cli.jar version` on Java 24 or later prints the same
output plus four `WARNING:` lines on standard error. `bin\vecdsl.ps1` does the same as `vecdsl.cmd`
where the PowerShell execution policy allows running scripts.

## 5. Generation against generated/

`src/test/scripts/compare-generated.sh` (Git Bash, Linux, macOS) generates every sample with
`gen --target <vendor> --lang all` and compares each file with `org.ftn.vecDSL.tests/generated/<vendor>` (demo
samples) and `<vendor>-full` (full samples). Expected: every file identical, none missing; the test project's extra
files (books.jsonl, run and server logs, build output) are counted, not compared. From the repository root:

```
bash vecdsl-cli/src/test/scripts/compare-generated.sh .
```

The CI job `samples` runs the same comparison (`ci/samples.sh`).

## 6. Troubleshooting

- `mvn clean` fails with "Failed to delete ...\target\classes\...": another process holds a file of `target` open (an
  Explorer window, the search indexer or an antivirus scan are the usual ones). Delete the folder and build again:

  ```powershell
  Remove-Item -Recurse -Force target
  mvn -q clean package
  ```

- `Unrecognized option: --sun-misc-unsafe-memory-access=allow`: a JDK older than 23 was given the option by hand; the
  launchers add it only from 24 on.
- `jar: N, bin: M` with N greater than M: `bin` is older than `src` (refresh org.ftn.vecDSL in Eclipse with F5), or the
  listing filter does not leave out `org/ftn/vecDSL/cli/`.
