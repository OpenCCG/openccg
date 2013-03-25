#!/usr/bin/env python
"""
(c) 2008 Dennis N. Mehay
Use this file any way you want, just please give the
author credit if it makes it into any research in any 
meaningful way.  I make no claims whatsoever about the
fitness or merchantability of this code.  Use at
your own risk.
"""
import sys, math
from optparse import OptionParser as OP

pr = OP()
pr.add_option("-i","--input",type="string",help="input source [default=<stdin>].",\
                  default=sys.stdin)
pr.add_option("-o","--output",type="string",help="output location [default=<stdout>].",\
                  default=sys.stdout)
pr.add_option("-n","--number",type="int",\
                  help="number of times a category must have been seen to retain [default=5]",\
                  default=5)
pr.add_option("-f","--feat_freq",type="int",help="feature frequency cutoff\n"+\
                  "(how frequent must a feature be to retain it? [default=1])",default=1)

(opts,args) = pr.parse_args(sys.argv)

inf = opts.input
if not inf is sys.stdin:
    inf = open(inf,'r')

outf = opts.output
if not outf is sys.stdout:
    outf = open(outf,'w')

try:
    tag_cnt = {}
    ft_cnt = {}
    lines = {}
    
    ln_cnt = -1
    for l in inf:
        l = l.strip()
        ln_cnt += 1
        lines[ln_cnt] = l
        l = l.split()
        tag = l[0]
        tag_cnt[tag] = tag_cnt.get(tag,0) + 1
        feats = l[1:]
        if ':' in feats[0] and not(feats[-1]==':'):
            # real-valued features
            feats = map(lambda a: (a[0:a.rfind(':')],a[a.rfind(':')+1:]), l[1:])

            for (f,act) in feats:
                ft_cnt[f] = ft_cnt.get(f,0) + 1 #math.fabs(float(act))
        else:
            # boolean
            for f in feats:
                ft_cnt[f] = ft_cnt.get(f,0) + 1

    for i in range(ln_cnt + 1):
        l = lines.get(i)
        l = l.strip().split()
        tag = l[0]
        feats = l[1:]
        if tag_cnt.get(tag) >= opts.number:
            tag_printed = False
            if ':' in feats[0] and not(feats[-1]==':'):
                # real-valued features
                feats = map(lambda a: (a[0:a.rfind(':')],a[a.rfind(':')+1:]), l[1:])
                for (f,act) in feats:
                    if ft_cnt.get(f) >= opts.feat_freq:
                        if not tag_printed:
                            print >> outf, tag,
                            tag_printed = True
                        print >> outf, f+':'+act,
            else:
                # boolean
                for f in feats:
                    if not tag_printed:
                        print >> outf, tag,
                        tag_printed = True
                    print >> outf, f,
            print >> outf, ''

        if i%100==0:
            outf.flush()
finally:
    outf.flush()
    if not inf is sys.stdin:
        inf.close()
    if not outf is sys.stdout:
        outf.close()
