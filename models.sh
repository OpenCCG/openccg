#! /bin/sh

FILE=/openccg/ccgbank/english-models.tgz

if [ -f "$FILE" ]; then
  ccg-build -f ccgbank/build-release.xml extract-models
else
  echo "$FILE does not exist, skipping this step"
fi
