#!/usr/bin/env python

"""
(c) 2008 Dennis N. Mehay
Use this file any way you want, just please give the
author credit if it makes it into any research in any 
meaningful way.  I make no claims whatsoever about the
fitness or merchantability of this code.  Use at
your own risk.

Take a file of CCGbank-style parses and get the words, 
POSs and lexical cat's from them.

We also insert the word as the 'lemma', just as a placeholder.

So we have the following output form (for each parse
in the input file):

<word1>|<word1AsLemma>|<POS1>|<ccg_lexcat1> ... <wordN>|<wordNAsLemma>|<POSN>|<ccg_lexcatN>

Print out parse IDs (if there) as they are.
"""
import sys, re
import optparse

p = optparse.OptionParser()
p.add_option("-i", "--inputf", type="string", \
                 help="Input file to be postprocessed (one parse per line with IDs preceding them) [defaults to stdin]",\
                 default=None)
p.add_option("-o", "--outputf", type="string", \
                 help="The output location [defaults to stdout]",\
                 default=None)

(opts, args) = p.parse_args(sys.argv)

inf = None
outf = None

if opts.inputf is None:
    inf = sys.stdin
else:
    inf = open(opts.inputf,'r')

if opts.outputf is None:
    outf = sys.stdout
else:
    outf = open(opts.outputf,'w')

try:
    global lexNodePattern
    lexNodePattern = re.compile(r'(<L\s.*?>)+?')
    
    """
    A procedure that returns a list of all lexical nodes in a CCGbank-style
    parse tree (in string representation).
    """
    def getLexicalNodes(tree):
        matches = re.findall(lexNodePattern, tree)
        return matches
    
    for l in inf:
        if "ID=" in l:
            print >> outf, l.strip()
        elif l.strip()!='':
            nodes = getLexicalNodes(l.strip())
            ans = ''
            for n in nodes:
                parts = n.split()
                (w,pos,st) = (parts[4],parts[2],parts[1])
                ans += w+'|'+w+'|'+pos+'|'+st+ ' '
            print >> outf, ans.strip()
        
finally:
    if not opts.inputf is None:
        inf.close()
    if not opts.outputf is None:
        outf.close()

    
