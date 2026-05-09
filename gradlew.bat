@echo off
rem
rem Gradle wrapper script (Windows).
rem
set DIR=%~dp0
set JAVA_EXE=java
if not "%JAVA_HOME%"=="" set JAVA_EXE=%JAVA_HOME%\bin\java

"%JAVA_EXE%" -jar "%DIR%\gradle\wrapper\gradle-wrapper.jar" %*

