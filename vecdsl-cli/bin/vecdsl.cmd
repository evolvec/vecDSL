@echo off
rem Runs target\vecdsl-cli.jar with the java of JAVA_HOME (else the PATH). From JDK 24 on, passes
rem --sun-misc-unsafe-memory-access=allow (JEP 498), so Guice 7.0.0 prints no Unsafe warning; JDK 21 rejects the option.
setlocal
set "VECDSL_JAR=%~dp0..\target\vecdsl-cli.jar"
set "VECDSL_JAVA=java"
if defined JAVA_HOME set "VECDSL_JAVA=%JAVA_HOME%\bin\java.exe"
set "VECDSL_VERSION_FILE=%TEMP%\vecdsl-java-version-%RANDOM%.txt"
"%VECDSL_JAVA%" -version 2> "%VECDSL_VERSION_FILE%"
set "VECDSL_VERSION=0"
for /f "usebackq tokens=2,3" %%u in ("%VECDSL_VERSION_FILE%") do if "%%u"=="version" set "VECDSL_VERSION=%%~v"
del "%VECDSL_VERSION_FILE%" 2>nul
set "VECDSL_MAJOR=0"
for /f "delims=." %%m in ("%VECDSL_VERSION%") do set "VECDSL_MAJOR=%%m"
set "VECDSL_OPTIONS="
if %VECDSL_MAJOR% GEQ 24 set "VECDSL_OPTIONS=--sun-misc-unsafe-memory-access=allow"
"%VECDSL_JAVA%" %VECDSL_OPTIONS% -jar "%VECDSL_JAR%" %*
exit /b %ERRORLEVEL%
