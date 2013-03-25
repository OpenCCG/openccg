"""
This program takes a set of documents (all streamed from stdin at once) and formats them in a way suitable for
use with the NIST-distributed mteval script. The output is in UTF-8.

Usage: cat [MTC_DIR_FOR_SYSTEM] | python convert-mtc.systems.py [doctype-string {'source', 'target', 'ref'} (default='target')] | [NEW_XML_DOC_TO_STDOUT]
"""
import sys, os, re, codecs, xml.sax.saxutils, my_unicode
try:
    import chardet
except:
    chardet = None
from xml.etree.ElementTree import *
from collections import defaultdict

def tokenize(t):
    """very simple text tokenization:
    <blah>n't => <blah> n't
    <blah>'s  => <blah> 's
    <blah>'   => <blah> '
    where '<blah>' is not whitespace.
    """
    t = t.replace("n't", " n't").replace("'s", " 's").replace("' ", " ' ")
    if t[-1] == "'":
        t = t[:-1] + " " + t[-1]
    return t.replace("  ", " ")

def decode_line(ln, encoding):
    res = None
    try:
        res = ln.decode(encoding)
    except:
        try:
            res = ln.decode("iso-8859-2")
        except:
            try:
                res = ln.decode("iso-8859-1")
            except:
                try:
                    res = ln.decode("utf-8")
                except:
                    try:
                        res = ln.decode("GB2312")
                    except:
                        try:
                            res = ln.decode("Big5")
                        except:
                            try:
                                res = ln.decode("EUC-TW")
                            except:
                                res = ln
                
    return res

doc_pattern = re.compile(u"<[Dd][Oo][Cc] docid=\"(.*)\" sysid=\"(.*)\">(.*)")
doc_pattern_source = re.compile(u"<[Dd][Oo][Cc] docid=\"(.*)\">(.*)")
seg_pattern = re.compile(u"<seg id=\"?(.*)\"?>(.*)</seg>$")

doc_type = "target"
if len(sys.argv) >= 2:
    doc_type = sys.argv[1].lower()
if not doc_type in ["target", "source", "reference"]:
    doc_type = "target"
    
mtc_in = sys.stdin.readlines()
mtc_all = (os.linesep).join(mtc_in)

if not chardet is None:
    encoding = chardet.detect(mtc_all)['encoding']
else:
    encoding = "ISO-8859-2"

# turn stdout into a UTF-8 converting writer.
streamWriter = codecs.lookup("UTF-8")[-1]
sys.stdout = streamWriter(sys.stdout)
output = sys.stdout

# map from auto-assigned ID to MTC ID.
autoid2mtcid = {}

mtc     = defaultdict(lambda: [])
sys = None
for l in mtc_in:
    l = decode_line(l, encoding).strip()
    if l.startswith("<DOC"):
        match = doc_pattern.findall(l)[0] if (doc_type == "target") else doc_pattern_source.findall(l)[0]
        if doc_type == "target":
            (docid, sysid) = (match[0], match[1])
        else:
            (docid, sysid) = (match[0], None)
        docid = docid.replace("_",".")
        curr_doc = docid
        curr_sys = sysid
        sys = curr_sys
    elif l.startswith("<seg"):
        match = seg_pattern.findall(l)[0]
        (segid, text) = (match[0], match[1])
        
        mtc[(curr_sys, curr_doc)].append((segid, text.strip()))

output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + \
             os.linesep + "<!DOCTYPE mteval SYSTEM \"ftp://jaguar.ncsl.nist.gov/mt/resources/mteval-xml-v1.3.dtd\">" + os.linesep +\
             "<mteval>" + os.linesep)
if doc_type == "target":
    output.write("    <tstset setid=\"multiple_translation_set\" srclang=\"Chinese\" trglang=\"English\" sysid=\"%s\">" % sys + os.linesep)
elif doc_type == "source":
    output.write("    <srcset setid=\"multiple_translation_set\" srclang=\"Chinese\">" + os.linesep)
else:
    output.write("    <refset setid=\"multiple_translation_set\" srclang=\"Chinese\" trglang=\"English\" refid=\"1\">" + os.linesep)
docs = mtc.keys()
docs.sort()

for (sy,doc) in docs:
    output.write("        <doc docid=\"%s\" genre=\"nw\">" % doc + os.linesep)
    segids_and_texts = mtc[(sy,doc)]
    segids_and_texts.sort(lambda a,b: cmp(int(a[0]),int(b[0])))
    for (segid,text) in segids_and_texts:
        output.write("            <seg id=\"%s\"> %s </seg>" % (segid,xml.sax.saxutils.escape(my_unicode.removeInvalidChars(tokenize(text)))) + os.linesep)
    output.write("        </doc>" + os.linesep)

if doc_type == "target":
    output.write("    </tstset>" + os.linesep + "</mteval>")
elif doc_type == "source":
    output.write("    </srcset>" + os.linesep + "</mteval>")
else:
    output.write("    </refset>" + os.linesep + "</mteval>")

