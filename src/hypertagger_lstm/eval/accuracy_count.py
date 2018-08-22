import numpy as np

class AccuracyCounter(object):
    ''' Handles calculation of accuracy '''

    def __init__(self, weights, countCorrect):
        self.total_count = 0
        self.correct_count = 0
        self.weights = weights
        ''' ndarray that determines which words are counted in accuracy '''
        self.countCorrect = countCorrect
        ''' Should be function that takes two ndarrays and returns integer.
        First ndarray is expected tags for each word of each sentence.
        Second ndarray is probability distribution of all possible tags for each word of each sentence. '''
    
    def process(self, expected, prob_dists):
        self.correct_count = self.countCorrect(expected, prob_dists)
        self.total_count = np.sum(self.weights)
    
    def accuracy(self):
        return 100*self.correct_count/self.total_count

# Factory for AccuracyCounters
class AccuracyCounterFactory(object):
    def __init__(self, weights):
        self.weights = weights
    
    def overallAccuracyCounter(self):
        def overallAccuracy(expected, prob_dists):
            actual = np.argmax(prob_dists, 2)
            num_correct = np.sum(np.equal(expected, actual) * self.weights)
            return num_correct
        return AccuracyCounter(self.weights, overallAccuracy)
    
    def betaBestAccuracyCounter(self, beta):
        def betaBestAccuracy(expected, prob_dists):
            num_correct = 0            
            for i in range(expected.shape[0]):
                for j in range(expected.shape[1]):
                    sorted_i = np.argsort(-prob_dists[i,j])
                    thresh_prob = beta*prob_dists[i,j,sorted_i[0]]
                    k = 0
                    while k < prob_dists.shape[2] and prob_dists[i,j,sorted_i[k]] >= thresh_prob:
                        if sorted_i[k] == expected[i,j]:
                            num_correct += 1
                            break
                        k += 1
            return num_correct
        return AccuracyCounter(self.weights, betaBestAccuracy)
    
    def unknownAccuracyCounter(self, X, num_tokens):
        # Set weight of words in training data to 0
        preds = X[:,:,0]
        w = self.weights.copy()
        for i in range(X.shape[0]):
            for j in range(num_tokens[i]):
                if preds[i,j] != 0:
                    w[i,j] = 0
        # Rest is like overall accuracy
        def unknownAccuracy(expected, prob_dists):
            actual = np.argmax(prob_dists, 2)
            num_correct = np.sum(np.equal(expected, actual) * w)
            return num_correct
        return AccuracyCounter(w, unknownAccuracy)
