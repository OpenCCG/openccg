import math

from tensorflow.python.framework import dtypes
from tensorflow.python.ops import random_ops

def dyer_initializer(factor=1.0, seed=None):
  def _initializer(shape, dtype=dtypes.float32, partition_info=None):
    max_val = math.sqrt(6)/math.sqrt(sum(float(dim) for dim in shape))
    return random_ops.random_uniform(shape, -max_val, max_val, dtype, seed=seed)
  return _initializer
