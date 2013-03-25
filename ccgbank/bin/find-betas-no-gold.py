"""
Given:

(1) a file supertagged words (OpenCCC file output format
as produced by, e.g., WordAndPOSDictionaryLabellingStrategy),

(2) a list (as a string) of tagging ambiguity levels (e.g.,
"1.4 1.6 1.8...") that represent the desired tag/word levels
(rounded off at the hundredths place to <=1.41, <=1.61, etc.),

(3) possibly tagging dictionaries (if needed),
and

(4) the corresponding 'K' parameters (e.g., "20 150" as in Clark and
Curran (2007)),

produce the list of betas that would produce those ambiguity
levels.
"""

import optparse
import sys
import decimal
import math

"""
A little on-the-fly class creation for iterating through multi-stag format files.
"""
class STIterator:
    def __init__(self, f):
        self.f = f
    def next(self):
        l = self.f.readline()
        while l.strip()=='' and l:
            l = self.f.readline()
        if l:
            lines = []
            # not at eof.
            if l.strip() != "<s>":
                print >> sys.stderr, "line=", l.strip(), "ill-formed st output file."
                raise Exception
            else:
                l = self.f.readline()
                while l.strip() != "</s>" and l:
                    lines.append(l.strip())
                    l = self.f.readline()
                if lines == []:
                    print >> sys.stderr, "line=", l.strip(), "ill-formed st output file."
                    raise Exception
                else:
                    res = []
                    for ln in lines:
                        # each line is: word <howmanypos> POS1 ... POSN <howmanysupertags> ST1 ... STM
                        # we just want the word, first pos and supertags.
                        parts = ln.split()
                        wd = parts[0]
                        pos = parts[2]
                        how_many_poss = int(parts[1])
                        stgs = zip(parts[2+(how_many_poss*2)+1::2],map(lambda n: float(n), parts[2+(how_many_poss*2)+2::2]))
                        res.append((wd,pos,stgs))
                    return res
        else:
            raise  StopIteration
    def __iter__(self): return self


p = optparse.OptionParser()

p.add_option("-i", "--inputf",  type="string", help="input source [default=<stdin>]",        default=sys.stdin)
p.add_option("-o", "--outputf", type="string", help="output destination [default=<stdout>]", default=sys.stdout)
p.add_option("-a", "--ambiguities", type="string", help="a space delimited string of tagging ambiguity levels [default=\"1.2 1.4 1.6 1.8 2.0 2.5 3.0 3.5\"]",
             default="1.2 1.4 1.6 1.8 2.0 2.5 3.0 3.5")
p.add_option("-K", "--Ks", type="string", help="a space delimited string of K values (only two) [default=\"20 150\", optional]", \
             default="20 150")
p.add_option("-w", "--wordkeyeddict", type="string", help="word-keyed tagging dict [no default, optional]",\
             default=None)
p.add_option("-p", "--poskeyeddict", type="string", help="POS-keyed tagging dict [no default, optional]",\
             default=None)

(ops,args) = p.parse_args()

   
try:
    # POS-keyed dict must be there if word-keyed one is.
    assert (not (not (ops.wordkeyeddict is None) and (ops.poskeyeddict is None)))
except:
    print >> sys.stderr, "need POS-keyed dict if using word-keyed dict."
    sys.exit(-1)

inf = ops.inputf
if not inf is sys.stdin:
    inf = open(inf, 'r')
    
outf = ops.outputf
if not outf is sys.stdout:
    outf = open(outf, 'w')

input_sents = [s for s in STIterator(inf)]

wdict = {}
if not ops.wordkeyeddict is None:
    entries = map(lambda l: l.split(), open(ops.wordkeyeddict, 'r').readlines())
    wdict[entries[0]] = (int(entries[1]), set(entries[2:]))

pdict = {}
if not ops.poskeyeddict is None:
    entries = map(lambda l: l.split(), open(ops.poskeyeddict, 'r').readlines())
    posdict[entries[0]] = set(entries[1:])
    
try:
    ambs = map(lambda a: float(a), ops.ambiguities.split())
    betas = []
    current_beta = 1.0
    last_beta_above  = None
    last_beta_below = 0.0
    total_tags   = 0.0
    total_words  = 0.0
    total_right  = 0.0
    ks = map(lambda kay: int(kay), ops.Ks.split())
    for a in ambs:
        current_beta = 1.0
        last_beta_above  = None
        last_beta_below = 0.0
        k =  ks[0] if a!=ambs[-1] else ks[1]
        found = False
        while not found:
            total_tags   = 0.0
            total_words  = 0.0
            total_right  = 0.0                    
            for insent in input_sents:
                for lex in insent:
                    total_words += 1
                    w   = lex[0]
                    pos = lex[1]
                    stags = lex[2]
                    if len(wdict)>0:
                        # filter with appropriate dictionary.
                        (freq,tags) = wdict.get(w,(0,set([])))
                        if freq >= k:
                            tags = tags
                        else:
                            tags = pdict.get(pos,set([]))
                        if len(tags)>0:
                            stags = filter(lambda st: st[0] in tags, stags)
                    best = stags[0][1]
                    # how many tags are there that made the beta cut-off?
                    total_tags += len(filter(lambda st: st[1] >= (current_beta * best), stags))
            # round to the nearest hundredth
            tags_per_word = (float(total_tags)/total_words)
            decimal.getcontext().prec = 4
            as_string = str(decimal.Decimal(str(tags_per_word)))
            # we're looking for 1.40..., or 1.60..., etc. (as the case may be)
            found = tags_per_word == a or ((as_string[:3]==str(a)[:3]) and (as_string[3]=='0'))

            if found:
                betas.append(current_beta)
                current_beta = 1.0
                last_beta = None
            else:
                # decide which direction to loosen the beta.
                if tags_per_word > a:
                    # get more restrictive (i.e., larger beta).
                    if last_beta_above is None:
                        print >> sys.stderr, "error"
                        sys.exit(-1)
                    else:
                        tempbeta = current_beta
                        current_beta += math.fabs(last_beta_above - current_beta)/2.0
                        last_beta_below = tempbeta

                else:
                    # get less restrictive (i.e., smaller beta)
                    tempbeta = current_beta
                    current_beta -= math.fabs(current_beta - last_beta_below)/2.0
                    last_beta_above = tempbeta

    print >> outf, "betas", ' '.join(map(lambda b: str(b), betas))
except:
    print "Unexpected error:", sys.exc_info()[0]
    raise
finally:
    # clean up, clean up...
    if not inf is sys.stdin:
        inf.close()
    if not outf is sys.stdout:
        outf.close()        
