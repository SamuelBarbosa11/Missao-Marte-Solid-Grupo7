@echo off
setlocal

if /I "%~1"=="start" goto start

echo Uso: run start
exit /b 1

:start
chcp 65001 >nul
if not exist out mkdir out

javac -encoding UTF-8 -d out ^
  src\solidexercicio10\*.java ^
  src\solidexercicio10\model\*.java ^
  src\solidexercicio10\presentation\*.java ^
  src\solidexercicio10\repository\*.java ^
  src\solidexercicio10\service\*.java

if errorlevel 1 exit /b 1

java -Dfile.encoding=UTF-8 -cp out solidexercicio10.Main
