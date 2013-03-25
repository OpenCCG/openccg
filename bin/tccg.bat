@echo off
rem For usage, do: tccg -h
call ccg-env
%JAVA_CMD% opennlp.ccg.TextCCG %*

