import tensorflow as tf
from util import Timer

class LSTMRunner(object):
    ''' Runs LSTM model and stores results '''
    
    def __init__(self, session, model_type, model, X, num_tokens):
        self.model_type = model_type
        self.model = model
        self.session = session
        self.X = X
        self.num_tokens = num_tokens
        self.prob_dists = None
    
    def run(self):
        with Timer(self.model_type + " evaluation"):
            scores = self.session.run(self.model.scores, {
                self.model.x: self.X,
                self.model.num_tokens: self.num_tokens
            })
        self.prob_dists = scores # self.session.run(tf.nn.softmax(scores))
