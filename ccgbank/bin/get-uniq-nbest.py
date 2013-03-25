from BeautifulSoup import BeautifulStoneSoup as BSS
import codecs
import sys, os
streamWriter = codecs.lookup('utf-8')[-1]
sys.stdout = streamWriter(sys.stdout)

inf = open(sys.argv[1], "rb").read()
try:
    beta = float(sys.argv[2])
except:
    beta = 0.1
    
soup = BSS(inf)

segs = soup.findAll(lambda t: t.name == u'seg')

tot_paraphrases = 0.0
tot_segs = 0.0

for seg in segs:
    tot_segs += 1
    if seg.get('complete') == 'true':
        best = seg.find(lambda p: p.name == 'best')
        ref          = seg.ref.find(text=True)
        eye_dee      = seg.get(u'id')
        paraphrases  = set([p.find(text=True) for p in seg.findAll(lambda e: e.name in [u'best', u'next'])])
        tot_paraphrases += len(paraphrases)
        sys.stdout.write(ref + u' ||| ' + u' <-> '.join(paraphrases))
        sys.stdout.write(os.linesep)

print "ave paraphrases/seg", tot_paraphrases/tot_segs
