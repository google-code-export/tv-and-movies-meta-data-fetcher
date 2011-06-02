@echo off

REM Set jvm heap initial and maximum sizes (in megabytes).
set JAVA_HEAP_INIT_SIZE=64
set JAVA_HEAP_MAX_SIZE=192

REM Find a java installation.
if not exist JAVA_HOME goto :javaHomeNotSet
set JAVA=%JAVA_HOME%/bin/java

:doit

REM Main class
set MAIN=org.stanwood.media.cli.manager.CLIMediaManager

REM Locations of libraries
set LIB_DIR=..\lib

REM Setup class path
set CLASSPATH=
for /f %%a IN (‘dir /b %LIB_DIR&/*.jar’) do set CLASSPATH=%CLASSPATH%;%%a 

REM Native lib location
set MM_NATIVE_DIR=..\native

REM Launch app
call %JAVA% -Xms%JAVA_HEAP_INIT_SIZE%M -Xmx%JAVA_HEAP_MAX_SIZE%M -classpath %CLASSPATH% %MAIN% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto :exit

:javaHomeNotSet

echo "Warning: JAVA_HOME environment variable not set! Consider setting it."
echo "          Attempting to use java on the path..."

set JAVA=java

goto :doit

:exit