import sys
from realize_eval.score_human_eval import KeyFileParser

class LatexTableGenerator(object):
    def generate(self, tsv_file, table_file, oldIsA, is_diff_set):
        with open(tsv_file, 'r') as f, open(table_file, 'w') as table_f:
            for i, line in enumerate(f):
                if i == 0: # Discard header
                    continue
                cols = line.strip().split('\t')
                ref = cols[0]
                realA = cols[1]
                realB = cols[2]
                sentOldIsA = oldIsA[i-1]
                sent_is_diff_set = is_diff_set[i-1]
                self.generateTxtRow(table_f, ref, realA, realB, sentOldIsA, sent_is_diff_set)
    
    def generateLatexRow(self, table_f, ref, realA, realB, sentOldIsA, sent_is_diff_set):
        sent_set = 'One complete, one incomplete' if sent_is_diff_set else 'Both complete or both incomplete'
        table_f.write('\\textsc{Set} & ' + sent_set + ' \\\\\n')        
        table_f.write('\\textsc{Reference} & ' + ref + ' \\\\\n')
        
        lstm_real = realB if sentOldIsA else realA
        maxent_real = realA if sentOldIsA else realB
        table_f.write('\\textsc{LSTM} & ' + lstm_real + ' \\\\\n')
        table_f.write('\\textsc{MaxEnt2} & ' + maxent_real + ' \\\\\n')
        table_f.write('\n~\\\\ \\hline\n\n')
        
    def generateTxtRow(self, table_f, ref, realA, realB, sentOldIsA, sent_is_diff_set):
        sent_set = 'One complete, one incomplete' if sent_is_diff_set else 'Both complete or both incomplete'
        table_f.write('Set: ' + sent_set + '\n')        
        table_f.write('Reference: ' + ref + '\n')
        
        lstm_real = realB if sentOldIsA else realA
        maxent_real = realA if sentOldIsA else realB
        table_f.write('LSTM: ' + lstm_real + '\n')
        table_f.write('MaxEnt2: ' + maxent_real + '\n')
        table_f.write('\n')

if __name__ == '__main__':
    tsv_file = sys.argv[1]
    key_file = sys.argv[2]
    table_file = sys.argv[3]
    oldIsA, is_diff_set = KeyFileParser.parse(key_file)
    LatexTableGenerator().generate(tsv_file, table_file, oldIsA, is_diff_set)