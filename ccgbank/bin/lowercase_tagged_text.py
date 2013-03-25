#!/usr/bin/env python
"""
Takes a POS-tagged file and writes out the text with tokens lowercased except for
proper nouns.  A file with the list of word-tag pairs can also be written out.

(c) 2010 Michael White
[insert LGPL here]
"""

import sys
from optparse import OptionParser as OP

pr = OP()
pr.add_option("-i","--input",type="string",help="input source [default=<stdin>]",\
                  default=sys.stdin)
pr.add_option("-o","--output",type="string",help="output location [default=<stdout>]",\
                  default=sys.stdout)
pr.add_option("-p","--pairs",type="string",help="output file for word-tag pairs",\
                  default=None)

(opts,args) = pr.parse_args(sys.argv)

inf = opts.input
if not inf is sys.stdin:
    inf = open(inf,'r')

outf = opts.output
if not outf is sys.stdout:
    outf = open(outf,'w')

pairsf = opts.pairs
if not pairsf is None:
    pairsf = open(pairsf,'w')
    
try:
    sent = []
    pairs = []
    for l in inf:
        l = l.strip()
        l = l.split()
        if l[0] == '<s>':
            sent = []
            pairs = []
        elif l[0] == '</s>':
            print >> outf, ' '.join(sent)
            if not pairsf is None:
                for (token,tag) in pairs:
                    print >> pairsf, token, tag
        else:
            token,tag = l[0],l[1]
            if tag[:3] != 'NNP' and (len(token) <= 1 or not token[1].isupper()):
                token = token.lower()
            sent.append(token)
            pairs.append((token,tag))
            if len(l) >= 4:
                tag2 = l[3]
                pairs.append((token,tag2))
finally:
    if not inf is sys.stdin:
        inf.close()
    if not outf is sys.stdout:
        outf.close()
    if not pairsf is None:
        pairsf.close()
