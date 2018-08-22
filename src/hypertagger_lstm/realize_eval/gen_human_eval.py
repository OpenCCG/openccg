import sys
import random
import numpy as np
import xml.etree.ElementTree as et

''' Script for generating TSV file for human evaluations on realizations '''

class DiffSetGetter(object):
    '''
    Gets set of sentences in which realization differed between two systems.
    Partitions set into those that in which one produced complete realization and other didn't, and set where both did or didn't
    '''
    
    def __init__(self):
        self.skipped_real = 0
        self.same_real = 0
    
    def getDiffSets(self, ref_tree, old_tree, new_tree):
        set_diff_comp = []
        set_same_comp = []    
        # Assume that docs are sorted in order of docid in all files
        doc_tuples = zip(ref_tree.getroot(), old_tree.getroot(), new_tree.getroot())
        for ref_doc, old_doc, new_doc in doc_tuples:
            self.processSegs(set_diff_comp, set_same_comp, ref_doc, old_doc, new_doc)
        return set_diff_comp, set_same_comp
    
    def processSegs(self, set_diff_comp, set_same_comp, ref_doc, old_doc, new_doc):
        j = k = 0
        num_segs = len(list(ref_doc))    
        for i in range(num_segs):
            ref_seg = ref_doc[i]
            old_seg = old_doc[j]
            new_seg = new_doc[k]
            
            if ref_seg.attrib['id'] != old_seg.attrib['id'] and ref_seg.attrib['id'] != new_seg.attrib['id']:
                self.skipped_real += 1
            elif ref_seg.attrib['id'] != old_seg.attrib['id']:
                self.skipped_real += 1
                k += 1
            elif ref_seg.attrib['id'] != new_seg.attrib['id']:
                self.skipped_real += 1
                j += 1
            else:
                self.processSeg(set_diff_comp, set_same_comp, ref_seg, old_seg, new_seg)
                j += 1
                k += 1
    
    def processSeg(self, set_diff_comp, set_same_comp, ref_seg, old_seg, new_seg):
        if old_seg.text == new_seg.text:
            self.same_real += 1
            return
        elif (old_seg.attrib.has_key('complete') and not new_seg.attrib.has_key('complete')) or \
        (new_seg.attrib.has_key('complete') and not old_seg.attrib.has_key('complete')):
            set_diff_comp.append((ref_seg.text, old_seg.text, new_seg.text))
        else:
            set_same_comp.append((ref_seg.text, old_seg.text, new_seg.text))

class TSVProducer(object):
    def generateTSVAndKey(self, set_diff_comp, set_same_comp, tsv_path, key_path):
        comb_set, oldIsA, inds, is_diff_set = self.getRandomizations(set_diff_comp, set_same_comp)
        self.writeKeyFile(oldIsA, is_diff_set, key_path)
        self.writeTSVFile(comb_set, oldIsA, inds, tsv_path)
    
    def getRandomizations(self, set_diff_comp, set_same_comp):
        comb_set = set_diff_comp + set_same_comp
        oldIsA = np.random.random(len(comb_set))
        oldIsA = oldIsA >= 0.5
        inds = np.arange(len(comb_set))
        np.random.shuffle(inds)
        is_diff_set = inds < len(set_diff_comp)
        return comb_set, oldIsA, inds, is_diff_set
    
    def writeKeyFile(self, oldIsA, is_diff_set, key_path):
        with open(key_path, 'w') as f:
            f.write(str(oldIsA) + '\n\n')
            f.write(str(is_diff_set) + '\n')
    
    def writeTSVFile(self, comb_set, oldIsA, inds, tsv_path):
        with open(tsv_path, 'w') as f:
            header = '\t'.join(['Reference', 'RealizationA', 'RealizationB', 'Adequacy', 'Fluency'])
            f.write(header + '\n')
            for i, ind in enumerate(inds):
                ref, old, new = comb_set[ind]
                if oldIsA[i]:
                    f.write('\t'.join([ref, old, new, '0', '0']) + '\n')
                else:
                    f.write('\t'.join([ref, new, old, '0', '0']) + '\n')

if __name__ == '__main__':
    ref_file = sys.argv[1]
    old_file = sys.argv[2]
    new_file = sys.argv[3]
    ref_tree = et.parse(ref_file)
    old_tree = et.parse(old_file)
    new_tree = et.parse(new_file)
    
    diffset_getter = DiffSetGetter()
    set_diff_comp, set_same_comp = diffset_getter.getDiffSets(ref_tree, old_tree, new_tree)
    print(diffset_getter.skipped_real)
    print(diffset_getter.same_real)
    
    sample_diff_comp = random.sample(set_diff_comp, 50)
    sample_same_comp = random.sample(set_same_comp, 50)
    
    TSVProducer().generateTSVAndKey(sample_diff_comp, sample_same_comp, '../output/humanTSV.tsv', '../output/humanKey.txt')
