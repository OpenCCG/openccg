import logging
from util import Timer
import tensorflow as tf
import numpy as np
import collections
import matplotlib.pyplot as plt

class OldStatsLogger(object):
    def __init__(self, session, hypertag_space, feat_spaces, index_translater, X, Y, num_tokens, weights,
                  unk_preds, unk_predtags, model, model_type):
        '''
        session: Tensorflow Session
        hypertag_space: Set of possible supertags
        feat_spaces: Map of feature to set of possible values
        index_translater: Map of feature to list of lists of feature values
        X: 3D matrix of feature indices
        Y: 2D matrix of expected supertags
        num_tokens: Vector of sentence lengths
        weights: 2D matrix of indicators for whether each word should count in accuracy
        model: Tensorflow LSTM
        model_type: training or dev
        '''
        self.session = session
        self.tag_space = hypertag_space
        self.feat_spaces = feat_spaces
        self.index_translater = index_translater
        self.X = X
        self.Y = Y
        self.num_tokens = num_tokens
        self.weights = weights
        self.unk_preds = unk_preds
        self.unk_predtags = unk_predtags
        self.model = model
        self.model_type = model_type
        self.prob_dists = None
        self.last_predictions = None
        self.betas = []
    
    def assignedTags(self):
        return self.last_predictions
    
    def eval(self, epoch):
        overall = self.evalOverall()
        self.evalAccuracyFromNonwords()
        if self.model_type.lower() != 'train':
            self.evalUnknownPreds()
            self.evalUnknownPredTags()
        self.evalLinPos()
        self.evalConfMat()
        return overall
    
    def evalOverall(self):
        with Timer(self.model_type + " evaluation"):
            scores = self.session.run(self.model.scores, {
                self.model.x: self.X,
                self.model.num_tokens: self.num_tokens
            })
