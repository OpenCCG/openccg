"""
Takes NE tagged text from stdin (assuming utf-8) and does just what it says: prints to stdout only the words.
"""
import codecs, sys, os
from optparse import OptionParser as OP

pr = OP()
pr.add_option("-o","--output",type="string",help="output location [default=<stdout>].",\
                  default=sys.stdout)

(opts,args) = pr.parse_args(sys.argv)

outf = opts.output
if not outf is sys.stdout:
    outf = open(outf,'w')

streamWriter = codecs.lookup("utf-8")[-1]
outw = streamWriter(outf)

for l in sys.stdin:
    l = l.decode("utf-8")
    parts = l.split()
    snt = []
    for p in parts:
        if u"_" in p:
            subparts = p.split(u"_")
            if len(subparts) > 1:
                w = u"_".join(subparts[:-1])
            else:
                w = p
            snt.append(w)
        else:
            snt.append(p)
    outw.write(u" ".join(snt) + '\n')

outf.flush()
if not outf is sys.stdout:
    outf.close()
