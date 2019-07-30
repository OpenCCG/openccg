import tensorflow as tf

class DyerLSTMCell(tf.nn.rnn_cell.RNNCell):
  """LSTM recurrent network cell variant from https://github.com/clab/cnn.
  Forgot and input gates are coupled.
  Gates contain peephole connections.
  """

  def __init__(self, num_units, input_size):
    self._num_units = num_units
    self._input_size = input_size

  @property
  def input_size(self):
    return self._input_size

  @property
  def output_size(self):
    return self._num_units

  @property
  def state_size(self):
    return 2 * self._num_units

  def __call__(self, inputs, state, scope=None):
    """Long short-term memory cell (LSTM)."""
    with tf.variable_scope(scope or type(self).__name__):  # "DyerLSTMCell"
      h, c = tf.split(1, 2, state)

      input_gate = tf.sigmoid(tf.nn.rnn_cell._linear([inputs, h, c], self._num_units, bias=True, bias_start=0.25, scope="input_gate"))
      new_input = tf.tanh(tf.nn.rnn_cell._linear([inputs, h], self._num_units, bias=True, scope="new_input"))
      new_c = input_gate * new_input + (1.0 - input_gate) * c
      output_gate = tf.sigmoid(tf.nn.rnn_cell._linear([inputs, h, new_c], self._num_units, bias=True, scope="output_gate"))
      new_h = tf.tanh(new_c) * output_gate
    return new_h, tf.concat(1, [new_h, new_c])
