@echo off
rem For usage, do: ccg-cvr -h
call ccg-env
%JAVA_CMD% opennlp.ccg.test.CrossValidateRealizer %* 

