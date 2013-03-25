@echo off
call ccg-env
%JAVA_CMD% opennlp.ccg.realize.hypertagger.TagExtract %*

