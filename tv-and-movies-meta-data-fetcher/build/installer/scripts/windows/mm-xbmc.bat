@echo off

REM Set jvm heap initial and maximum sizes (in megabytes).
set JAVA_HEAP_INIT_SIZE=64
set JAVA_HEAP_MAX_SIZE=192

REM Find a java installation.
if not exist %JAVA_HOME% goto javaHomeNotSet
set JAVA=%JAVA_HOME%/bin/java

:doit

REM Main class
set MAIN=org.stanwood.media.source.xbmc.cli.CLIManageAddons

REM Locations of libraries
set LIB_DIR=..\libs

REM Setup class path
set CLASSPATH=/usr/share/java/MediaInfoFetcher.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/jakarta-commons-cli.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/jakarta-commons-lang.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/jakarta-commons-exec.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/jakarta-commons-logging.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/jakarta-commons-exec.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/isoparser.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/log4j.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/jdom.jar
set CLASSPATH=%CLASSPATH%:%LIB_DIR%/ROME.jar

REM Launch app
call %JAVA% -Xms%JAVA_HEAP_INIT_SIZE% -Xmx%JAVA_HEAP_MAX_SIZE%M -classpath %CLASSPATH% %MAIN% "$@"

exit

:javaHomeNotSet

echo "Warning: JAVA_HOME environment variable not set! Consider setting it."
echo "          Attempting to use java on the path..."

set JAVA=java        

goto doit