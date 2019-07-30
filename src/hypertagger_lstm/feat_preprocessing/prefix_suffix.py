from features import EmpiricalSuffixSpace, EmpiricalPrefixSpace
from collections import OrderedDict

class PreSufSpaces(object):
    def __init__(self, presuf_feats, train_words):
        self.spaces = OrderedDict()
        for feat in presuf_feats:
            n = int(feat[feat.index('_') + 1:])
            presuf = feat[:feat.index('_')]
            if presuf == 'prefix':
                self.spaces[feat] = EmpiricalPrefixSpace(n, train_words, just_tokens=True)
            else:
                self.spaces[feat] = EmpiricalSuffixSpace(n, train_words, just_tokens=True)

class PreSufFeatAdder(object):
    def addPresufFeats(self, presuf_feats, presuf_spaces, train_word_feats, dev_word_feats):
        '''
        presuf_feats: List of strings in format 'prefix_<number>' or 'suffix_<number>'
        presuf_spaces: List of PrefixSpace and SuffixSpace objects
        train_word_feats: WordFeatures object containing training data
        dev_word_feats: WordFeatures object containing dev data
        '''
        train_word_feats.word_feat_order += presuf_feats # Due to aliasing adding presuf_feats only needs to be done once
        for feat in presuf_feats:
            space = presuf_spaces[feat]
            train_feat = [[space.extract(token) for token in sent] for sent in train_word_feats.PN]
            dev_feat = [[space.extract(token) for token in sent] for sent in dev_word_feats.PN]
            setattr(train_word_feats, feat, train_feat)
            setattr(dev_word_feats, feat, dev_feat)
    
    def addPresufSpaces(self, presuf_spaces, params):
        '''
        presuf_spaces: List of PrefixSpace and SuffixSpace objects
        params: Parameters object
        '''
        params.embedding_spaces.update(presuf_spaces)
