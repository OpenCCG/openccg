"""
This program re-inserts the MTC unique IDs (sys+DOC+segment) into an auto-number-ID'ed parse of said
MTC (or similar) document produced by OpenCCG's 'ccg-parse'.

Usage: python merge-mtc-ids.py [output-of-OpenCCG-parser] [MTC-like-input-file] > [output-of-OpenCCG-parser-with-original-MTC-ids]
"""
import sys, os, re, codecs

try:
    import chardet
except:
    chardet = None
from xml.etree.ElementTree import *

doc_pattern = re.compile(u"<[Dd][Oo][Cc] docid=\"(.*)\" sysid=\"(.*)\">(.*)")
seg_pattern = re.compile(u"<seg id=\"?(.*)\"?>(.*)</seg>$")

openccg_in = sys.argv[1]
#mtc_in = codecs.open(sys.argv[2], "rb", "utf-8").read()
if not chardet is None:
    encoding = chardet.detect(open(sys.argv[2], "rb").read())['encoding']
else:
    encoding = "ISO-8859-2"
mtc_in = codecs.open(sys.argv[2], "rb", encoding).readlines()

# turn stdout into a UTF-8 converting writer.
streamWriter = codecs.lookup(encoding)[-1]
sys.stdout = streamWriter(sys.stdout)
output = sys.stdout

# map from auto-assigned ID to MTC ID.
autoid2mtcid = {}

mtc_ids     = []
for l in mtc_in:
    l = l.strip()
    if l.startswith("<DOC"):
        match = doc_pattern.findall(l)[0]
        (docid, sysid) = (match[0], match[1])
        curr_doc = docid
        curr_sys = sysid
    elif l.startswith("<seg"):
        match = seg_pattern.findall(l)[0]
        (segid, text) = (match[0], match[1])
        mtc_ids.append((curr_sys, curr_doc, segid, text.strip()))


output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + os.linesep + "<regression>" + os.linesep)
for event, elem in iterparse(openccg_in):
    if elem.tag.lower() == "item":
        next_mtc_id = mtc_ids.pop(0)
        elem.set("info", u",".join(next_mtc_id[:-1]))
        output.write(u"\t" + tostring(elem).strip() + os.linesep)
output.write("</regression>" + os.linesep)
