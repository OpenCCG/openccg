@echo off
rem For usage, do: ccg-update -h
call ccg-env
%JAVA_CMD% opennlp.ccg.test.UpdateTestbed %*

