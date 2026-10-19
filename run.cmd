@echo off
setlocal
cd /d "%~dp0"
chcp 65001 >nul
call mvnw.cmd --no-transfer-progress verify
if errorlevel 1 exit /b 1
java -Dfile.encoding=UTF-8 -jar target\habit-tracker-1.0.0.jar
exit /b %errorlevel%
