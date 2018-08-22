import sys
import rpyc
from rpyc.utils.server import ThreadedServer
import logging

from ccgbank import AnnotSeqReader, WordFeats
from feat_preprocessing.feat_orders import FeatOrders
from feat_preprocessing.pickle_handler import PickleHandler
from features import SupertagSpace
from feat_preprocessing.tensorizer import Tensorizer
from restored_model import RestoredHypertagger
from definitions import PROJ_DIR

class TaggerflowServer(rpyc.Service):      
    def __init__(self, feat_order_name, lin_order_name, max_beta):
        self.feat_order_name = feat_order_name
        self.lin_order_name = lin_order_name
        self.max_beta = max_beta
        
        logfile = PROJ_DIR + '/hypertagger_logs/' + feat_order_name + '_' + lin_order_name + '.log'
        logging.basicConfig(filename=logfile, filemode='w', level=logging.DEBUG)
        self.feat_order = FeatOrders.featOrder(feat_order_name)
        logging.info('Feature order: {}'.format(self.feat_order))
        self.parameters = PickleHandler.loadParameters(feat_order_name, lin_order_name)
        self.temp_hack_set_feat_spaces()
        logging.info('Number of feature spaces: {}'.format(len(self.parameters)))
        for feat, space in self.parameters.items():
            if space.size() < 15:
                logging.info('Space of {}: {}'.format(feat, space.space))
            else:
                logging.info('Size of space of {}: {}'.format(feat, space.size()))
        
        self.supertag_space = SupertagSpace(PROJ_DIR + '/data/categories_hypertagger')
        logging.info('Number of hypertags: {}'.format(len(self.supertag_space.space)))
        self.model = RestoredHypertagger(self.supertag_space)
        logging.info('Model x: {}'.format(self.model.x))
        logging.info('Model num_tokens: {}'.format(self.model.num_tokens))
        logging.info('Model scores: {}'.format(self.model.scores))
        print('Server ready')
        sys.stdout.flush()
    
    def on_connect(self, conn):
        pass
    
    def on_disconnect(self, conn):
        pass
    
    def temp_hack_set_feat_spaces(self):
        self.parameters['XC'].setSpace(['', 'NAME', 'MONEY', 'PERCENT', 'PERSON', 'LOCATION', 'TIME', 'DATE', 'ORGANIZATION', None, '*UNKNOWN*'])
        self.parameters['ZD'].setSpace(['', None, 'nil', '*UNKNOWN*'])
        self.parameters['ZM'].setSpace(['', 'dcl', None, 'int', 'imp', 'excl', '*UNKNOWN*'])
        self.parameters['ZN'].setSpace(['', 'sg', None, 'pl', '*UNKNOWN*'])
        self.parameters['ZP'].setSpace(['', 'past', None, 'pres', 'pass', '*UNKNOWN*'])
        self.parameters['ZT'].setSpace(['', 'past', None, 'pres', '*UNKNOWN*'])
        self.parameters['FO'].setSpace(['', '10', None, '1', '0', '3', '2', '5', '4', '7', '6', '9', '8', '14', '*UNKNOWN*'])
        self.parameters['NA'].setSpace(['', None, '1', '0', '3', '2', '4', '7', '*UNKNOWN*'])
        self.parameters['PR0'].setSpace(['', 'Arg3', None, 'ParenRel', 'Arg4', 'Next', 'InterruptRel', 'ArgM', 'EmphFinal', 'ArgA',
                                          'Arg2b', 'Arg2a', 'EllipsisRel', 'Purpose', 'rel', 'GenRel', 'Mod', 'EmphIntro', 'Of',
                                           'Det', 'GenOwn', 'ElabRel', 'DashInterp', 'First', 'Arg1b', 'Arg1a', 'Arg0', 'Arg1',
                                            'Arg2', 'whApposRel', 'ApposRel', 'Arg5', 'Num', 'colonExp', 'Arg', '*UNKNOWN*'])
        self.parameters['PR1'].setSpace(['', 'ApposRel', 'Arg1a', None, 'Arg0', 'Arg1', 'Arg2', 'Arg3', 'Arg4', 'Next', 'ArgM', 'rel', 'Of', 'ArgA', 'DashInterp', 'Arg1b', 'Arg', 'Mod', '*UNKNOWN*'])
        self.parameters['PR2'].setSpace(['', 'Arg1a', None, 'Arg0', 'Of', 'Arg2', 'Arg3', 'ArgM', 'rel', 'Arg1', 'ArgA', '*UNKNOWN*'])
        self.parameters['PR3'].setSpace(['', 'Arg1a', None, 'Arg0', 'Of', 'Arg2', 'Arg3', 'Arg1', '*UNKNOWN*'])
        self.parameters['PR4'].setSpace(['', None, 'Arg0', 'Arg1', 'Arg2', 'Arg3', 'Of', '*UNKNOWN*'])
        self.parameters['AT0'].setSpace(['', 'Arg1a', None, 'Arg0', 'Arg1', 'Arg2', 'Arg3', 'Arg4', 'Arg2a', '*UNKNOWN*'])
        self.parameters['AT1'].setSpace(['', 'Arg1b', 'Arg1a', None, 'Arg0', 'Arg1', 'Arg2', 'Arg3', 'Arg4', 'Arg2b', 'Arg2a', 'Arg5', '*UNKNOWN*'])
        self.parameters['AT2'].setSpace(['', 'Arg1b', 'Arg1a', None, 'Arg1', 'Arg2', 'Arg3', 'Arg4', 'Arg5', 'Arg2b', '*UNKNOWN*'])
        self.parameters['AT3'].setSpace(['', None, 'Arg2', 'Arg3', 'Arg4', 'Arg5', '*UNKNOWN*'])
        self.parameters['AT4'].setSpace(['', '\n', 'Arg4', None, '*UNKNOWN*'])
    
    def exposed_get_hypertag_results(self, annot_seq):
        word_feats = self.load_word_feats(annot_seq, self.feat_order)
        logging.info('Feature order: {}'.format(word_feats.word_feat_order))
        for feat in word_feats.word_feat_order:
            logging.info(getattr(word_feats, feat)[:25])
        
        tensor_creater = Tensorizer(self.supertag_space, self.parameters, word_feats, parens=self.lin_order_name.endswith('paren'))
        x, _, num_tokens, _, weights = tensor_creater.tensors
        logging.info('x shape: {}'.format(x.shape))
        logging.info('num_tokens: {}'.format(num_tokens))
        logging.info('weights: {}'.format(weights[0,:25]))
        results = self.model.getTagMaps(x, num_tokens, weights, self.max_beta)
        return results
    
    def load_word_feats(self, annot_seq, feat_order):
        str_feats = FeatOrders.strFeats()
        annot_seq_reader = AnnotSeqReader()
        feat_order = annot_seq_reader.expand(feat_order)
        word_feats = WordFeats(feat_order, str_feats)
        annot_seq_reader.load_sentence(annot_seq, word_feats)
        return word_feats
    
if __name__=='__main__':
    port_num = int(sys.argv[1])
    feat_order_name = sys.argv[2]
    lin_order_name = sys.argv[3]
    max_beta = float(sys.argv[4])
    
    server = ThreadedServer(TaggerflowServer(feat_order_name, lin_order_name, max_beta), port=port_num)
    server.start()
