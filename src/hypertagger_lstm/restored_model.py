import numpy as np
import tensorflow as tf
from definitions import PROJ_DIR

class RestoredHypertagger(object):
    def __init__(self, supertag_space):
        self.supertag_space = supertag_space
        
        self.restore_dir = PROJ_DIR + '/logs-allrel-engparen/default/'
        self.saver = tf.train.import_meta_graph(self.restore_dir + 'model.ckpt-45269.meta')
        self.session = tf.Session()
        self.saver.restore(self.session, self.restore_dir + 'model.ckpt-45269')
        
        self.x = self.session.graph.get_tensor_by_name("model_1/inputs/x:0")
        self.num_tokens = self.session.graph.get_tensor_by_name("model_1/inputs/num_tokens:0")
        self.scores = self.session.graph.get_tensor_by_name("model_1/prediction/scores:0")
            
    def getTagMaps(self, x, num_tokens, weights, max_beta):
        with self.session.graph.as_default():
            scores = self.session.run(self.scores, {self.x: x, self.num_tokens: num_tokens})
            prob_dists = self.session.run(tf.nn.softmax(scores))
            
        tag_maps = []
        for i in range(prob_dists.shape[0]):
            sent_prob_dists = prob_dists[i,:,:]
            sent_weights = weights[i,:]
            sent_tag_maps = self.getSentTagMaps(sent_prob_dists, sent_weights, max_beta)
            tag_maps.append(sent_tag_maps)
        return tag_maps

    def getSentTagMaps(self, sent_prob_dists, sent_weights, beta):
        ''' sent_prob_dists: tag probability distributions for tokens in this sentence '''
        sent_tag_maps = []
        for i in range(sent_prob_dists.shape[0]):
            if sent_weights[i] == 0:
                sent_tag_maps.append({})
                continue
            token_prob_dist = sent_prob_dists[i,:]
            token_tag_map = self.getTokenTagMap(token_prob_dist, beta)
            sent_tag_maps.append(token_tag_map)
        return sent_tag_maps
    
    def getTokenTagMap(self, token_prob_dist, beta):
        token_tag_map = {}
        sorted_i = np.argsort(-token_prob_dist)
        thresh_prob = beta*token_prob_dist[sorted_i[0]]
        
        i = 0
        while i < token_prob_dist.shape[0] and token_prob_dist[sorted_i[i]] >= thresh_prob:
            tag = self.supertag_space.feature(sorted_i[i])
            prob = token_prob_dist[sorted_i[i]]
            token_tag_map[tag] = prob
            i += 1
        return token_tag_map
