@echo off

REM Set jvm heap initial and maximum sizes (in megabytes).
set JAVA_HEAP_INIT_SIZE=64
set JAVA_HEAP_MAX_SIZE=192

REM Find a java installation.
if not exist JAVA_HOME goto :javaHomeNotSet
set JAVA=%JAVA_HOME%/bin/java

:doit

REM Main class
set MAIN=org.stanwood.media.source.xbmc.cli.CLIManageAddons

REM Locations of libraries
set LIB_DIR=..\lib

REM Setup class path
set CLASSPATH=%LIB_DIR%\MediaManager-1.98.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\antlr-runtime-3.1.3.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\commons-cli-1.1.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\commons-exec-1.1.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\commons-lang-2.6.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\commons-logging-1.0.4.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\isoparser-svn.r145.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\jdom.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\log4j-1.2.15.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\mysql-connector-java-5.1.6.jar
set CLASSPATH=%CLASSPATH%;%LIB_DIR%\rome-1.0.jar

REM Launch app
call %JAVA% -Xms%JAVA_HEAP_INIT_SIZE%M -Xmx%JAVA_HEAP_MAX_SIZE%M -classpath %CLASSPATH% %MAIN% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto :exit

:javaHomeNotSet

echo "Warning: JAVA_HOME environment variable not set! Consider setting it."
echo "          Attempting to use java on the path..."

set JAVA=java

goto :doit

:exit