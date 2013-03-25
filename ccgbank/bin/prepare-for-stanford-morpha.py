"""
Take in a space-delimited file of <word>...<POS>...<SEM-CLASS> and turn it into 
a form that the Stanford NE recognizer can accept (and from which we can extract
all the information we need later).

<stdin> => <stdout>
"""

import sys, codecs, os

streamWriter = codecs.lookup("utf-8")[-1]
sys.stdout = streamWriter(sys.stdout)

streamReader = codecs.lookup("utf-8")[2]
sys.stdin = streamReader(sys.stdin)

for l in sys.stdin:
    l = l.strip()
    parts = l.split()
    if len(parts) > 2:
        # has NE label.
        wordform = u"*****".join([parts[0],parts[-1]]).replace("_","^")
    else:
        wordform = parts[0]
    pos = parts[1]

    joined = u"_".join([wordform, pos])
    sys.stdout.write(joined + '\n')
