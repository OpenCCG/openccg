"""
Assuming UTF-8 input (defaults to stdin, otherwise, supply a file), normalize plain text in
various ways -- e.g., normalize quotation marks.

Copyright Dennis N. Mehay (2011)
[Insert LGPL here]
"""
from optparse import OptionParser as OP
import codecs, sys, os

op = OP()
op.add_option("-i", "--input", type="string", help="input stream of text (file, or default=sys.stdin)", default=sys.stdin)
op.add_option("-o", "--output", type="string", help="output stream (file, or default=sys.stdout)", default=sys.stdout)

(ops,args) = op.parse_args()


if not ( ops.input is sys.stdin or ops.input == "-" ):
    inf = codecs.open(ops.input, "rb", "utf-8")
else:
    streamReader = codecs.lookup("utf-8")[2]
    inf = streamReader(sys.stdin)

if not ( ops.output is sys.stdout or ops.output == "-" ):
    outf = codecs.open(ops.output, "wb", "utf-8")
else:
    streamWriter = codecs.lookup("utf-8")[-1]
    outf = streamWriter(sys.stdout)

try:
    l = inf.readline().strip()
    while l:
        transformed_line = []
        opening_quotes = True
        for c in l:
            if c == u'"' and opening_quotes:
                transformed_line.append(u"``")
                opening_quotes = False # next double quotes will be closing quotes.
            elif c == u'"' and not opening_quotes:
                transformed_line.append(u"''")
                opening_quotes = True # reset the open-close tracker.
            else:
                transformed_line.append(c)

        outf.write(u"".join(transformed_line) + os.linesep)
        
        l = inf.readline().strip()
finally:
    inf.close()
    outf.close()


