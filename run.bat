@echo off
rem Compile and run the simulator from a Windows terminal. Requires JDK 11+ (17+ recommended).
cd /d "%~dp0"
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
del sources.txt
java -cp bin main.Main
