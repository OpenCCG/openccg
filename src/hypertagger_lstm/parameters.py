import logging
import collections
import re

import numpy as np
import tensorflow as tf

from features import *
from util import *

class ParameterReader(object):
    def readline(self, line):
        raise NotImplementedError("Subclasses must implement this!")

    def get_result(self):
        raise NotImplementedError("Subclasses must implement this!")

class EmbeddingsReader(ParameterReader):
    embedding_regexes = {
        "prefix_(\d)" : lambda g: PrefixSpace(int(g[0])),
        "suffix_(\d)" : lambda g: SuffixSpace(int(g[0])),
        "words" : lambda g: WordSpace()
    }

    unknown_marker = "*UNKNOWN*"

    def __init__(self, name):
        self.name = name
        self.words = []
        self.embeddings = []
        self.default_index = None
        self.embedding_size = None

    def readline(self, i, line):
        splits = line.split()
        word = splits[0]

        if word == self.unknown_marker or word == self.unknown_marker.lower():
            if self.default_index is None:
                self.default_index = i
            else:
                raise ValueError("Unknown word repeated.")

        embedding = [float(s) for s in splits[1:]]
        if self.embedding_size is None:
            self.embedding_size = len(embedding)
        elif self.embedding_size != len(embedding):
            if self.embedding_size == len(embedding) + 1:
                # Assume this corresponds to the empty string.
                word = ""
                embedding = [float(s) for s in splits]
            else:
                raise ValueError("Dimensions mismatch. Expected {} but was {}.".format(self.embedding_size, len(embedding)))

        self.words.append(word)
        self.embeddings.append(embedding)

    def get_result(self):
        if self.default_index is None:
            return ValueError("Unknown word not found.")
        embedding_space = None
        for regex, space_function in self.embedding_regexes.items():
            match = re.match(regex, self.name)
            if match:
                embedding_space = space_function(match.groups())
                break
        if embedding_space is None:
            raise ValueError("Unknown embedding space: {}".format(self.name))
        embedding_space.embedding_size = self.embedding_size
        embedding_space.space = self.words
        embedding_space.ispace = collections.defaultdict(lambda:self.default_index, {f:i for i,f in enumerate(self.words)})
        embedding_space.embeddings = self.embeddings
        return embedding_space

class MatrixReader(ParameterReader):
    def __init__(self, name):
        self.name = name
        self.matrix = []
        self.dimensions = None

    def readline(self, i, line):
        if self.dimensions is None:
            # {columns,rows} or {rows}
            line = line[line.index("{") + 1:line.index("}")]
            self.dimensions = [int(s) for s in line.split(",")]
            if len(self.dimensions) != 1 and len(self.dimensions) != 2:
                raise ValueError("Unsupported shape: {}".format(self.dimensions))
        else:
            splits = line.split()
            expected_column_size = 1 if len(self.dimensions) == 1 else self.dimensions[1]
            if len(splits) != expected_column_size:
                raise ValueError("Expected column size {} but was {}".format(expected_column_size, len(splits)))

            if len(splits) == 1:
                self.matrix.append(float(splits[0]))
            else:
                self.matrix.append([float(s) for s in splits])

    def get_result(self):
        if len(self.matrix) != self.dimensions[0]:
            raise ValueError("Expected row size {} but was {}.".format(self.dimensions[0], len(self.matrix)))
        return np.array(self.matrix).T

