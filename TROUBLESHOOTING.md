# Troubleshooting

Known problems when building, testing or running vecDSL, and what to do about each.

- **`org.ftn.vecDSL.ui.tests` fails with `NoClassDefFoundError: org/eclipse/e4/ui/workbench/IWorkbench`.** It was run
  as a plain JUnit Test. Run it as a JUnit Plug-in Test: the launch configuration's Test tab with "Run in UI thread"
  checked and the JUnit 5 runner, the Main tab with "Run an application" set to `org.eclipse.ui.ide.workbench`. A
  JUnit Plug-in Test starts a second Eclipse: the first start can take a few minutes, and its window may open behind
  the first one. The SLF4J "No SLF4J providers were found" lines and the warnings about `javax.annotation` in the
  console are harmless.
- **The parser rejects a file written by Windows PowerShell 5.1** with "extraneous input '?' expecting 'script'".
  `Set-Content -Encoding UTF8` and `Out-File` in PowerShell 5.1 write a byte order mark. Write the file without one,
  for example `[System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))`, or use
  PowerShell 7, whose UTF8 encoding has no byte order mark.
- **The extension does not build.** vscode-vecdsl needs Node.js 22.13 or later (`node -v`): `@vscode/vsce`,
  `@vscode/test-electron` and ESLint refuse older versions.
- **`CoverageIndexTest` fails** after a change to a `COVERAGE.md`: rebuild the index (README.md, "Coverage matrices") and press F5 on `org.ftn.vecDSL`.
- **Java 24 or later prints `WARNING: A terminally deprecated method in sun.misc.Unsafe`** when the jars are run with
  plain `java -jar`. Guice 7.0.0 causes it; the launchers in `vecdsl-cli/bin` and `vecdsl-lsp/bin` add
  `--sun-misc-unsafe-memory-access=allow` from Java 24 on.
- **`mvn` in Windows PowerShell 5.1 prints red `NativeCommandError` lines** with `WARNING: A terminally deprecated
  method in sun.misc.Unsafe has been called ... com.google.inject.internal.aop.HiddenClassDefiner`. Maven's own copy of
  Guice prints this on Java 24 and later, and PowerShell 5.1 shows anything a program writes to standard error as an
  error. The build is not affected: `$LASTEXITCODE` 0 and `BUILD SUCCESS` (without `-q`) mean it passed. PowerShell 7
  shows the same lines without the error wrapper.
- **`mvn -f ci/tests/pom.xml clean test` reports `NoClassDefFoundError: org/junit/Assert`** in `VecDSLFormatterTest`.
  Xtext's `FormatterTestHelper` calls JUnit 4, which Eclipse provides but `org.eclipse.xtext.testing` does not bring
  along; `ci/tests/pom.xml` declares `junit:junit` 4.13.2 for it. A copy of the pom without that dependency shows
  this error.
