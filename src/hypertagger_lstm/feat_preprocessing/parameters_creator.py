from features import *
from parameters import Parameters
from collections import Counter

class ParametersCreator(object):
    def __init__(self):
        self.spec_preds = ["PERF","PASS","PROG"]
    
    def get_hypertag_embeddings(self, feat_order, str_feats, train_word_feats, tag_freq_thresh=0):        
        word_embedding_file = "data/embeddings.raw"
        word_embeddings = TurianEmbeddingSpace(word_embedding_file, spec_preds=self.spec_preds)
#         word_embedding_file = "data/final_vocab.p"
#         word_embeddings = PickledEmbeddingSpace(word_embedding_file, self.spec_preds)
        
        embedding_spaces = []
        for feat in feat_order:
            if feat == "hypertag":
                embedding_spaces.append((feat, self.get_hypertag_feat_space(train_word_feats, tag_freq_thresh)))
            elif feat in str_feats:
                embedding_spaces.append((feat, word_embeddings))
            elif feat.startswith('prefix'):
                embedding_spaces.append((feat, self.get_prefix_space(train_word_feats, feat)))
            elif feat.startswith('suffix'):
                embedding_spaces.append((feat, self.get_suffix_space(train_word_feats, feat)))
            else:
                embedding_spaces.append((feat, self.get_categorical_feat_space(train_word_feats, feat)))
        return Parameters(embedding_spaces)
    
    def get_hypertag_feat_space(self, train_word_feats, tag_freq_thresh):
        feat_values = getattr(train_word_feats, "hypertag")
        feat_counts = Counter([value for feat_list in feat_values for value in feat_list])
        poss_feat_values = [val for val in feat_counts.keys() if feat_counts[val] > tag_freq_thresh]
        return HypertagFeatureSpace(poss_feat_values)
    
    def get_categorical_feat_space(self, train_word_feats, feat):
        feat_values = getattr(train_word_feats, feat)
        flat_feat_values = [value for feat_list in feat_values for value in feat_list]
        poss_feat_values = set(flat_feat_values)
        return CategoricalFeatureSpace(poss_feat_values)
    
    def get_prefix_space(self, train_word_feats, feat):
        n = int(feat[-1])
        sentences = getattr(train_word_feats, 'PN')
        return EmpiricalPrefixSpace(n, sentences, just_tokens=True)
    
    def get_suffix_space(self, train_word_feats, feat):
        n = int(feat[-1])
        sentences = getattr(train_word_feats, 'PN')
        return EmpiricalSuffixSpace(n, sentences, just_tokens=True)
