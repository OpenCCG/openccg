"""
Requires Python >= 2.6x+ but < 3.0.

Takes in a stream (from stdin) or file of *tokenized* plain text (utf-8),
returns a list of words that occurred more than twice and were in upper-case
more frequently than not.
"""
import optparse, sys, codecs, os
from collections import defaultdict

def isAllUpper(st):
    return ( st.upper() == st and st.lower() != st )

op = optparse.OptionParser()

op.add_option("-i", "--input", type="string", help="input file or stream (default = <stdin>)",
              default=sys.stdin)
op.add_option("-o", "--output", type="string", help="output file or stream (default = <stdout>)",
              default=sys.stdout)
op.add_option("-f", "--use_first", action="store_true", help="whether to use the first word of each "+\
              "sentence for counting uppercase vs. lowercase (default = False)", default=False)

(ops, args) = op.parse_args()

inf = ops.input
if not inf is sys.stdin:
    inf = open(inf, "rb")

use_first_word   = ops.use_first

outf = ops.output
if not outf is sys.stdout:
    outf = codecs.open(outf, "wb", "utf-8")
else:
    # make stdout code utf-8
    streamWriter = codecs.lookup("UTF-8")[-1]
    outf = streamWriter(outf)

# map from: lowercased_word_key => specific_cased_form => count
wds2ulcounts = defaultdict(lambda: defaultdict(lambda: 0))

try:
    l = inf.readline()

    while l:
        l = l.strip().decode("utf-8")
        if l == u"":
            continue
    
        words = l.split()
    
        if not use_first_word:
           words = words[1:]
    
        for w in words:
            key = w.lower()
            wds2ulcounts[key][w] += 1
                
        l = inf.readline()
    
    for (wdkey,frms) in wds2ulcounts.items():
        wdforms = frms.items()
        # sum all counts. if more than 2, then write out the most frequent, else don't.
        sum_all = sum([cnt for (wf,cnt) in wdforms])
        # if there is only one form, seen more than once and it is a cased form, print it (this last will avoid printing punctuation and
        # always-lowercase words like 'the').
        if len(wdforms) == 1:
            most_freq = wdforms[0]            
            if sum_all > 2 and most_freq[0].lower() != most_freq[0]:
                #outf.write("wd %s only has one form, seen %d times" % (wdforms[0][0], wdforms[0][1]) + os.linesep)
                outf.write(most_freq[0] + os.linesep)
            else:
                continue
        else:
            if sum_all == 2:
                #outf.write("wd %s only occurred twice. cannot decide which is most frequent." % (wdkey) + os.linesep)
                continue
            else:
                wdforms.sort(lambda a,b: -cmp(a[1],b[1]))
                most_freq = wdforms[0]
                second_most_freq = wdforms[1]
                # see whether there is a tie. if so, no dice.
                if most_freq[1] == second_most_freq[1]:
                    #outf.write("wd %s occurred more than twice, but there was a tie btw forms %s and %s (perhaps others)." % \
                    #           (wdkey, wdforms[0][0], wdforms[1][0]) + os.linesep)
                    continue
                else:
                    # only mention it if the most freq form is uppercased somewhere.
                    if most_freq[0][0].lower() != most_freq[0][0]:
                        #outf.write("wd %s occurred most with form %s." % (wdkey, wdforms[0][0]) + os.linesep)
                        outf.write(most_freq[0] + os.linesep)
                    
finally:
    try:
        outf.close()
    except:
        pass

    try:
        inf.close()
    except:
        pass
        