#         self.prob_dists = self.session.run(tf.nn.softmax(scores))
        self.prob_dists = scores
        predictions = np.argmax(scores, 2)
        self.last_predictions = predictions
        
        unk_ind = self.feat_spaces['hypertag'].index('*UNKNOWN*')
        logging.info('Unknown tag index: {}'.format(unk_ind))
        unks_exp = np.where(self.Y == unk_ind)
        unks_exp = set([(i,j) for i, j in zip(unks_exp[0], unks_exp[1])])
        logging.info('Number of unknown tags expected: {}'.format(len(unks_exp)))
        unks_act = np.where(predictions == unk_ind)
        unks_act = set([(i,j) for i, j in zip(unks_act[0], unks_act[1])])
        logging.info('Number of unknown tags predicted: {}'.format(len(unks_act)))
        num_unk_correct = len(unks_exp.intersection(unks_act))
        logging.info('Number of matching unknown tags: {}'.format(num_unk_correct))
        
        num_correct = np.sum(np.equal(predictions, self.Y) * self.weights) - num_unk_correct
        num_total = np.sum(self.weights)
        accuracy = (100.0 * num_correct)/num_total
        logging.info("Overall accuracy: {:.3f}% ({}/{})".format(accuracy, num_correct, num_total))
        return accuracy
    
    def evalAccuracyFromNonwords(self):
        exp_i = [i for sent in self.Y for i in sent]
        exp_tag = [self.tag_space[i] for i in exp_i]
        act_i = [i for sent in self.last_predictions for i in sent]
        w = [weight for sent in self.weights for weight in sent]
        
        nonword_indices = [i for i, tag in enumerate(exp_tag) if tag == '']
        nonword_exp = [exp_i[i] for i in nonword_indices]
        nonword_act = [act_i[i] for i in nonword_indices]
        nonword_w = [w[i] for i in nonword_indices]
        num_correct = np.sum(np.equal(nonword_act, nonword_exp) * nonword_w)
        num_total = np.sum(nonword_w)
        accuracy = (100.0 * num_correct)/num_total
        logging.info("Accuracy from non-words: {:.3f}% ({}/{})".format(accuracy, num_correct, num_total))
    
    def evalUnknownPreds(self):
        preds = self.X[:,:,0].tolist()
        sent_lens = self.num_tokens
        unk_pred_inds = [(pred_i,i,j) for i, sent in enumerate(preds) \
                          for j, pred_i in enumerate(sent) if sent_lens[i] > j and self.unk_preds[i,j]]
        total = len(unk_pred_inds)
        correct = len([pred_i for i, sent in enumerate(preds) for j, pred_i in enumerate(sent) \
                       if sent_lens[i] > j and self.unk_preds[i,j] and self.Y[i,j] == self.last_predictions[i,j]])
        accuracy = (100.0 * correct)/total
        logging.info("Accuracy on preds in dev but not train: {:.3f}% ({}/{})".format(accuracy, correct, total))
    
    def evalUnknownPredTags(self):
        preds = self.X[:,:,0].tolist()
        sent_lens = self.num_tokens
        unk_pred_inds = [(pred_i,i,j) for i, sent in enumerate(preds) \
                          for j, pred_i in enumerate(sent) if sent_lens[i] > j and self.unk_predtags[i,j]]
        total = len(unk_pred_inds)
        correct = len([pred_i for i, sent in enumerate(preds) for j, pred_i in enumerate(sent) \
                       if sent_lens[i] > j and self.unk_predtags[i,j] and self.Y[i,j] == self.last_predictions[i,j]])
        accuracy = (100.0 * correct)/total
        logging.info("Accuracy on pred-tag pairs in dev but not train: {:.3f}% ({}/{})".format(accuracy, correct, total))
    
    def evalLinPos(self):
        for i in range(7):
            col_num = i*5 + 1
            exp_col = self.Y[:,col_num].tolist()
            act_col = self.last_predictions[:,col_num].tolist()
            num_sents = len([j for j,_ in enumerate(exp_col) if self.num_tokens[j] > col_num])
            num_correct = len([j for j,_ in enumerate(exp_col) if self.num_tokens[j] > col_num and exp_col[j] == act_col[j]])
            accuracy = (100.0 * num_correct)/num_sents
            logging.info("Accuracy for {}th column: {:.3f}% ({}/{})".format(col_num, accuracy, num_correct, num_sents))
    
    def evalConfMat(self):
        # Get confusion matrix entries
        exp_tags = [self.tag_space[i] for sent in self.Y for i in sent]
        act_tags = [self.tag_space[i] for sent in self.last_predictions for i in sent]
        conf_mat = collections.Counter(zip(exp_tags, act_tags))
        sorted_conf_mat = sorted(conf_mat, key=conf_mat.get, reverse=True)
        most_wrong = [(exp_tag, act_tag) for exp_tag, act_tag in sorted_conf_mat if exp_tag != act_tag and exp_tag is not None and exp_tag != ""][:10]
        del sorted_conf_mat
        
        # Get examples of wrong words and sentences
        sent_length = self.X.shape[1]
        word_space = self.feat_spaces['PN']
        words = [word_space[i] for row in self.X[:,:,0] for i in row]
        wrong_words = []
        for i in range(len(exp_tags)):
            if exp_tags[i] != act_tags[i] and exp_tags[i] is not None:
                wrong_word = words[i]
                wrong_sent_indices = self.X[i // sent_length,:,0]
                wrong_sent = [word_space[i] for i in wrong_sent_indices]
                tag_pair = (exp_tags[i], act_tags[i])
                ex_pair = (wrong_word, wrong_sent)
                wrong_words.append((tag_pair, ex_pair))
        wrong_words_map = {}
        for tag_pair, exp_pair in wrong_words:
            wrong_words_map.setdefault(tag_pair, []).append(exp_pair)
        del wrong_words
    
        # Log information
        logging.info("******************************")
        logging.info(self.model_type + " Confusion Matrix")
        for i in range(len(most_wrong)):
            logging.info("{}\t{}".format(most_wrong[i], conf_mat.get(most_wrong[i])))
#             wrong_word, wrong_sent = wrong_words_map[most_wrong[i]][0]
#             logging.info("Ex: {}\t{}".format(wrong_word, wrong_sent))
        logging.info("******************************")
    
    def plotLoss(self, loss_vect):
        logging.info("Plotting loss function")
        plt.plot(loss_vect)
        plt.xlabel("Epoch")
        plt.ylabel("Loss")
        plt.savefig('output/lossfun.png', bbox_inches='tight')
        logging.info("Plot saved to output/lossfun.png")
