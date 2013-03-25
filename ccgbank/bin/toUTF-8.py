"""
Copryright (c) 2011 Dennis N. Mehay

Assumes that 'chardet' is installed.

Re-encodes most known Unicode encodings as UTF-8.

(Provided that there is enough text for chardet to
correctly determine the encoding of the input file.)

If any file exists by the output file name, it will
be overwritten.

[Insert LGPL here]
"""

import sys, codecs, optparse

try:
    import chardet
except ImportError, ie:
    print >> sys.stderr, "'chardet' must be installed for this script to work. Exiting..."
    sys.exit(-1)

op = optparse.OptionParser()
op.add_option("-i", "--inputf", type="string", help="input file [required]", default=None)
op.add_option("-o", "--outputf", type="string", help="output file [required, will be overwritten]", default=None)

(ops,args) = op.parse_args()

try:
    assert(not (ops.inputf is None or ops.outputf is None))
except AssertionError, ae:
    print >> sys.stderr, "provide input and output files (type: 'python toUTF-8.py -h' for help)"
    sys.exit(-1)

# get input file's content and convert to utf-8
inf = open(ops.inputf, "rb")
input = inf.read()
outf = None
try:
    encoding = chardet.detect(input).get('encoding')
    input = input.decode(encoding)
    outf = codecs.open(ops.outputf, "wb", "utf-8")
    outf.write(input)
except Exception, e:
    print >> sys.stderr, "Something went wrong. Perhaps your input format is too obscure"
finally:
    outf.close()
    inf.close()
