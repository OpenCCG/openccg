@echo off
rem Usage: ccg-realize (-g <grammarfile>) <inputfile> (<outputfile>)
call ccg-env
rem set HPROF=-Xrunhprof:cpu=times,file=hmm-prof.txt
%JAVA_CMD% opennlp.ccg.Realize %1 %2 %3 %4 %5 %6 %7 %8 %9 

