#!/bin/bash
#
# Tag a file $1 using Stanford NER located in base directory $2 (first arg, e.g., "/home/me/stanford-ner-2.1.0") 
# with model $3 (second arg, e.g., "all.3class.distsim.crf.ser.gz").
#
# Output is placed in the file whose path is given in argument $4.
#
java -mx700m -cp "$1/stanford-ner.jar:`dirname $0`/NERApp.jar" nerapp.NERApp $2/classifiers/$3 $1 2> /dev/null | python `dirname $0`/post-process-stanford-ner.py > $4
