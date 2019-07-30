import numpy as np

class Tensorizer(object):
    max_tokens = 102
    
    def __init__(self, supertag_space, embedding_spaces, word_feats, parens=False):
        if parens:
            self.max_tokens = 180
        self.nonwords = ['(', ')']
        self.supertag_space = supertag_space               
        self.embedding_spaces = embedding_spaces
        self.embedding_spaces['hypertag'].setSpace(self.supertag_space.space)
        
        self.word_feats = word_feats
        self.tensors = self.get_data(word_feats)
    
    def get_data(self, word_feats):
        '''
        word_feats: WordFeats object containing feature values for all sentences
        returns: List of 5 tensors that meet following descriptions
            First is tensor of feature value indices with shape (num_sentences, max_sentence_length, num_features). Indices should be between 0 and num_feature_values for associated feature.
            Second is tensor of expected hypertag indices with shape (num_sentences, max_sentence_length). Indices should be between 0 and num_hypertags.
            Third is tensor of sentence lengths, which should all be below max_sentence_length
            Fourth indicates whether example is from tritrain set and can be ignored for the hypertagger
            Last indicates whether each token in sentence is an actual token (as opposed to a dummy start or end token added in the code)
        '''
        tensors = []
        for i in range(len(word_feats.PN)): # For each sentence
            sent_feats = [getattr(word_feats, feat)[i] for feat in word_feats.word_feat_order]
            sent_tensors = self.tensorize(sent_feats)
            tensors.append(sent_tensors)
        results = [np.array(v) for v in zip(*(t for t in tensors if t is not None))]
        return results
    
    def tensorize(self, sent_feats):
        ''' sent_feats: List of lists containing feature values for sentence. Each list contains values for one particular feature. '''
        
        sent_length = len(sent_feats[0])
        num_feats = len(sent_feats)
        if sent_length > self.max_tokens:
            return None

        embed_indices = []
        for i in range(sent_length):
            word_feat_vect = [sent_feats[j][i] for j in range(num_feats)]
            word_embed_indices = self.get_embedding_indexes(word_feat_vect)
            embed_indices.append(word_embed_indices)
        embed_matrix = np.array(embed_indices)
        embed_matrix.resize([self.max_tokens, num_feats], refcheck=False)
        y = embed_matrix[:,1]
        x = np.delete(embed_matrix, 1, 1)

        # None labels and hierarchy parentheses should have 0 weight.
        weights = (y > 0).astype(float)
        self.unweight_nonwords(weights, sent_feats[0], sent_feats[1])
        return x, np.absolute(y), sent_length, 0, weights
    
    def get_embedding_indexes(self, word_feat_vect):
        ''' word_feat_vect: List of feature values for one word '''
        spaces = self.embedding_spaces.values()
        num_feats = len(word_feat_vect)
        return [spaces[i].index(word_feat_vect[i]) for i in range(num_feats)]
    
    def unweight_nonwords(self, weights, preds, tags):
        for i in range(len(preds)):
            if preds[i] in self.nonwords and tags[i] == '':
                weights[i] = 0
    