from ccgbank import AnnotSeqReader
from feat_preprocessing.feat_orders import FeatOrders
from feat_preprocessing.decoder import Decoder
import numpy as np
import sys

class UnknownFinder(object):
    def unknownPreds(self, train_preds, dev_preds, output_file=None, decode=False):
        train_word_set = set([word for sent in train_preds for word in sent])
        dev_word_set = set([word for sent in dev_preds for word in sent])
        unknowns = dev_word_set.difference(train_word_set)
        
        if output_file is not None:
            with open(output_file, 'w') as f:
                for word in unknowns:
                    if decode:
                        word = Decoder.decode(word)
                    f.write(word + '\n')
        return unknowns
    
    def unknownPredTags(self, train_preds, train_tags, dev_preds, dev_tags, unk_preds, output_file=None, decode=False):
        train_preds_flat = [word for sent in train_preds for word in sent]
        train_tags_flat = [tag for sent in train_tags for tag in sent]
        train_pair_set = set([pair for pair in zip(train_preds_flat, train_tags_flat)])
        
        dev_preds_flat = [word for sent in dev_preds for word in sent]
        dev_tags_flat = [tag for sent in dev_tags for tag in sent]
        dev_pair_set = set([pair for pair in zip(dev_preds_flat, dev_tags_flat) if pair[0] not in unk_preds])
        
        unknowns = dev_pair_set.difference(train_pair_set)
        if output_file is not None:
            with open(output_file, 'w') as f:
                for word, tag in unknowns:
                    if decode:
                        word = Decoder.decode(word)
                        tag = Decoder.decode(tag)
                    f.write('{}|{}\n'.format(word, tag))
        return unknowns

class UnknownPredTags(object):
    def __init__(self, unk_preds_file, unk_predtags_file):
        self.unk_preds = self.loadUnkPreds(unk_preds_file)
        self.unk_predtags = self.loadUnkPredTags(unk_predtags_file)
    
    def loadUnkPreds(self, unk_preds_file):
        unk_preds = []
        with open(unk_preds_file, 'r') as f:
            for unk_pred in f:
                unk_preds.append(unk_pred.strip())
        return set(unk_preds)
    
    def loadUnkPredTags(self, unk_predtags_file):
        unk_predtags = []
        with open(unk_predtags_file, 'r') as f:
            for unk_predtag in f:
                pred, tag = unk_predtag.strip().split('|')
                unk_predtags.append((pred, tag))
        return set(unk_predtags)

class UnkTensorBuilder(object):
    def __init__(self, unk_preds_file, unk_predtags_file):
        self.unk_predtags = UnknownPredTags(unk_preds_file, unk_predtags_file)
    
    def unkPredTensor(self, preds, max_tokens):
        num_sents = len(preds)
        result = []
    
        for i in range(num_sents):
            if len(preds[i]) > max_tokens:
                continue
            sent = np.zeros(max_tokens)
            for j in range(len(preds[i])):
                if preds[i][j] in self.unk_predtags.unk_preds:
                    sent[j] = 1
            result.append(sent)
        return np.asarray(result).astype(bool)
    
    def unkPredTagTensor(self, preds, tags, max_tokens):
        num_sents = len(preds)
        result = []
    
        for i in range(num_sents):
            if len(preds[i]) > max_tokens:
                continue
            sent = np.zeros(max_tokens)
            for j in range(len(preds[i])):
                if (preds[i][j], tags[i][j]) in self.unk_predtags.unk_predtags:
                    sent[j] = 1
            result.append(sent)
        return np.asarray(result).astype(bool)

if __name__=='__main__':
    unknown_pred_file = '../data/unknown_preds_decode'
    unknown_predtags_file = '../data/unknown_predtags_decode'
    feat_order = FeatOrders.featOrder('allrel')
    str_feats = FeatOrders.strFeats()
    train_word_feats, dev_word_feats = AnnotSeqReader().load_data("../data/train_hypertagger", "../data/dev_hypertagger", feat_order, str_feats)
    
#     with open('../output/dev_expected.txt', 'w') as f:
#         exp_tags = dev_word_feats.hypertag
#         num_sents = num_tokens = 0
#         for sent in exp_tags:
#             for i in range(len(sent)):
#                 if len(sent) <= 180 and sent[i] is not None and sent[i] != '':
#                     f.write(sent[i] + '\n')
#                     num_tokens += 1
#             f.write('\n')
#             num_sents += 1
#     print(num_sents, num_tokens)
#     sys.exit()
    
    finder = UnknownFinder()
    unknown_preds = finder.unknownPreds(train_word_feats.PN, dev_word_feats.PN, unknown_pred_file, True)
    unknown_predtags = finder.unknownPredTags(train_word_feats.PN, train_word_feats.hypertag,
                                               dev_word_feats.PN, dev_word_feats.hypertag, unknown_preds, unknown_predtags_file, True)
    
    unk_predtags = UnknownPredTags(unknown_pred_file, unknown_predtags_file)    
    print(len(unknown_preds) == len(unk_predtags.unk_preds))
    print(len(unknown_predtags) == len(unk_predtags.unk_predtags))
