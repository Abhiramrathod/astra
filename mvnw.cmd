@REM ----------------------------------------------------------------------------
@REM Maven Wrapper - Start Up Batch script
@REM ----------------------------------------------------------------------------

@if "%MAVEN_DEBUG%" == "" @echo off
@REM set title of command window
title %0
@if "%MAVEN_BATCH_ECHO%" == "on" echo %MAVEN_BATCH_ECHO%

if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

if not "%MAVEN_SKIP_RC%" == "" goto skipRcPre
if exist "%USERPROFILE%\mavenrc_pre.bat" call "%USERPROFILE%\mavenrc_pre.bat" %*
:skipRcPre

set ERROR_CODE=0

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome
echo Error: JAVA_HOME not found in your environment. >&2
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto chkMHome
echo Error: JAVA_HOME is set to an invalid directory. >&2
goto error

:chkMHome
set "MAVEN_PROJECTBASEDIR=%cd%"
if not "%M2_HOME%" == "" set "MAVEN_HOME=%M2_HOME%"

:setMHome
set "WRAPPER_JAR=%~dp0\.mvn\wrapper\maven-wrapper.jar"
if exist "%WRAPPER_JAR%" goto run
echo Error: Maven wrapper JAR not found at %WRAPPER_JAR% >&2
goto error

:run
"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1
:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
if not "%MAVEN_SKIP_RC%" == "" goto skipRcPost
if exist "%USERPROFILE%\mavenrc_post.bat" call "%USERPROFILE%\mavenrc_post.bat" %*
:skipRcPost
exit /B %ERROR_CODE%
