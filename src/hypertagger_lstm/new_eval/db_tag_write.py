from get_db_id import DbIdGetter
import logging

class DbTagWriter(object):
    def __init__(self, strategyID, model_type, featvals, num_tokens, expected_tags, counted,
                 unk_preds, unk_predtags, translater, db_id_getter=None):
        ''' featvals is ndarray of indices for all features '''
        self.strategyID = strategyID
        self.model_type = model_type
        self.featvals = featvals
        self.sentences = featvals[:,:,0]
        self.num_tokens = num_tokens
        self.expected_tags = expected_tags
        self.counted = counted
        self.unk_preds = unk_preds
        self.unk_predtags = unk_predtags
        self.separator = '|'
        self.translater = translater
        self.id_getter = db_id_getter
        if self.id_getter is None:
            self.id_getter = DbIdGetter()
    
    def writeCSV(self, tagcompare_file, feat_file, wordprops_file, assigned_tags):
        lines_written = 0
        with open(tagcompare_file, 'w') as tcf, open(feat_file, 'w') as ff, open(wordprops_file, 'w') as wpf:
            self.writeHeaders(tcf, ff, wpf)
            for i in range(self.sentences.shape[0]):
                sent_id = self.id_getter.sentenceID(self.model_type, i)
                for j in range(self.num_tokens[i]):
                    counted_in_acc = int(self.counted[i,j])
                    if counted_in_acc == 1:
                        word = self.translater.wordAtIndex(self.sentences[i,j])
                        word_id = self.id_getter.wordID(word)
                        self.writeTagCompareLine(tcf, sent_id, word_id, i, j, assigned_tags)
                        if self.model_type.lower() != 'train':
                            self.writeFeatLine(ff, sent_id, word_id, i, j)
#                         self.writeWordPropsLine(wpf, sent_id, word_id, i, j)
                        lines_written += 1
        logging.info('Wrote {} lines to {}, {}, {}'.format(lines_written, tagcompare_file, feat_file, wordprops_file))
    
    def writeHeaders(self, tagcompare_file, feat_file, wordprops_file):
        tagcompare_cols = ["StrategyID", "SentID", "WordID", "Position", "ExpTagID", "ActTagID"]
        tagcompare_header = self.separator.join(tagcompare_cols)
        tagcompare_file.write(tagcompare_header + '\n')
        
        feat_cols = ["StrategyID", "SentID", "WordID", "Position", "NEC", "Det", "Mood", "Num", "Ptc", "Tense", 
                     "FanOut", "NumArg", "PRel0", "PRel1", "PRel2", "PRel3", "PRel4", "CRel0", "CRel1", "CRel2", "CRel3", "CRel4"]
        feat_header = self.separator.join(feat_cols)
        feat_file.write(feat_header + '\n')
        
        wordprops_cols = ["SentID", "WordID", "Position", "UnkPred", "UnkPredTag"]
        wordprops_header = self.separator.join(wordprops_cols)
        wordprops_file.write(wordprops_header + '\n')
    
    def writeTagCompareLine(self, tagcompare_file, sent_id, word_id, sent_num, position, assigned_tags):
        exp_tag = self.translater.tagAtIndex(self.expected_tags[sent_num, position])
        exp_tag_id = self.id_getter.tagID(exp_tag)
        act_tag = self.translater.tagAtIndex(assigned_tags[sent_num, position])
        act_tag_id = self.id_getter.tagID(act_tag)
        line_elements = [str(self.strategyID), str(sent_id), str(word_id), str(position), str(exp_tag_id), str(act_tag_id)]
        line = self.separator.join(line_elements)
        tagcompare_file.write(line + '\n')
    
    def writeFeatLine(self, feat_file, sent_id, word_id, sent_num, position):
        num_feats = self.featvals.shape[2]
        feat_cols = []
        for i in range(1, num_feats): # Skip first two features: predicate name and hypertag
            ind = self.featvals[sent_num, position, i]
            feat_cols.append(str(self.translater.featValAtIndex(i+1, ind)))
        feat_cols = [str(self.strategyID), str(sent_id), str(word_id), str(position)] + feat_cols
        line = self.separator.join(feat_cols)
        feat_file.write(line + '\n')
    
    def writeWordPropsLine(self, wordprops_file, sent_id, word_id, sent_num, position):
        unk_pred = self.unk_preds[sent_num, position]
        unk_predtag = self.unk_predtags[sent_num, position]
        line_elements = [str(sent_id), str(word_id), str(position), str(unk_pred), str(unk_predtag)]
        line = self.separator.join(line_elements)
        wordprops_file.write(line + '\n')
