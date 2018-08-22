#!/usr/bin/env python

import math
import collections
import logging
import itertools

import numpy as np
import random

from features import *
from ccgbank import *
from util import *
from data_stats.unknowns import UnkTensorBuilder

class SupertaggerData(object):
    max_tokens = 102
    batch_size = 32
    bucket_size = 5
    max_tritrain_length = 72

    def __init__(self, supertag_space, embedding_spaces, train_sentences, tritrain_sentences, dev_sentences):
        self.supertag_space = supertag_space
        self.embedding_spaces = embedding_spaces

        logging.info("Number of supertags: {}".format(self.supertag_space.size()))
        for name, space in self.embedding_spaces.items():
            logging.info("Number of {}: {}".format(name, space.size()))

        logging.info("Train sentences: {}".format(len(train_sentences)))
        logging.info("Tri-train sentences: {}".format(len(tritrain_sentences)))
        logging.info("Dev sentences: {}".format(len(dev_sentences)))

        if len(tritrain_sentences) > 0:
            self.tritrain_ratio = 15
        else:
            self.tritrain_ratio = 1

        logging.info("Massaging data into input format...")
        self.train_sentences = train_sentences
        self.tritrain_sentences = tritrain_sentences
        self.dev_data = self.get_data(dev_sentences)

    def format_distribution(self, distribution):
        return ",".join("{:.5f}".format(p) for p in distribution)

    def get_bucket(self, sentence_length):
        # Don't count <s> and </s>.
        return max(sentence_length - 1 - 2, 0)/self.bucket_size

    def get_sentence_length_distribution(self, sentences):
        counts = collections.Counter((self.get_bucket(len(s[0])) for s in sentences))
        buckets = [counts[i] for i in range(self.max_tritrain_length/self.bucket_size)]
        buckets_sum = float(sum(buckets))
        return [b/buckets_sum for b in buckets]

    def get_embedding_indexes(self, token):
        return [space.index(space.extract(token)) for space in self.embedding_spaces.values()]

    def tensorize(self, sentence):
        tokens, supertags, is_tritrain = sentence

        if len(tokens) != len(supertags):
            raise ValueError("Number of tokens ({}) should match number of supertags ({}).".format(len(tokens), len(supertags)))
        if len(tokens) > self.max_tokens:
            logging.info("Skipping sentence of length {}.".format(len(tokens)))
            return None

        x = np.array([self.get_embedding_indexes(t) for t in tokens])
        y = np.array([self.supertag_space.index(s) for s in supertags])

        # None labels should have 0 weight.
        weights = (y >= 0).astype(float)

        x.resize([self.max_tokens, x.shape[1]])
        y.resize([self.max_tokens])
        weights.resize([self.max_tokens])
        return x, np.absolute(y), len(tokens), int(is_tritrain), weights

    def populate_train_queue(self, session, model):
        i = 0
        tritrain_probability = len(self.tritrain_sentences)/float(len(self.tritrain_sentences) + 15)
        while True:
            if np.random.rand() > tritrain_probability:
                s = random.choice(self.train_sentences)
            else:
                s = random.choice(self.tritrain_sentences)
            tensors = self.tensorize(s)
            if tensors is not None:
                session.run(model.input_enqueue, { i:t for i,t in zip(model.inputs, tensors) })
                i += 1
                if i % 10000 == 0:
                    logging.info("Queued {} sentences.".format(i))

    def get_data(self, sentences):
        tensors = (self.tensorize(s) for s in sentences)
        results = [np.array(v) for v in zip(*(t for t in tensors if t is not None))]
        return results

