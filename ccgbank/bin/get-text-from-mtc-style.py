"""
Gets the text from a MTC-style corpus.
Just looks for '<seg id=...> </seg>' segments.

<stdin> => <stdout>

(c) 2011 Dennis Nolan Mehay
[Insert LGPL here]
"""
import re, sys, codecs, os

pattern = re.compile(u"<seg id=[^ ]*>\\s*(.*)\\s*</seg>")

input = sys.stdin.read()

try:
    import chardet
    encoding = chardet.detect(input)['encoding']
except:
    # this is what the original MTC corpus is encoded in.
    encoding = "iso-8859-2"

input = input.decode(encoding)
streamWriter = codecs.lookup(encoding)[-1]
sys.stdout = streamWriter(sys.stdout)

for seg in pattern.findall(input):
    sys.stdout.write(seg.strip() + os.linesep)
    
