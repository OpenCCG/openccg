import sys
import numpy as np

''' Script for generating TSV file for human evaluations on realizations '''

class KeyFileParser(object):
    @classmethod
    def parse(cls, key_file):
        with open(key_file, 'r') as f:
            oldIsA_str = ''
            is_diff_set_str = ''
            past_empty_line = False
            
            for line in f:
                if line.strip() == '':
                    past_empty_line = True
                elif not past_empty_line:
                    oldIsA_str += line.rstrip()
                else:
                    is_diff_set_str += line.rstrip()
            
            oldIsA = oldIsA_str[1:-1].strip().split()
            oldIsA = [s == 'True' for s in oldIsA]
            oldIsA = np.array(oldIsA)
            
            is_diff_set = is_diff_set_str[1:-1].strip().split()
            is_diff_set = [s == 'True' for s in is_diff_set]
            is_diff_set = np.array(is_diff_set)
            return oldIsA, is_diff_set

class TSVScorer(object):
    def __init__(self):
        self.old_sys_ad_diff = self.old_sys_fl_diff = self.new_sys_ad_diff = self.new_sys_fl_diff = 0
        self.old_sys_ad_same = self.old_sys_fl_same = self.new_sys_ad_same = self.new_sys_fl_same = 0
    
    def score(self, tsv_file, oldIsA, is_diff_set):
        adA, adB, flA, flB = ([] for i in range(4)) # Lists of indices
        with open(tsv_file, 'r') as f:
            for i, line in enumerate(f):
                if i == 0: # Discard header
                    continue
                cols = line.strip().split('\t')
                ad = cols[3] if len(cols) >= 4 else ''
                fl = cols[4] if len(cols) >= 5 else ''
                self.updateAdequacyCounts(oldIsA, is_diff_set, i-1, ad)
                self.updateFluencyCounts(oldIsA, is_diff_set, i-1, fl)
                
                if ad.upper() == 'A':
                    adA.append(i)
                elif ad.upper() == 'B':
                    adB.append(i)
                if fl.upper() == 'A':
                    flA.append(i)
                elif fl.upper() == 'B':
                    flB.append(i)
        return adA, adB, flA, flB
    
    def updateAdequacyCounts(self, oldIsA, is_diff_set, index, better_ad):
        '''
        better_ad: string indicating choice of which realization has better adequacy
        '''        
        if oldIsA[index] and is_diff_set[index]:
            if better_ad.upper() == 'A':
                self.old_sys_ad_diff += 1
            elif better_ad.upper() == 'B':
                self.new_sys_ad_diff += 1
        elif oldIsA[index] and not is_diff_set[index]:
            if better_ad.upper() == 'A':
                self.old_sys_ad_same += 1
            elif better_ad.upper() == 'B':
                self.new_sys_ad_same += 1
        elif not oldIsA[index] and is_diff_set[index]:
            if better_ad.upper() == 'A':
                self.new_sys_ad_diff += 1
            elif better_ad.upper() == 'B':
                self.old_sys_ad_diff += 1
        else:
            if better_ad.upper() == 'A':
                self.new_sys_ad_same += 1
            elif better_ad.upper() == 'B':
                self.old_sys_ad_same += 1
    
    def updateFluencyCounts(self, oldIsA, is_diff_set, index, better_fl):
        '''
        better_fl: string indicating choice of which realization has better fluency
        '''        
        if oldIsA[index] and is_diff_set[index]:
            if better_fl.upper() == 'A':
                self.old_sys_fl_diff += 1
            elif better_fl.upper() == 'B':
                self.new_sys_fl_diff += 1
        elif oldIsA[index] and not is_diff_set[index]:
            if better_fl.upper() == 'A':
                self.old_sys_fl_same += 1
            elif better_fl.upper() == 'B':
                self.new_sys_fl_same += 1
        elif not oldIsA[index] and is_diff_set[index]:
            if better_fl.upper() == 'A':
                self.new_sys_fl_diff += 1
            elif better_fl.upper() == 'B':
                self.old_sys_fl_diff += 1
        else:
            if better_fl.upper() == 'A':
                self.new_sys_fl_same += 1
            elif better_fl.upper() == 'B':
                self.old_sys_fl_same += 1

