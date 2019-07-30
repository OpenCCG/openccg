#!/usr/bin/env python
import os
import logging
import threading
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import tensorflow as tf
import custom_init_ops

from evaluation import *
from util import *
from model import *
from config import *
from new_eval.stats_comp import StatsComponentCreator

class SupertaggerTrainer(object):
    ### MODIFIED FOR HYPERTAGGER ###
    def __init__(self, logdir, feat_order_name, lin_order_name):
    ### MODIFIED FOR HYPERTAGGER ###
        self.logdir = logdir
        self.writer = tf.train.SummaryWriter(logdir, flush_secs=20)
        ### ADDED FOR HYPERTAGGER ###
        self.lossvect = []
        self.stats_comp_creator = StatsComponentCreator()
        self.feat_order_name = feat_order_name
        self.lin_order_name = lin_order_name
        ### ADDED FOR HYPERTAGGER ###

    def train(self, config, data, params):
        with tf.Session() as session, Timer("Training") as timer:
            with tf.variable_scope("model", initializer=custom_init_ops.dyer_initializer()):
                train_model = SupertaggerModel(config, data, is_training=True)

            with tf.variable_scope("model", reuse=True):
                dev_model = SupertaggerModel(config, data, is_training=False)

            session.run(tf.initialize_all_variables())

            with tf.variable_scope("model", reuse=True):
                params.assign_pretrained(session)

            population_thread = threading.Thread(target=data.populate_train_queue, args=(session, train_model))
            population_thread.start()
            
            ### ADDED FOR HYPERTAGGER ###
            train_eval = self.stats_comp_creator.getOldStatsLogger(session, data, params, dev_model, is_training=True)
            dev_eval = self.stats_comp_creator.getOldStatsLogger(session, data, params, dev_model)
            tag_writer = self.stats_comp_creator.getHypertagWriter(data.supertag_space.space, 'dev', data.dev_data[4], 0, 'output/dev_predictions.txt')
            train_dbtagwriter = self.stats_comp_creator.getDbTagWriter(1, True, data, params)
            dev_dbtagwriter = self.stats_comp_creator.getDbTagWriter(1, False, data, params)
            ### ADDED FOR HYPERTAGGER ###
            
            ### MODIFIED FOR HYPERTAGGER ###
#             evaluator = SupertaggerEvaluator(session, data.supertag_space.space, data.dev_sent_lens, data.dev_data, 
#                                              dev_model, train_model.global_step, self.writer, self.logdir)
            evaluator = SupertaggerEvaluator(dev_eval, tag_writer, dev_dbtagwriter, self.feat_order_name, self.lin_order_name,
                                              train_model.global_step, self.writer, self.logdir)
            ### MODIFIED FOR HYPERTAGGER ###

            i = 0
            epoch = 0
            train_loss = 0.0

            # Evaluator tells us if we should stop.
            while evaluator.maybe_evaluate(epoch):
                i += 1
                
                _, loss = session.run([train_model.optimize,
                                       train_model.loss])
                train_loss += loss
                if i % 100 == 0:
                    timer.tick("{} training steps".format(i))


                if i >= len(data.train_word_feats.PN)/data.batch_size:
                    train_loss = train_loss / i
                    logging.info("Epoch {} complete(steps={}, loss={:.3f}).".format(epoch, i, train_loss))
                    self.writer.add_summary(tf.Summary(value=[tf.Summary.Value(tag="Train Loss", simple_value=train_loss)]),
                                            tf.train.global_step(session, train_model.global_step))
                    ### ADDED FOR HYPERTAGGER ###
                    self.lossvect.append(train_loss)
                    ### ADDED FOR HYPERTAGGER ###
                    i = 0
                    epoch += 1
                    train_loss = 0.0
            
                    ### ADDED FOR HYPERTAGGER ###
                    # Calculate training accuracy
                    train_eval.eval(epoch)
                    assign_tags = train_eval.assignedTags()
                    tagcompare_file = "output/train_tagcompare_" + self.feat_order_name + "_" + self.lin_order_name + ".csv"
                    feat_file = "output/train_featvals_" + self.feat_order_name + "_" + self.lin_order_name + ".csv"
                    wordprops_file = "output/train_wordprops_" + self.feat_order_name + "_" + self.lin_order_name + ".csv"
                    train_dbtagwriter.writeCSV(tagcompare_file, feat_file, wordprops_file, assign_tags)
                    
                    # Graph loss function
                    dev_eval.plotLoss(self.lossvect)
                    ### ADDED FOR HYPERTAGGER ###
