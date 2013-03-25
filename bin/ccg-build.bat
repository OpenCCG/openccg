@echo off
call ccg-env
set ANT_HOME=%OPENCCG_LIB%
set PROPS=-Dant.home=%ANT_HOME% -Dopenccg.home=%OPENCCG_HOME%
set XALAN_JARS=%OPENCCG_LIB%\xalan.jar;%OPENCCG_LIB%\xercesImpl.jar;%OPENCCG_LIB%\xml-apis.jar;%OPENCCG_LIB%\xsltc.jar;%OPENCCG_LIB%\serializer.jar
set ANT_JARS=%OPENCCG_LIB%\ant.jar;%OPENCCG_LIB%\ant-launcher.jar;%OPENCCG_LIB%\ant-contrib.jar
set ANT_JARS=%ANT_JARS%;%OPENCCG_LIB%\ant-junit.jar;%OPENCCG_LIB%\ant-junit4.jar;%OPENCCG_LIB%\junit-4.10.jar
set CP="%JAVA_HOME%\lib\tools.jar";%OPENCCG_JAR%;%ANT_JARS%;%XALAN_JARS%;%DIRLIBS%;.
%JAVA% %JAVA_MEM% -classpath %CP% %PROPS% org.apache.tools.ant.launch.Launcher %*
