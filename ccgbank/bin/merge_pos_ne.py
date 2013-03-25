#!/usr/bin/env python
"""
Takes a POS-tagged file and a file of the original, NE-tagged text and writes out a file of:

...
[word]<whitespace(s)>[POS]<whitespace(s)>[SEM_CLASS](if any)<whitespace(s)>[stem](if any)
...

(c) 2011 Dennis N. Mehay
[insert LGPL here]
"""

class POSOutputIter:
    def __init__(self, filelikeobj):
        self.f = filelikeobj

    def __iter__(self): return self
    
    def next(self):
        try:
            nxtLn = self.f.readline().strip()
            nxtSent = []
            if nxtLn != "<s>":
                raise StopIteration
            nxtLn = self.f.readline().strip()
            while nxtLn != "</s>":
                nxtSent.append(nxtLn)
                nxtLn = self.f.readline().strip()
            return nxtSent
        except:
            raise StopIteration
        
import sys, codecs, os
from optparse import OptionParser as OP

pr = OP()
pr.add_option("-p","--pos_in",type="string",help="POS-tagged input",\
                  default=None)
pr.add_option("-n","--ner_tagged_in",type="string",help="NE-tagged input (no POS tags yet)",\
                  default=None)
pr.add_option("-o","--output",type="string",help="output location [default=<stdout>]",\
                  default=sys.stdout)

(opts,args) = pr.parse_args(sys.argv)

pinf = opts.pos_in
if not pinf is sys.stdin:
    pinf = codecs.open(pinf,'r', 'utf-8')

ninf = opts.ner_tagged_in
if not ninf is sys.stdin:
    ninf = codecs.open(ninf,'r', 'utf-8')
    
outf = opts.output
if not outf is sys.stdout:
    outf = codecs.open(outf,'wb', 'utf-8')
else:
    streamWriter = codecs.lookup("utf-8")[-1]
    outf = streamWriter(sys.stdout)

try:
    for posSent in POSOutputIter(pinf):
        origSent = ninf.readline()        
        for (posTW,NETagW) in zip(posSent, origSent.split()):
            NETagWParts = NETagW.split(u"_")

            if len(NETagWParts) > 1:
                NETag = u"\t" + NETagWParts[-1]
            else:
                NETag = ""

            posTW = posTW.split()
            w = posTW[0]
            
            tgs = posTW[1:][::2][:2]
            for t in tgs:
                outf.write(w + u"\t" + t + NETag + '\n')
        

finally:
    pinf.close()
    ninf.close()
    outf.close()
