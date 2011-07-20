@echo off

REM Set jvm heap initial and maximum sizes (in megabytes).
set JAVA_HEAP_INIT_SIZE=64
set JAVA_HEAP_MAX_SIZE=192

REM Find a java installation.
if not exist JAVA_HOME goto :javaHomeNotSet
set JAVA=%JAVA_HOME%/bin/java

:doit

REM Launch app
call %JAVA% -Xms%JAVA_HEAP_INIT_SIZE%M -Xmx%JAVA_HEAP_MAX_SIZE%M  %1 %2 %3 %4 %5 %6 %7 %8 %9

goto :exit

:javaHomeNotSet

echo "Warning: JAVA_HOME environment variable not set! Consider setting it."
echo "          Attempting to use java on the path..."

set JAVA=java

goto :doit

:exit