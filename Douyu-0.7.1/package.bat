@echo off

rem xcopy /?
rem rd /?

rem call mvn clean package -Dmaven.test.skip=true

set version=0.7.1

call mvn package -Dmaven.test.skip=true

if exist "target" goto ok1
mkdir target\classes
goto ok2
:ok1
rd /Q /S target
mkdir target\classes
:ok2

xcopy /S /Y /Q douyu-api\target\classes target\classes
xcopy /S /Y /Q douyu-javac\target\classes target\classes
xcopy /S /Y /Q douyu-core\target\classes target\classes
xcopy /S /Y /Q douyu-mvc\target\classes target\classes
xcopy /S /Y /Q douyu-http\target\classes target\classes
xcopy /S /Y /Q douyu-plugins\target\classes target\classes
xcopy /S /Y /Q douyu-netty\target\classes target\classes
xcopy /S /Y /Q douyu-startup\target\classes target\classes
xcopy /S /Y /Q douyu-logging\target\classes target\classes

rem rd /Q /S target\classes\javax

jar cf target\douyu-%version%.jar -C target\classes .

