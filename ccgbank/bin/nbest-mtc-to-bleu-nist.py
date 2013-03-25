"""
This program takes the n-best realizer output as one file (with sys+DOC+segment IDs -- 'info' attributes),
the tb.xml parser output of OpenCCG (for grabbing the strings of those things that did not parse),
and creates an XML form suitable for use as a reference in the NIST-distributed BLEU script.

Usage: python nbest-mtc-to-bleu-nist.py [nbest-from-realizer] [parser-output(tb.xml)] [max-n] | [NIST/BLEU-compatible-multiref-file]
"""
import sys, os, re, codecs, tempfile, xml.sax.saxutils
try:
    import chardet
except:
    chardet = None
from xml.etree.ElementTree import *
from collections import defaultdict

# hack procedure. remove later.
def remove_ne(txt):
    # remove: Time, Location, Organization, Person, Money, Percent, Date
    txt = txt.replace(" LOCATION", "").replace(" PERSON", "").replace(" MONEY", "").replace(" PERCENT", "").replace(" DATE", "").replace(" TIME", "").replace(" ORGANIZATION", "")
    return txt

doc_pattern = re.compile(u"<[Dd][Oo][Cc] docid=\"(.*)\" sysid=\"(.*)\">(.*)")
seg_pattern = re.compile(u"<seg id=\"?(.*)\"?>(.*)</seg>$")

openccg_all = open(sys.argv[1], "rb").read().replace("_&", "_&amp;").replace(" & ", "&amp; ")
parser_all = open(sys.argv[2], "rb").read().replace("_&", "_&amp;").replace(" & ", "&amp; ")

if not chardet is None:
    encoding1 = chardet.detect(openccg_all)['encoding']
else:
    encoding1 = "utf-8"

if not chardet is None:
    encoding2 = chardet.detect(parser_all)['encoding']
else:
    encoding2 = "utf-8"
    
openccg_src = tempfile.NamedTemporaryFile()
openccg_src.write(openccg_all)
openccg_src.flush()
openccg_all = None

parser_src = tempfile.NamedTemporaryFile()
parser_src.write(parser_all)
parser_src.flush()
parser_all = None

try:
    n_size = int(sys.argv[3])
except:
    n_size = 4
    
# turn stdout into a UTF-8 converting writer.
streamWriter = codecs.lookup("utf-8")[-1]
sys.stdout = streamWriter(sys.stdout)
output = sys.stdout

# list of list of (ID/ref pairs)
refs = []

# max number of unique refs in any (there may not 'n_size' in any of them)
max_num_refs = 0

# map from docID -> [(segID, [text])]
doc_to_segs = defaultdict(lambda: [])

# get unparsed strings.
for event, elem in iterparse(parser_src.name):
    if elem.tag.lower() == "item" and elem.get("numOfParses") == "0":
        txt = elem.get("string").strip()
        (sys,doc,seg) = elem.get("info").split(",")
        doc_to_segs[doc].append((seg, [txt]))

for event, elem in iterparse(openccg_src.name):
    if elem.tag.lower() == "seg":
        (sys,doc,seg) = elem.get("id").split(",")
        is_complete = True if elem.get("complete") else False
        nbest_realizations = []
        if not is_complete:
            # just get the original input.            
            for child in list(elem):
                if child.tag.lower() == "ref":
                    nbest_realizations.append(child.text.strip())
        else:
            # get the n-best (only keeping unique strings), so, e.g., 4-best might turn into
            # 1-best if they're all the same.
            how_many = 0
            for child in list(elem):
                if child.tag.lower() in ["ref", "best", "next"] and how_many < n_size:
                    txt = child.text.strip()
                    if not txt in nbest_realizations:
                        nbest_realizations.append(txt)
                    how_many += 1
                elif how_many >= n_size:
                    break
            if len(nbest_realizations) > max_num_refs:
                max_num_refs = len(nbest_realizations)
                
        doc_to_segs[doc].append((seg, nbest_realizations))


output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + \
             os.linesep + "<!DOCTYPE mteval SYSTEM \"ftp://jaguar.ncsl.nist.gov/mt/resources/mteval-xml-v1.3.dtd\">" + os.linesep +\
             "<mteval>" + os.linesep)

docs = doc_to_segs.keys()
docs.sort()

for i in range(max_num_refs):
    output.write("<refset setid=\"multiple_translation_set\" srclang=\"Chinese\" trglang=\"English\" refid=\"%d\">" % (i+1) + os.linesep)
    for doc in docs:
        output.write("    <doc docid=\"%s\" genre=\"nw\">" % doc + os.linesep)
        segs = doc_to_segs[doc]
        segs.sort(lambda a,b: cmp(int(a[0]), int(b[0])))
        for (seg, paraphrases) in segs:
            this_one = i
            if i >= len(paraphrases):
                # there aren't as many paraphrases here as there are in the maximum length ref, so we just re-duplicate
                # the last one of this ref.                
                this_one = len(paraphrases)-1
            output.write("        <seg id=\"%d\"> %s </seg>" % (int(seg), xml.sax.saxutils.escape(remove_ne(paraphrases[this_one].replace("_"," "))) + os.linesep))
        output.write("    </doc>" + os.linesep)
    output.write("</refset>" + os.linesep)
output.write("</mteval>")
