"""
Given two files: (1) the output of Stanford's re-implementation of 'morpha' and (2) the 'pairs' file of <word><space><POS>(<space><SEMCLASS>),
merge them into a morph.xml file.
"""
import sys, codecs, os
from optparse import OptionParser as OP
from xml.sax import saxutils

pr = OP()
pr.add_option("-m","--morpha_input",type="string",help="morpha input file (required arg)",\
                  default=None)
pr.add_option("-p","--pairs_input",type="string",help="pairs input file (required arg)",\
                  default=None)
pr.add_option("-o","--output",type="string",help="output location [default=<stdout>]",\
                  default=sys.stdout)

(opts,args) = pr.parse_args(sys.argv)

# we do not check that you passed in the files (this is intended for internal use only, not as a
# user-friendly app).
pinf = codecs.open(opts.pairs_input,'rb','utf-8')
minf = codecs.open(opts.morpha_input,'rb','utf-8')

outf = opts.output
if not outf is sys.stdout:
    outf = codecs.open(outf,'wb','utf-8')
else:
    streamWriter = codecs.lookup("utf-8")[-1]
    outf = streamWriter(sys.stdout)

pl = pinf.readline()
ml = minf.readline()

outf.write('<?xml version="1.0" encoding="UTF-8"?>' + '\n')
outf.write('<morph name="novel">' + '\n')

entries = []

try:
    while pl and ml:
        pl = pl.strip()
        ml = ml.strip()
        pl = pl.split()
        ml = ml.split()
        # skip blank lines from line ending differences
        if len(pl) < 2: 
            pl = pinf.readline()
            ml = minf.readline()        
            continue
        s = ['<entry word="']
        s.append(saxutils.escape(pl[0]))
        s.append('" pos="')
        s.append(pl[1])
        s.append('"')
        if len(pl) > 2:
            s.append(' class="')
            s.append(saxutils.escape(pl[2]))
            s.append('"')
        if ml[0].lower() != pl[0].lower() and not ("^" in ml[0] or "*****" in ml[0]): # add stem only if distinct.
            s.append(' stem="')
            s.append(saxutils.escape(ml[0]))
            s.append('"')
        s.append('/>')
        entries.append(s[:])
        pl = pinf.readline()
        ml = minf.readline()        

    # sort/uniq
    entries.sort()
    last_one = None

    for e in entries:
        if last_one is None or e != last_one:
            outf.write(u''.join(e) + '\n')
            last_one = e
            
    outf.write('</morph>' + '\n')
finally:
    pinf.close()
    minf.close()
    if not outf is sys.stdout:
        outf.close()
        
