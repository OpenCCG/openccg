"""
Correlate human judgments (streamed into sys.stdin -- e.g., from the MTC)
to the BLEU/NIST scores in the given directories (sys.argv[1] and sys.argv[2]).

Depends on rpy2 having been installed.
"""
import sys, os
try:
    import rpy2
    import rpy2.robjects as robjs
    
except ImportError:
    print >> sys.stderr, "please install rpy2. exiting..."
    sys.exit(-1)



def num2string(n):
    """
    E.g., 0 => '00', 1 => '01' and 10 => '10'.
    """
    try:
        numm = int(n)
    except:
        numm = n
    if numm < 10:
        return str(numm)
    else:
        return str(numm)
    
human_judgments = []
human_judgments_fluency = [] # fluency.
human_judgments_acc = [] # accuracy.
human_judgments_ave = [] # average of both.
   
for l in sys.stdin:
    l = l.strip()    
    if l.startswith("#"):
        continue
    lparts = l.split(",")
    # appending (sys,doc,judge,ref_sys,segment,fluency,accuracy)
    human_judgments.append(tuple(lparts[0:7]))
    human_judgments_fluency.append(int(lparts[5]))
    human_judgments_acc.append(int(lparts[6]))
    human_judgments_ave.append((float(lparts[5]) + float(lparts[6]))/2.0)

human_judgments_fluency = robjs.FloatVector([float(i) for i in human_judgments_fluency])
human_judgments_acc = robjs.FloatVector([float(i) for i in human_judgments_acc])
human_judgments_ave = robjs.FloatVector([float(i) for i in human_judgments_ave])

bleu_nist_dir1 = sys.argv[1]
bleu_nist_dir2 = sys.argv[2]

# the next two are maps from: (sys,doc,ref,segID) => score
bleu_scores = {}
nist_scores = {}

for f in [fl for fl in os.listdir(bleu_nist_dir1) if ("BLEU" in fl or "NIST" in fl)]:
    for l in open(bleu_nist_dir1 + os.sep + f, "rb").readlines():
        if l.strip() == "":
            continue
        (sys,ref_sys,doc,seg,bleu_or_nist_score) = l.split(",")
        if "BLEU" in f:
            bleu_scores[(sys,doc,ref_sys,"S"+num2string(seg))] = bleu_or_nist_score
        else:
            nist_scores[(sys,doc,ref_sys,"S"+num2string(seg))] = bleu_or_nist_score
    

for f in [fl for fl in os.listdir(bleu_nist_dir2) if ("BLEU" in fl or "NIST" in fl)]:
    for l in open(bleu_nist_dir2 + os.sep + f, "rb").readlines():
        if l.strip() == "":
            continue
        (sys,ref_sys,doc,seg,bleu_or_nist_score) = l.split(",")
        if "BLEU" in f:
            bleu_scores[(sys,doc,ref_sys,"S"+num2string(seg))] = float(bleu_or_nist_score)
        else:
            nist_scores[(sys,doc,ref_sys,"S"+num2string(seg))] = float(bleu_or_nist_score)


# for both BLEU and NIST, compute rpy2 vectors that parallel the seqeuence
# of human judgments.
# step through the (sys,doc,judge,ref_sys,segment,fluency,accuracy) tuples.
bleu_lst = []
nist_lst = []
for (s,d,j,rs,sg,f,a) in human_judgments:
    if (s,d,rs,sg) in bleu_scores:
        bleu_lst.append(bleu_scores.get((s,d,rs,sg)))
    else:
        print "nope", (s,d,rs,sg)
    if (s,d,rs,sg) in nist_scores:
        nist_lst.append(nist_scores.get((s,d,rs,sg)))
    else:
        print "nope", (s,d,rs,sg)

bleu_vec = robjs.FloatVector(bleu_lst)
nist_vec = robjs.FloatVector(nist_lst)

# compute correlations
b_fluency = robjs.r['cor'](bleu_vec, human_judgments_fluency)
b_accuracy = robjs.r['cor'](bleu_vec, human_judgments_acc)
b_average  = robjs.r['cor'](bleu_vec, human_judgments_ave)

print "BLEU's Pearson correlation wrt fluency:", b_fluency
print "BLEU's Pearson correlation wrt accuracy:", b_accuracy
print "BLEU's Pearson correlation wrt the average of fluency and accuracy", b_average

nist_fluency = robjs.r['cor'](nist_vec, human_judgments_fluency)
nist_accuracy = robjs.r['cor'](nist_vec, human_judgments_acc)
nist_average  = robjs.r['cor'](nist_vec, human_judgments_ave)

print "NIST's Pearson correlation wrt fluency:", nist_fluency
print "NIST's Pearson correlation wrt accuracy:", nist_accuracy
print "NIST's Pearson correlation wrt the average of fluency and accuracy", nist_average
