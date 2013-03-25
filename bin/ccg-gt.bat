@echo off
rem For usage, do: ccg-gt -h
call ccg-env
%JAVA_CMD% opennlp.ccg.test.GenTargets %*

