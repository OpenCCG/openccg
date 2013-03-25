#!/usr/bin/env python
"""
Takes a file of word-tag pairs or word-tag-stem triples and writes an xml morph file.

(c) 2010 Michael White (modifed by D.N. Mehay 2011)
[insert LGPL here]
"""

import sys
from optparse import OptionParser as OP
from xml.sax import saxutils

global sem_classes
sem_classes = set(["PERSON", "ORGANIZATION", "LOCATION", "MONEY", "PERCENT", "TIME", "DATE"])

pr = OP()
pr.add_option("-i","--input",type="string",help="input source [default=<stdin>]",\
                  default=sys.stdin)
pr.add_option("-o","--output",type="string",help="output location [default=<stdout>]",\
                  default=sys.stdout)

(opts,args) = pr.parse_args(sys.argv)

inf = opts.input
if not inf is sys.stdin:
    inf = open(inf,'r')

outf = opts.output
if not outf is sys.stdout:
    outf = open(outf,'w')

try:
    print >> outf, '<morph name="novel">'
    for l in inf:
        l = l.strip()
        l = l.split()
        s = ['<entry word="']
        s.append(saxutils.escape(l[0]))
        s.append('" pos="')
        s.append(l[1])
        s.append('"')
        # added by DNM (02 Nov 2011)
        if len(l) >= 3:
            if l[2] in sem_classes:
                s.append(' class="')
                s.append(saxutils.escape(l[2]))
                s.append('"')
            else:
                s.append(' stem="')
                s.append(saxutils.escape(l[2]))
                s.append('"')
        if len(l) >= 4:
            s.append(' stem="')
            s.append(saxutils.escape(l[3]))
            s.append('"')

finally:
    if not inf is sys.stdin:
        inf.close()
    if not outf is sys.stdout:
        outf.close()
