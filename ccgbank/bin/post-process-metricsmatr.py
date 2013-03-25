"""
Pipe in a MetricsMATR-style mteval script output and, given (as sys.argv[1]) the name of the
reference system, produce a file that has lines of the form:

<sys>,<ref>,<doc>,<seg>,<score>
...
<sys>,<ref>,<doc>,<seg>,<score>
"""
import sys
refid = sys.argv[1].strip()

for l in sys.stdin:
    # e.g., "multiple_translation_set        E09     XIN20020316.0014        1       0.0715856157727753"
    (setid,sysid,docid,segid,score) = l.strip().split()
    print ",".join([sysid, refid, docid, segid, score])
    
