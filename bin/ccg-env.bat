@echo off
rem sets OpenCCG environment variables

if not exist "%JAVA_HOME%" goto no_JAVA_HOME
if not exist "%OPENCCG_HOME%" goto no_OPENCCG_HOME

set OPENCCG_LIB=%OPENCCG_HOME%\lib
set DIRLIBS=%OPENCCG_LIB%\trove.jar;%OPENCCG_LIB%\jdom.jar;%OPENCCG_LIB%\jline.jar;%OPENCCG_LIB%\jopt-simple.jar
set XMLLIBS=%OPENCCG_LIB%\xml-apis.jar;%OPENCCG_LIB%\xercesImpl.jar;%OPENCCG_LIB%\xalan.jar
set OPENCCG_SRC=%OPENCCG_HOME%\src
set OPENCCG_CLASSES=%OPENCCG_HOME%\output\classes
set OPENCCG_JAR=%OPENCCG_HOME%\lib\openccg.jar
rem variant without XMLLIBS
rem set CP=%OPENCCG_JAR%;%DIRLIBS%;.
rem variant with XMLLIBS
set CP=%OPENCCG_JAR%;%DIRLIBS%;%XMLLIBS%;.
rem variant for use with 'build compile' option, if desired:
rem set CP=%OPENCCG_CLASSES%;%OPENCCG_SRC%;%DIRLIBS%
set JAVA="%JAVA_HOME%\bin\java"
set JAVA_MEM=-Xmx256m
rem set JAVA_MEM=-Xmx2048m
set JAVA_CMD=%JAVA% %JAVA_MEM% -classpath %CP% -Dfile.encoding=UTF8

goto end

:no_JAVA_HOME
echo.
echo Error: JAVA_HOME not found in your environment.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.
echo.
exit /b 1

:no_OPENCCG_HOME
echo.
echo Error: OPENCCG_HOME not found in your environment.
echo.
echo Please set the OPENCCG_HOME variable in your environment to match the
echo location of your OpenNLP CCG Library distribution.
echo.
exit /b 1

:end
