import util
import itertools
import logging

START_MARKER = "<s>"
END_MARKER = "</s>"

class SupertagReader(object):
    def get_word_and_supertag(self, split):
        if len(split) == 3:
            return (split[0].strip(), split[2].strip())
        elif len(split) == 2:
            return (split[0].strip(), split[1].strip())
        else:
            raise ValueError("Unknown split length: {}".format(split))

    def get_sentences(self, filepath, is_tritrain):
        with open(filepath) as f:
            lines = f.readlines()
            sentences = (itertools.izip(*[self.get_word_and_supertag(split.split("|")) for split in line.split(" ")]) for line in lines)
            return [([START_MARKER] + list(words) + [END_MARKER],
                     [None] + list(supertags) + [None],
                     is_tritrain) for words,supertags in sentences]

    def get_split(self, split_name, is_tritrain):
        return self.get_sentences(util.maybe_download("data",
                                                       "http://appositive.cs.washington.edu/resources/",
                                                       split_name + ".stagged"), is_tritrain)

    def get_splits(self, read_tritrain=True):
        train = self.get_split("train", is_tritrain=False)
        tritrain = self.get_split("tritrain", is_tritrain=True) if read_tritrain else []
        dev = self.get_split("dev", is_tritrain=False)
        return train, tritrain, dev

### ADDED FOR HYPERTAGGER ###
class AnnotSeqReader(object):
    def load_data(self, train_file, dev_file, feat_order, str_feats, from_str=False):
        feat_order = self.expand(feat_order)
        train_word_feats = WordFeats(feat_order, str_feats)
        dev_word_feats = WordFeats(feat_order, str_feats)
        logging.info('Loading train sentences')
        self.load_sentences(train_file, train_word_feats, from_str);
        logging.info('Loading dev sentences')
        self.load_sentences(dev_file, dev_word_feats, from_str)
        return train_word_feats, dev_word_feats
    
    def expand(self, feat_order):
        new_order = []
        for feat in feat_order:
            if '#' in feat:
                num = int(feat[feat.index('#') + 1:])
                feat = feat[:feat.index('#')]
                for i in range(num):
                    new_order.append(feat + str(i))
            else:
                new_order.append(feat)
        return new_order
    
    def load_sentences(self, data_str, word_feats_obj, from_str):
        num_sents = num_tokens = num_nonwords = 0
        if from_str:
            sentences = data_str.splitlines()
            for sentence in sentences:
                self.load_sentence(sentence, word_feats_obj)
                num_sents += 1
                word_feat_strs = sentence.split(" ")
                num_tokens += len([word for word in word_feat_strs if word[0] != '(' and word[0] != ')'])
                num_nonwords += len([word for word in word_feat_strs if word[0] == '(' or word[0] == ')'])
        else:
            with open(data_str) as sentences:
                for sentence in sentences:
                    self.load_sentence(sentence, word_feats_obj)
                    num_sents += 1
                    word_feat_strs = sentence.split(" ")
                    num_tokens += len([word for word in word_feat_strs if word[0] != '(' and word[0] != ')'])
                    num_nonwords += len([word for word in word_feat_strs if word[0] == '(' or word[0] == ')'])
        logging.info('Read {} sentences, {} tokens, and {} parentheses'.format(num_sents, num_tokens, num_nonwords))
    
    def load_sentence(self, sentence, word_feats_obj):
        feat_lists = self.get_lists(word_feats_obj.word_feat_order, word_feats_obj.str_feats)
        word_feat_strs = sentence.split(" ")
        for word_feat_str in word_feat_strs:
            self.load_word_features(word_feat_str, feat_lists)
        self.end_lists(word_feats_obj.word_feat_order, word_feats_obj.str_feats, feat_lists)
        word_feats_obj.addFeats(feat_lists)
    
    def get_lists(self, feat_order, str_feats):
        feat_lists = []
        for feat in feat_order:
            if feat in str_feats:
                feat_lists.append([START_MARKER])
            else:
                feat_lists.append([None])
        return feat_lists
    
    def load_word_features(self, word_feat_str, feat_lists):
        word_feats = word_feat_str.split("|")
        if len(word_feats) != len(feat_lists):
            print("ERROR: Expected " + str(len(feat_lists)) + " features but got " + \
                  str(len(word_feats)) + " from " + word_feat_str)
        for i in range(len(word_feats)):
            feat_lists[i].append(word_feats[i])
    
    def end_lists(self, feat_order, str_feats, feat_lists):
        for i in range(len(feat_order)):
            if feat_order[i] in str_feats:
                feat_lists[i].append(END_MARKER)
            else:
                feat_lists[i].append(None)

class WordFeats(object):
    def __init__(self, feat_order, str_feats):
        self.word_feat_order = feat_order
        self.str_feats = str_feats
        for word_feat in feat_order:
            setattr(self, word_feat, [])
    
    def addFeats(self, feat_lists):
        for i in range(len(feat_lists)):
            feat_list = getattr(self, self.word_feat_order[i])
            feat_list.append(feat_lists[i])
### ADDED FOR HYPERTAGGER ###