if __name__ == '__main__':
    tsv_file1 = sys.argv[1]
    tsv_file2 = sys.argv[2]
    key_file = sys.argv[3]
    
    oldIsA, is_diff_set = KeyFileParser.parse(key_file)
    scorer1 = TSVScorer()
    scorer2 = TSVScorer()
    adequacyA1, adequacyB1, fluencyA1, fluencyB1 = scorer1.score(tsv_file1, oldIsA, is_diff_set)
    adequacyA2, adequacyB2, fluencyA2, fluencyB2 = scorer2.score(tsv_file2, oldIsA, is_diff_set)    
    print(scorer1.new_sys_ad_diff, scorer1.new_sys_ad_same, scorer1.new_sys_fl_diff, scorer1.new_sys_fl_same)
    print(scorer1.old_sys_ad_diff, scorer1.old_sys_ad_same, scorer1.old_sys_fl_diff, scorer1.old_sys_fl_same)
    print(scorer2.new_sys_ad_diff, scorer2.new_sys_ad_same, scorer2.new_sys_fl_diff, scorer2.new_sys_fl_same)
    print(scorer2.old_sys_ad_diff, scorer2.old_sys_ad_same, scorer2.old_sys_fl_diff, scorer2.old_sys_fl_same)
    print('')
    
    adA = set(adequacyA1).intersection(set(adequacyA2))
    adB = set(adequacyB1).intersection(set(adequacyB2))
    flA = set(fluencyA1).intersection(set(fluencyA2))
    flB = set(fluencyB1).intersection(set(fluencyB2))
    print(adA)
    print(adB)
    print(flA)
    print(flB)
    print(adA.intersection(flA))
    print(adB.intersection(flB))
    print('')
    
    disagreeAdA = len(set(adequacyA1).difference(set(adequacyA2))) + len(set(adequacyA2).difference(set(adequacyA1)))
    disagreeAdB = len(set(adequacyB1).difference(set(adequacyB2))) + len(set(adequacyB2).difference(set(adequacyB1)))
    disagreeFlA = len(set(fluencyA1).difference(set(fluencyA2))) + len(set(fluencyA2).difference(set(fluencyA1)))
    disagreeFlB = len(set(fluencyB1).difference(set(fluencyB2))) + len(set(fluencyB2).difference(set(fluencyB1)))
    agree = 200 - (disagreeAdA + disagreeAdB + disagreeFlA + disagreeFlB)
    print(agree, disagreeAdA + disagreeAdB, disagreeFlA + disagreeFlB)
    print('')
    
    lstm_better1_ad = scorer1.new_sys_ad_diff + scorer2.new_sys_ad_diff
    lstm_better2_ad = scorer1.new_sys_ad_same + scorer2.new_sys_ad_same
    lstm_better1_fl = scorer1.new_sys_fl_diff + scorer2.new_sys_fl_diff
    lstm_better2_fl = scorer1.new_sys_fl_same + scorer2.new_sys_fl_same
    lstm_worse1_ad = scorer1.old_sys_ad_diff + scorer2.old_sys_ad_diff
    lstm_worse2_ad = scorer1.old_sys_ad_same + scorer2.old_sys_ad_same
    lstm_worse1_fl = scorer1.old_sys_fl_diff + scorer2.old_sys_fl_diff
    lstm_worse2_fl = scorer1.old_sys_fl_same + scorer2.old_sys_fl_same
    lstm_same1_ad = 100 - lstm_better1_ad - lstm_worse1_ad
    lstm_same2_ad = 100 - lstm_better2_ad - lstm_worse2_ad
    lstm_same1_fl = 100 - lstm_better1_fl - lstm_worse1_fl
    lstm_same2_fl = 100 - lstm_better2_fl - lstm_worse2_fl
    
    print('Number in judgments for set 1 in which LSTM did better (adequacy): {}'.format(lstm_better1_ad))
    print('Number in judgments for set 2 in which LSTM did better (adequacy): {}'.format(lstm_better2_ad))
    print('Number in judgments for set 1 in which LSTM did better (fluency): {}'.format(lstm_better1_fl))
    print('Number in judgments for set 2 in which LSTM did better (fluency): {}'.format(lstm_better2_fl))
    print('Number in judgments for set 1 in which LSTM did worse (adequacy): {}'.format(lstm_worse1_ad))
    print('Number in judgments for set 2 in which LSTM did worse (adequacy): {}'.format(lstm_worse2_ad))
    print('Number in judgments for set 1 in which LSTM did worse (fluency): {}'.format(lstm_worse1_fl))
    print('Number in judgments for set 2 in which LSTM did worse (fluency): {}'.format(lstm_worse2_fl))
    print('Number in judgments for set 1 in which LSTM did equally well (adequacy): {}'.format(lstm_same1_ad))
    print('Number in judgments for set 2 in which LSTM did equally well (adequacy): {}'.format(lstm_same2_ad))
    print('Number in judgments for set 1 in which LSTM did equally well (fluency): {}'.format(lstm_same1_fl))
    print('Number in judgments for set 2 in which LSTM did equally well (fluency): {}'.format(lstm_same2_fl))
