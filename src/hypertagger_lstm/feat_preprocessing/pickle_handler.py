import dill as pkl
from definitions import PROJ_DIR

class PickleHandler(object):
    @classmethod
    def saveParameters(cls, parameters, feat_order_name, lin_order_name):
        filepath = PROJ_DIR + '/pickle/parameters' + '_' + feat_order_name + '_' + lin_order_name + '.pkl'
        with open(filepath, 'wb') as f:
            pkl.dump(parameters, f)
    
    @classmethod
    def loadParameters(cls, feat_order_name, lin_order_name):
        filepath = PROJ_DIR + '/pickle/parameters' + '_' + feat_order_name + '_' + lin_order_name + '.pkl'
        with open(filepath, 'rb') as f:
            parameters = pkl.load(f)
        return parameters
    
    @classmethod
    def saveWeights(cls, weights, feat_order_name, lin_order_name):
        filepath = PROJ_DIR + '/pickle/weights' + '_' + feat_order_name + '_' + lin_order_name + '.pkl'
        with open(filepath, 'wb') as f:
            pkl.dump(weights, f)
    
    @classmethod
    def loadWeights(cls, feat_order_name, lin_order_name):
        filepath = PROJ_DIR + '/pickle/weights' + '_' + feat_order_name + '_' + lin_order_name + '.pkl'
        with open(filepath, 'rb') as f:
            weights = pkl.load(f)
        return weights