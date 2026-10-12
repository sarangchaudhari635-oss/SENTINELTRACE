@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "DIR=%~dp0"

@REM Locate Java
if defined JAVA_HOME goto checkJava
for /d %%I in ("C:\Program Files\Microsoft\jdk-21*" "C:\Program Files\Eclipse Adoptium\jdk-21*" "C:\Program Files\Java\jdk-21*") do (
    if exist "%%~I\bin\java.exe" (
        set "JAVA_HOME=%%~I"
        goto checkJava
    )
)
for /d %%I in ("C:\Program Files\Microsoft\jdk-*" "C:\Program Files\Eclipse Adoptium\jdk-*" "C:\Program Files\Java\jdk-*") do (
    if exist "%%~I\bin\java.exe" (
        set "JAVA_HOME=%%~I"
        goto checkJava
    )
)

:checkJava
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    echo Please set JAVA_HOME to point to your JDK 21 installation.
    exit /b 1
)

@REM Check if maven is downloaded in wrapper cache
set "MAVEN_VERSION=3.9.9"
set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
set "MVN_CMD=%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd"

if exist "%MVN_CMD%" goto runMaven

@REM Try system mvn if installed
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    call mvn %*
    exit /b %ERRORLEVEL%
)

@REM Download Maven 3.9.9
echo Downloading Apache Maven %MAVEN_VERSION%...
if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%"
set "ZIP_FILE=%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%-bin.zip"
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%ZIP_FILE%'"
if %ERRORLEVEL% neq 0 (
    echo Failed to download Maven.
    exit /b 1
)

echo Extracting Apache Maven %MAVEN_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%MAVEN_DIR%' -Force"

:runMaven
call "%MVN_CMD%" %*
exit /b %ERRORLEVEL%
