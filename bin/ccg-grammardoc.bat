@echo off
rem Usage: ccg-grammardoc [-s|--source sourceDir] [-d|--dest destDir]
call ccg-env
set ANT_HOME=%OPENCCG_HOME%\lib
set CP=%CP%;%ANT_HOME%\ant.jar
set JAVA_ARGS=-Xmx128m -classpath %CP%
%JAVA% %JAVA_ARGS% opennlp.ccg.grammardoc.GrammarDoc %*

