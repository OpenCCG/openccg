from ccgbank import AnnotSeqReader
from feat_preprocessing.feat_orders import FeatOrders
from new_eval.get_db_id import DbIdGetter

if __name__ == '__main__':
    str_feats = FeatOrders.strFeats()
    feat_order_name = 'allrel'
    feat_order = FeatOrders.featOrder(feat_order_name)
    db_id_getter = DbIdGetter(remove_duplicates=False)
    
    reader = AnnotSeqReader()
    train_word_feats, dev_word_feats = reader.load_data("../data/train_hypertagger", "../data/dev_hypertagger", feat_order, str_feats)
    tagcompare_file = 'train_tagcompare_allrel_engparen.csv'
    nonwords = ['(',')','<s>','</s>']
    
    lines_written = sum_sent_lens = num_nonwords = 0
    with open(tagcompare_file, 'w') as tcf:
        for i in range(len(train_word_feats.PN)):
            sent_id = db_id_getter.sentenceID('train', i)
            sum_sent_lens += len(train_word_feats.PN[i])
            
            for j, token in enumerate(train_word_feats.PN[i]):
                if token not in nonwords:
                    word_id = db_id_getter.wordID(token)
                    exp_tag_id = db_id_getter.tagID(train_word_feats.hypertag[i][j])
                    act_tag_id = db_id_getter.tagID('')
                    line_elements = ['10', str(sent_id), str(word_id), str(j), str(exp_tag_id), str(act_tag_id)]
                    line = '|'.join(line_elements)
                    tcf.write(line + '\n')
                    lines_written += 1
                else:
                    num_nonwords += 1
    print('Wrote {} lines to {}'.format(lines_written, tagcompare_file))
    print('Num nonwords: {}'.format(num_nonwords))
    print('Num tokens: {}'.format(sum_sent_lens))