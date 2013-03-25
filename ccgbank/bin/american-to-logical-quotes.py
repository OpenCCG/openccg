"""
Copryright (c) 2011 Dennis N. Mehay

Assumes tokenized, PTB3-normalized UTF-8 text, one sentence per line.

<stdin> => <stdout>

Turns 'American'-style quotations into 'British'/'logical'-style quotations.

So, e.g.,

`` Hello , '' said John .

becomes:

`` Hello '' , said John .

[Insert LGPL here]
"""

import sys, codecs, os

streamReader = codecs.lookup("utf-8")[2]
streamWriter = codecs.lookup("utf-8")[-1]

sys.stdin = streamReader(sys.stdin)
sys.stdout = streamWriter(sys.stdout)

for ln in sys.stdin:
    # trim off extra whitespace and replace double spaces with single spaces.
    ln = ln.strip().replace(u"  ", u" ")
    # now replace
    #  <space>, ''
    # with
    #  <space>''
    # and
    # <space>. ''
    # with
    # <space>'' .
    ln = ln.replace(u" , ''", " '' ,").replace(u" . ''", " '' .")
    # now fix any double-punct messes this might have created.
    ln = ln.replace(u" '' . ?", u" . '' ?").replace(u" '' . !", u" . '' !")
    sys.stdout.write(ln + '\n')