### ADDED FOR HYPERTAGGER ###
class HypertaggerData(object):
    max_tokens = 102
    batch_size = 32
    
    def __init__(self, supertag_space, embedding_spaces, train_word_feats, dev_word_feats, db_id_getter, parens=False):
        if parens:
            self.max_tokens = 180
        self.db_id_getter = db_id_getter
        self.nonwords = ['(', ')']
        self.supertag_space = supertag_space               
        self.embedding_spaces = embedding_spaces
        self.embedding_spaces['hypertag'].setSpace(self.supertag_space.space)
        
        dev_num_unk_tags = self.count_unk_tags(dev_word_feats.hypertag, supertag_space.space)
        logging.info('Dev num words with unk tags: {}'.format(dev_num_unk_tags))
        train_num_unk_tags = self.count_unk_tags(train_word_feats.hypertag, supertag_space.space)
        logging.info('Train num words with unk tags: {}'.format(train_num_unk_tags))
        self.max_indices = []
        
        for name, space in self.embedding_spaces.items():
            self.max_indices.append(space.size)
            logging.info("Number of {}: {}".format(name, space.size()))
            if space.size() < 20:
                logging.info(space.space)
        self.tritrain_ratio = 1

        logging.info("Massaging data into input format...")
        self.dev_word_feats = dev_word_feats
        self.train_word_feats = train_word_feats
        unk_tensor_builder = UnkTensorBuilder('data/unknown_preds_hypertagger', 'data/unknown_predtags_hypertagger')
        self.unk_preds = unk_tensor_builder.unkPredTensor(dev_word_feats.PN, self.max_tokens)
        self.unk_predtags = unk_tensor_builder.unkPredTagTensor(dev_word_feats.PN, dev_word_feats.hypertag, self.max_tokens)
        
        self.dev_data = self.get_data(dev_word_feats, 'dev')
        dx, dy, dn, _, dw = self.dev_data
        logging.info("Dev data ndarrays: {}, {}, {}, {}".format(dx.shape, dy.shape, np.sum(dn), np.sum(dw)))
        self.log_unk_preds(dev_word_feats.PN, dx, dn)
        
        self.train_data = self.get_data(train_word_feats, 'train')
        tx, ty, tn, _, tw = self.train_data
        logging.info("Train data ndarrays: {}, {}, {}, {}".format(tx.shape, ty.shape, np.sum(tn), np.sum(tw)))        
    
    def count_unk_tags(self, tags, tag_space):
        count = 0
        for sent in tags:
            for tag in sent:
                if tag not in tag_space or tag == '*UNKNOWN*':
                    count += 1
        return count
    
    def log_unk_preds(self, preds, x, num_tokens):
        unk_pred_inds = [(i,j) for i, sent in enumerate(x) for j, pred_i in enumerate(sent) if num_tokens[i] > j and pred_i[0] == 0]
        logging.info('Number of unknown preds: {}'.format(len(unk_pred_inds)))
#         unk_preds = [preds[i][j] for i,j in unk_pred_inds if preds[i][j] not in self.nonwords]
#         unk_pred_counts = collections.Counter(unk_preds)
#         logging.info('Unknown predicates: {}'.format(unk_pred_counts))
    
    def get_data(self, word_feats, feat_set):
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
            if feat_set == 'dev' and sent_tensors is not None:
                self.db_id_getter.addDevSentID(i)
#             elif feat_set == 'train' and sent_tensors is not None:
#                 self.db_id_getter.addTrainSentID(i)
            tensors.append(sent_tensors)
        results = [np.array(v) for v in zip(*(t for t in tensors if t is not None))]
        return results
    
    def tensorize(self, sent_feats):
        ''' sent_feats: List of lists containing feature values for sentence. Each list contains values for one particular feature. '''
        
        sent_length = len(sent_feats[0])
        num_nonwords = len([word for word in sent_feats[0] if word in self.nonwords])
        num_feats = len(sent_feats)
        if sent_length > self.max_tokens:
            logging.info("Skipping sentence of length {} with {} nonwords.".format(sent_length, num_nonwords))
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
        sum_w = np.sum(weights)
        if sum_w != sent_length-2:
            logging.info('Should have {} weighted tokens before unweighting but has {}'.format(sent_length-2, sum_w))
        self.unweight_nonwords(weights, sent_feats[0], sent_feats[1])
        sum_w = np.sum(weights)
        if sum_w != sent_length-num_nonwords-2:
            logging.info('Should have {} weighted tokens after unweighting but has {}'.format(sent_length-num_nonwords-2, sum_w))
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
    
    def populate_train_queue(self, session, model):
        i = 0
        num_sents = len(self.train_word_feats.PN)
        for feat in self.train_word_feats.word_feat_order:
            if len(getattr(self.train_word_feats, feat)) != num_sents:
                logging.info("Feature lists don't have same length")
        while True:
            sent_index = random.randint(0, num_sents-1)
            sent_feats = [getattr(self.train_word_feats, feat)[sent_index] for feat in self.train_word_feats.word_feat_order]
            tensors = self.tensorize(sent_feats)
            if tensors is not None:
                session.run(model.input_enqueue, { i:t for i,t in zip(model.inputs, tensors) })
                i += 1
                if i % 10000 == 0:
                    logging.info("Queued {} sentences.".format(i))
    
    # TODO: Maybe replace original populate_train_queue with this
    def populate_train_queue2(self, session, model):
        i = 0
        num_sents = self.train_data[0].shape[0]
        while True:
            sent_index = random.randint(0, num_sents-1)
            x = self.train_data[0][sent_index,:,:]
            y = self.train_data[1][sent_index,:]
            num_tokens = self.train_data[2][sent_index]
            tritrain = self.train_data[3][sent_index]
            weights = self.train_data[4][sent_index,:]
            tensors = [x, y, num_tokens, tritrain, weights]
            session.run(model.input_enqueue, { i:t for i,t in zip(model.inputs, tensors) })
            i += 1
            if i % 10000 == 0:
                logging.info("Queued {} sentences.".format(i))
### ADDED FOR HYPERTAGGER ###