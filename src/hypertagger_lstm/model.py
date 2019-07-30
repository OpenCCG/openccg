import collections
import numpy as np
import tensorflow as tf

import logging
import features

from custom_rnn_cell import *

class SupertaggerModel(object):
    lstm_hidden_size = 128
    penultimate_hidden_size = 64
    num_layers = 2

    # If variables in the computation graph are frozen, the protobuffer can be used out of the box.
    def __init__(self, config, data, is_training, max_tokens=None):
        self.data = data
        self.config = config
        self.max_tokens = max_tokens or data.max_tokens

        # Redeclare some variables for convenience.
        supertags_size = data.supertag_space.size()
        embedding_spaces = data.embedding_spaces
        num_sents = len(data.train_word_feats.PN)

        with tf.name_scope("inputs"):
            if is_training:
                x_prop = (tf.int32, [self.max_tokens, len(embedding_spaces) - 1])
                y_prop = (tf.int32, [self.max_tokens])
                num_tokens_prop = (tf.int64, [])
                tritrain_prop = (tf.float32, [])
                weights_prop = (tf.float32, [self.max_tokens])
                dtypes, shapes = zip(x_prop, y_prop, num_tokens_prop, tritrain_prop, weights_prop)
                input_queue = tf.RandomShuffleQueue(num_sents, 0, dtypes, shapes=shapes)
                self.inputs = [tf.placeholder(dtype, shape) for dtype, shape in zip(dtypes, shapes)]
                self.input_enqueue = input_queue.enqueue(self.inputs)
                self.x, self.y, self.num_tokens, self.tritrain, self.weights = input_queue.dequeue_many(data.batch_size)
            else:
                # Each training step is batched with a maximum length.
                self.x = tf.placeholder(tf.int32, [None, self.max_tokens, len(embedding_spaces) - 1], name="x")
                self.num_tokens = tf.placeholder(tf.int64, [None], name="num_tokens")

        # From feature indexes to concatenated embeddings.
        with tf.name_scope("embeddings"):
            with tf.device("/cpu:0"):
                # embeddings_w is map of feature name to associated TF variable object
                embeddings_w = collections.OrderedDict(
                    (name, tf.get_variable(name, [space.size(), space.embedding_size])) for name,
                    space in embedding_spaces.items() if name != 'hypertag')
                # Split index matrix on feature type. Each slice has shape (batch size, max sentence length, 1).
                self.splits = tf.split(2, len(embedding_spaces) - 1, self.x)
                embeddings = [tf.gather(e,i) for e,i in zip(embeddings_w.values(), self.splits)]
                logging.info('Word embeddings shape: {}'.format(embeddings[0].get_shape()))
            concat_embedding = tf.concat(3, embeddings)
            concat_embedding = tf.squeeze(concat_embedding, [2])
            logging.info('Concatenated embeddings shape: {}'.format(concat_embedding.get_shape()))
            if is_training:
                concat_embedding = tf.nn.dropout(concat_embedding, 1.0 - config.dropout_probability)

        with tf.name_scope("lstm"):
            # LSTM cell is replicated across stacks and timesteps.
            first_cell = DyerLSTMCell(self.lstm_hidden_size, concat_embedding.get_shape()[2].value)
            if self.num_layers > 1:
                stacked_cell = DyerLSTMCell(self.lstm_hidden_size, self.lstm_hidden_size)
                cell = tf.nn.rnn_cell.MultiRNNCell([first_cell] + [stacked_cell] * (self.num_layers - 1))
            else:
                cell = first_cell

            outputs, _ = tf.nn.bidirectional_dynamic_rnn(cell, cell, concat_embedding, sequence_length=self.num_tokens, dtype=tf.float32)
            outputs = tf.concat(2, outputs)
        with tf.name_scope("softmax"):
            # From LSTM outputs to logits.
            flattened = self.flatten(outputs)
            penultimate = tf.nn.relu(tf.nn.rnn_cell._linear(flattened, self.penultimate_hidden_size, bias=True, scope="penultimate"))
            logits = tf.nn.rnn_cell._linear(penultimate, supertags_size, bias=True, scope="softmax")

        with tf.name_scope("prediction"):
            self.scores = self.unflatten(logits, name="scores")

        if is_training:
            with tf.name_scope("loss"):
                modified_weights = self.weights * tf.expand_dims((1.0 - self.tritrain) +  config.tritrain_weight * self.tritrain, 1)

                """
                softmax = tf.nn.softmax(logits)
                softmax_list = [tf.squeeze(split, [1]) for split in tf.split(1, self.max_tokens, self.unflatten(softmax))]
                y_list = [tf.squeeze(split, [1]) for split in tf.split(1, self.max_tokens, self.y)]
                modified_weights_list = [tf.squeeze(split, [1]) for split in tf.split(1, self.max_tokens, modified_weights)]
                cross_entropy_list = [-tf.log(tf.gather(tf.transpose(s), y)) for s, y in zip(softmax_list, y_list)]
                cross_entropy_list = [tf.reduce_sum(ce * w) for ce, w in zip(cross_entropy_list, modified_weights_list)]
                self.loss = sum(cross_entropy_list)
                """
                self.loss = tf.nn.seq2seq.sequence_loss([logits],
                                                        [self.flatten(self.y)],
                                                        [self.flatten(modified_weights)],
                                                        average_across_timesteps=False, average_across_batch=False)

                params = tf.trainable_variables()

            # Construct training operations.
            with tf.name_scope("training"):
                self.global_step = tf.get_variable("global_step", [], trainable=False, initializer=tf.constant_initializer(0))
                optimizer = tf.train.MomentumOptimizer(0.01, 0.7)
                grads = tf.gradients(self.loss, params)
                grads, _ = tf.clip_by_global_norm(grads, config.max_grad_norm)
                self.optimize = optimizer.apply_gradients(zip(grads, params), global_step=self.global_step)

    # Commonly used reshaping operations.
    def flatten(self, x):
        if len(x.get_shape()) == 2:
            return tf.reshape(x, [-1])
        elif len(x.get_shape()) == 3:
            return tf.reshape(x, [-1, x.get_shape()[2].value])
        else:
            raise ValueError("Unsupported shape: {}".format(x.get_shape()))

    def unflatten(self, flattened, name=None):
        return tf.reshape(flattened, [-1, self.max_tokens, flattened.get_shape()[1].value], name=name)
