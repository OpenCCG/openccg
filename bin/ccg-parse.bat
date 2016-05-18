@echo off
rem Usage: ccg-parse -h
call ccg-env
%JAVA_CMD% opennlp.ccg.Parse %1 %2 %3 %4 %5 %6 %7 %8 %9 

