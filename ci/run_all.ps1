# Runs ci/run_all.sh with the bash of Git for Windows, from Windows PowerShell 5.1 or PowerShell 7; arguments are step
# names (none: every step). The exit status is run_all.sh's. Java, Maven, Node.js and Python must be on the PATH.
$ErrorActionPreference = 'Stop'
$git = Get-Command git.exe -ErrorAction SilentlyContinue
if ($null -eq $git) { throw 'git.exe is not on the PATH: install Git for Windows, which provides bash.exe' }
$folder = Split-Path $git.Source
$bash = $null
while ($folder -and -not $bash) {
    $folder = Split-Path $folder
    if ($folder -and (Test-Path (Join-Path $folder 'bin\bash.exe'))) { $bash = Join-Path $folder 'bin\bash.exe' }
}
if ($null -eq $bash) { throw "no bin\bash.exe above $($git.Source)" }
$script = (Join-Path $PSScriptRoot 'run_all.sh') -replace '\\', '/'
& $bash $script @args
exit $LASTEXITCODE
