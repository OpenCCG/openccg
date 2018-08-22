from new_eval.db_tag_write import DbTagWriter
from eval.old_stats_logging import OldStatsLogger
from eval.stats_logging import StatsLogger
from eval.index_translate import IndexTranslater
from new_eval.hypertag_write import HypertagWriter

class StatsComponentCreator(object):
    '''
    Creates components used to calculate and record statistics
    '''
    def getIndexTranslater(self, data, params):
        '''
        data: HypertaggerData object
        params: Parameters object
        '''
        tag_space = data.supertag_space.space
        feat_order = data.train_word_feats.word_feat_order
        feat_spaces = {feat: params.embedding_spaces[feat].space for feat in params.embedding_spaces.keys()}
        return IndexTranslater(tag_space, feat_spaces, feat_order)
    
    def getHypertagWriter(self, tag_space, model_type, counted, num_tags, output_file):
        return HypertagWriter(tag_space, model_type, counted, num_tags, output_file)
    
    def getDbTagWriter(self, strategy_id, is_training, data, params):
        '''
        is_training: If true, DbTagWriter writes tags for training sentences. Otherwise it writes tags for dev sentences.
        data: HypertaggerData object
        translater: IndexTranslater object
        '''
        if is_training:
            model_type = 'train'
            X, Y, num_tokens, _, weights = data.train_data
        else:
            model_type = 'dev'
            X, Y, num_tokens, _, weights = data.dev_data
        
        translater = self.getIndexTranslater(data, params)
        dbTagWriter = DbTagWriter(strategy_id, model_type, X, num_tokens, Y, weights,
                                  data.unk_preds, data.unk_predtags, translater, db_id_getter=data.db_id_getter)
        return dbTagWriter
    
    def getOldStatsLogger(self, session, data, params, model, is_training=False):
        '''
        session: Tensorflow Session object
        data: HypertaggerData object
        params: Parameters object
        model: SupertaggerModel object
        is_training: If true, returned OldStatsLogger logs stats for training. Otherwise it logs stats for dev.
        '''
        if is_training:
            feat_order = data.train_word_feats.word_feat_order
            feat_values = {feat: getattr(data.train_word_feats, feat) for feat in feat_order}
            X, Y, num_tokens, _, weights = data.train_data
            model_type = 'Train'
        else:
            feat_order = data.dev_word_feats.word_feat_order
            feat_values = {feat: getattr(data.dev_word_feats, feat) for feat in feat_order}
            X, Y, num_tokens, _, weights = data.dev_data
            model_type = 'Dev'
        
        index_translater = self.getIndexTranslater(data, params)
        feat_spaces = {feat: params.embedding_spaces[feat].space for feat in params.embedding_spaces.keys()}
        stats_logger = OldStatsLogger(session, data.supertag_space.space, feat_spaces, index_translater, X, Y,
                                       num_tokens, weights, data.unk_preds, data.unk_predtags, model, model_type)
        return stats_logger
    
    def getStatsLogger(self, session, data, params, model, is_training=False):
        '''
        session: Tensorflow Session object
        data: HypertaggerData object
        params: Parameters object
        model: SupertaggerModel object
        is_training: If true, returned OldStatsLogger logs stats for training. Otherwise it logs stats for dev.
        '''
        if is_training:
            X, Y, num_tokens, _, weights = data.train_data
            model_type = 'Train'
        else:
            X, Y, num_tokens, _, weights = data.dev_data
            model_type = 'Dev'
        
        translater = self.getIndexTranslater(data, params)
        stats_logger = StatsLogger(session, translater, X, Y, num_tokens, weights, model, model_type)
        return stats_logger
