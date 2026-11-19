# Runs target\vecdsl-lsp.jar with the java of JAVA_HOME (else the PATH). From JDK 24 on, passes
# --sun-misc-unsafe-memory-access=allow (JEP 498), so Guice 7.0.0 prints no Unsafe warning; JDK 21 rejects the option.
# For the command line (version); an editor starts java itself, since PowerShell does not pass a native
# program's standard input and output through byte for byte.
$ErrorActionPreference = 'Continue'
$jar = Join-Path $PSScriptRoot '..\target\vecdsl-lsp.jar'
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }
$versionLine = & $java -version 2>&1 | ForEach-Object { "$_" } | Where-Object { $_ -match ' version "' } | Select-Object -First 1
$major = 0
if ($versionLine -match ' version "(\d+)') { $major = [int]$Matches[1] }
$options = @()
if ($major -ge 24) { $options += '--sun-misc-unsafe-memory-access=allow' }
& $java @options -jar $jar @args
exit $LASTEXITCODE
