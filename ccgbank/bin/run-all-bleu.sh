#!/bin/bash
# run Bleu/NIST on all systems sys directory (passed in) with all references in the ref directory
# (also passed in as an arg).
# $1 is the mteval script.
# $2 is the location of the system directory (where all the system files are stored)
# $3 is the location of the reference directory (where all the reference files are stored).
# $4 is the location of the source *file*.
# $5 is the location where the scores will go.
curr_dir=`dirname $0`
for sys in `ls $2/E*`
do
    for ref in `ls $3/*`
    do
	echo "Command line: $1 --metricsMATR -t $sys -r $ref -s $4"
	sys_shortname=`basename ${sys} | sed "s/^\(E[0-9][0-9]\).*/\1/g"`
	ref_shortname=`basename ${ref} | sed "s/^\(E[0-9][0-9]\).*/\1/g"`
	$1 --metricsMATR -t $sys -r $ref -s $4
	cat BLEU-seg.scr | python $curr_dir/post-process-metricsmatr.py ${ref_shortname} > $5/BLEU.${sys_shortname}-${ref_shortname}.scr
	cat NIST-seg.scr | python $curr_dir/post-process-metricsmatr.py ${ref_shortname} > $5/NIST.${sys_shortname}-${ref_shortname}.scr
	rm BLEU-*; rm NIST-*;
    done
done
