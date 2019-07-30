from collections import Counter

class ConfusionMatrix(object):
    def __init__(self, index_translater, X, num_tokens):
        self.conf_mat = Counter()
        self.index_translater = index_translater
        self.X = X
        self.num_tokens = num_tokens
        self.wrong_sents = {}
    
    def clear(self):
        self.conf_mat = Counter()
        self.wrong_sents = {}
    
    def process(self, exp, act):
        self.clear()
        for i in range(exp.shape[0]):
            for j in range(self.num_tokens[i]):
                if exp[i,j] != act[i,j]:
                    self.updateConfMat(exp[i, j], act[i, j], i, j, self.num_tokens[i])
    
    def updateConfMat(self, exp_tag_i, act_tag_i, sent_i, word_i, num_words):
        exp_tag = self.index_translater.tagAtIndex(exp_tag_i)
        act_tag = self.index_translater.tagAtIndex(act_tag_i)
        self.conf_mat[(exp_tag, act_tag)] += 1
        wrong_sent = self.getSentence(sent_i, word_i, num_words)
        if (exp_tag, act_tag) in self.wrong_sents:
            self.wrong_sents[(exp_tag, act_tag)].append(wrong_sent)
        else:
            self.wrong_sents[(exp_tag, act_tag)] = [wrong_sent]
    
    def getSentence(self, sent_i, word_i, num_words):
        ''' Returns sentence with wrong tag. Word with wrong tag has *'s at beginning and end. '''
        
        sent = ''
        for i in range(num_words):
            index = self.X[sent_i, i, 0]
            if i == word_i:
                sent += '*'
            sent += self.index_translater.wordAtIndex(index)
            if i == word_i:
                sent += '*'
            sent += ' '
        return sent
    
    def mostMissed(self, n):
        sorted_keys = sorted(self.conf_mat, key=self.conf_mat.get, reverse=True)
        return sorted_keys[:n]
    
    def count(self, tag_pair):
        return self.conf_mat[tag_pair]
    
    def getExamples(self, tag_pair, n):
        examples = self.wrong_sents[tag_pair]
        return examples[:n]
