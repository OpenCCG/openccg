from collections import OrderedDict
from definitions import PROJ_DIR
import logging

class DbIdGetter(object):
    '''
    Gets ID in DB for tags, words, and sentences
    '''
    def __init__(self, remove_duplicates=False):
        self.remove_duplicates = remove_duplicates
        self.separator = '|'
        self.dev_sents = self.loadCSV(PROJ_DIR + '/db_csv/dev_sents.csv')
        self.test_sents = self.loadCSV(PROJ_DIR + '/db_csv/test_sents.csv')
        self.train_sents = self.loadCSV(PROJ_DIR + '/db_csv/train_sents.csv')
        self.tags = self.loadCSV(PROJ_DIR + '/db_csv/tags.csv')
        self.words = self.loadCSV(PROJ_DIR + '/db_csv/words.csv')
        
        self.dev_sent_id_list = []
        self.train_sent_id_list = []
        self.test_sent_id_list = []
        self.checkLens()
    
    def checkLens(self):
        if len(self.dev_sents) != 1883:
            print('Expected 1883 dev sents but got {}'.format(len(self.dev_sents)))
        if len(self.test_sents) != 2310:
            print('Expected 2310 test sents but got {}'.format(len(self.test_sents)))
        if len(self.train_sents) != 35765:
            print('Expected 35765 train sents but got {}'.format(len(self.train_sents)))
        if len(self.tags) != 528:
            print('Expected 528 tags but got {}'.format(len(self.tags)))
    
    def loadCSV(self, filepath):
        dbid_map = OrderedDict()
        line_num = 0
        num_duplicates = 0
        
        with open(filepath, 'r') as f:
            for line in f:
                dbid, item = line.strip().split(self.separator)
                if item == 'None':
                    item = None
                dbid = int(dbid)
                if item in dbid_map:
                    num_duplicates += 1
                    if not self.remove_duplicates:
                        dbid_map[line_num] = dbid
                else:
                    dbid_map[item] = dbid
                line_num += 1
        
        logging.info('Read {} lines from {}'.format(line_num, filepath))
        logging.info('{} duplicates in {}'.format(num_duplicates, filepath))
        return dbid_map
    
    def addDevSentID(self, i):
        self.dev_sent_id_list.append(self.dev_sents.values()[i])
    
    def addTrainSentID(self, i):
        self.train_sent_id_list.append(self.train_sents.values()[i])
    
    def sentenceID(self, model_type, index):
        if model_type.lower() == 'dev':
            return self.dev_sent_id_list[index]
        elif model_type.lower() == 'test':
            return self.test_sent_id_list[index]
        else:
            return self.train_sents.values()[index]
    
    def wordID(self, word):
        if word not in self.words:
            return self.words['*UNKNOWN*']
        return self.words[word]
    
    def tagID(self, tag):
        try:
            return self.tags[tag]
        except KeyError:
            return self.tags['*UNKNOWN*']

if __name__=='__main__':
    dbid_getter = DbIdGetter()
    print(dbid_getter)