class Parameters:
    readers = {
        "EMBEDDINGS" : EmbeddingsReader,
        "PARAMETERS" : MatrixReader
    }

    one_layer_variable_mapping = {
        # Forward LSTM.
        "BiRNN_FW/RNN/DyerLSTMCell/input_gate/Matrix" : ["forward_lstm_layer_1_parameters_0", "forward_lstm_layer_1_parameters_1", "forward_lstm_layer_1_parameters_2"],
        "BiRNN_FW/RNN/DyerLSTMCell/input_gate/Bias" : ["forward_lstm_layer_1_parameters_3"],
        "BiRNN_FW/RNN/DyerLSTMCell/new_input/Matrix" : ["forward_lstm_layer_1_parameters_8", "forward_lstm_layer_1_parameters_9"],
        "BiRNN_FW/RNN/DyerLSTMCell/new_input/Bias" : ["forward_lstm_layer_1_parameters_10"],
        "BiRNN_FW/RNN/DyerLSTMCell/output_gate/Matrix" : ["forward_lstm_layer_1_parameters_4", "forward_lstm_layer_1_parameters_5", "forward_lstm_layer_1_parameters_6"],
        "BiRNN_FW/RNN/DyerLSTMCell/output_gate/Bias" : ["forward_lstm_layer_1_parameters_7"],

        # Backward LSTM.
        "BiRNN_BW/RNN/DyerLSTMCell/input_gate/Matrix" : ["backward_lstm_layer_1_parameters_0", "backward_lstm_layer_1_parameters_1", "backward_lstm_layer_1_parameters_2"],
        "BiRNN_BW/RNN/DyerLSTMCell/input_gate/Bias" : ["backward_lstm_layer_1_parameters_3"],
        "BiRNN_BW/RNN/DyerLSTMCell/new_input/Matrix" : ["backward_lstm_layer_1_parameters_8", "backward_lstm_layer_1_parameters_9"],
        "BiRNN_BW/RNN/DyerLSTMCell/new_input/Bias" : ["backward_lstm_layer_1_parameters_10"],
        "BiRNN_BW/RNN/DyerLSTMCell/output_gate/Matrix" : ["backward_lstm_layer_1_parameters_4", "backward_lstm_layer_1_parameters_5", "backward_lstm_layer_1_parameters_6"],
        "BiRNN_BW/RNN/DyerLSTMCell/output_gate/Bias" : ["backward_lstm_layer_1_parameters_7"],

        # Penultimate layer.
        "penultimate/Matrix" : ["forward_lstm_to_penultimate", "backward_lstm_to_penultimate"],
        "penultimate/Bias" : ["penultimate_bias"],

        # Softmax layer.
        "softmax/Matrix" : ["penultimate_to_softmax"],
        "softmax/Bias" : ["softmax_bias"]
    }

    two_layer_variable_mapping = {
        # First layer of the forward LSTM.
        "BiRNN_FW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/input_gate/Matrix" : ["forward_lstm_layer_1_parameters_0", "forward_lstm_layer_1_parameters_1", "forward_lstm_layer_1_parameters_2"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/input_gate/Bias" : ["forward_lstm_layer_1_parameters_3"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/new_input/Matrix" : ["forward_lstm_layer_1_parameters_8", "forward_lstm_layer_1_parameters_9"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/new_input/Bias" : ["forward_lstm_layer_1_parameters_10"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/output_gate/Matrix" : ["forward_lstm_layer_1_parameters_4", "forward_lstm_layer_1_parameters_5", "forward_lstm_layer_1_parameters_6"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/output_gate/Bias" : ["forward_lstm_layer_1_parameters_7"],

        # Second layer of the forward LSTM.
        "BiRNN_FW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/input_gate/Matrix" : ["forward_lstm_layer_2_parameters_0", "forward_lstm_layer_2_parameters_1", "forward_lstm_layer_2_parameters_2"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/input_gate/Bias" : ["forward_lstm_layer_2_parameters_3"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/new_input/Matrix" : ["forward_lstm_layer_2_parameters_8", "forward_lstm_layer_2_parameters_9"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/new_input/Bias" : ["forward_lstm_layer_2_parameters_10"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/output_gate/Matrix" : ["forward_lstm_layer_2_parameters_4", "forward_lstm_layer_2_parameters_5", "forward_lstm_layer_2_parameters_6"],
        "BiRNN_FW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/output_gate/Bias" : ["forward_lstm_layer_2_parameters_7"],

        # First layer of the backward LSTM.
        "BiRNN_BW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/input_gate/Matrix" : ["backward_lstm_layer_1_parameters_0", "backward_lstm_layer_1_parameters_1", "backward_lstm_layer_1_parameters_2"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/input_gate/Bias" : ["backward_lstm_layer_1_parameters_3"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/new_input/Matrix" : ["backward_lstm_layer_1_parameters_8", "backward_lstm_layer_1_parameters_9"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/new_input/Bias" : ["backward_lstm_layer_1_parameters_10"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/output_gate/Matrix" : ["backward_lstm_layer_1_parameters_4", "backward_lstm_layer_1_parameters_5", "backward_lstm_layer_1_parameters_6"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell0/DyerLSTMCell/output_gate/Bias" : ["backward_lstm_layer_1_parameters_7"],

        # Second layer of the backward LSTM.
        "BiRNN_BW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/input_gate/Matrix" : ["backward_lstm_layer_2_parameters_0", "backward_lstm_layer_2_parameters_1", "backward_lstm_layer_2_parameters_2"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/input_gate/Bias" : ["backward_lstm_layer_2_parameters_3"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/new_input/Matrix" : ["backward_lstm_layer_2_parameters_8", "backward_lstm_layer_2_parameters_9"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/new_input/Bias" : ["backward_lstm_layer_2_parameters_10"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/output_gate/Matrix" : ["backward_lstm_layer_2_parameters_4", "backward_lstm_layer_2_parameters_5", "backward_lstm_layer_2_parameters_6"],
        "BiRNN_BW/RNN/MultiRNNCell/Cell1/DyerLSTMCell/output_gate/Bias" : ["backward_lstm_layer_2_parameters_7"],

        # Penultimate layer.
        "penultimate/Matrix" : ["forward_lstm_to_penultimate", "backward_lstm_to_penultimate"],
        "penultimate/Bias" : ["penultimate_bias"],

        # Softmax layer.
        "softmax/Matrix" : ["penultimate_to_softmax"],
        "softmax/Bias" : ["softmax_bias"]
    }

    param_header_regex = "\*(.*)\*(.*)"

    def __init__(self, embedding_spaces=[]):
        self.matrices = {}
        self.embedding_spaces = collections.OrderedDict(embedding_spaces)

    def write(self, spaces_dir):
        maybe_mkdirs(spaces_dir)
        for name, space in self.embedding_spaces.items():
            with open(os.path.join(spaces_dir, name + ".txt"), "w") as f:
                f.write("\n".join(space.space))

    def read(self, filename):
        current_reader = None
        offset = 0
        with open(filename) as f:
            for i,line in enumerate(f.readlines()):
                line = line.strip()
                if current_reader is None:
                    param_type, name = re.match(self.param_header_regex, line).groups()
                    name = name.strip().replace(" ", "_").lower()
                    current_reader = self.readers[param_type](name)
                    offset = i + 1
                elif len(line) == 0:
                    if isinstance(current_reader, EmbeddingsReader):
                        self.embedding_spaces[current_reader.name] = current_reader.get_result()
                    elif isinstance(current_reader, MatrixReader):
                        self.matrices[current_reader.name] = current_reader.get_result()
                    else:
                        raise ValueError("Unknown reader type: {}".format(type(current_reader)))
                    current_reader = None
                else:
                    current_reader.readline(i - offset, line)

            logging.info("Loaded pretrained embedding spaces: {}".format(self.embedding_spaces.keys()))
            for k,v in self.matrices.items():
                logging.info("Loaded pretrained matrix: {} {}".format(k, v.shape))

    def assign_pretrained(self, session):
        '''
        space.embeddings is list of lists of floats
        '''
        unassigned_variables = set(v.name for v in tf.trainable_variables())
        for name, space in self.embedding_spaces.items():
            if hasattr(space, "embeddings"):
                variable = tf.get_variable(name, [space.size(), space.embedding_size])
                logging.info("Assigning pretrained embeddings for {}.".format(variable.name))
                unassigned_variables.remove(variable.name)
                session.run(tf.assign(variable, space.embeddings))

        # TODO: do this the right way...
        if len(self.matrices) != 0:
            for name, matrix_names in self.two_layer_variable_mapping.items():
            #for name, matrix_names in self.one_layer_variable_mapping.items():
                if not all((n in self.matrices) for n in matrix_names):
                    logging.info("Skipping parameters for {}".format(name))
                    continue
                concat = np.concatenate([self.matrices[n] for n in matrix_names])
                variable = tf.get_variable(name, concat.shape)
                logging.info("Assigning pretrained matrix for {} ({}).".format(variable.name, variable.get_shape()))
                unassigned_variables.remove(variable.name)
                session.run(tf.assign(variable, concat))

        logging.info("Remaining unassigned variables: {}".format(unassigned_variables))
