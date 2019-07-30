import tensorflow as tf
import numpy as np

class TagProbDist(object):
    def __init__(self, tagprobdist_file, exptag_file):
        self.tags, self.scores = self.loadTagProbDist(tagprobdist_file)
        self.prob_dists = tf.Session().run(tf.nn.softmax(self.scores))
        self.exptags = self.loadExpTags(exptag_file)
    
    def loadTagProbDist(self, tagprobdist_file):
        prob_dists = []
        tags = []
        with open(tagprobdist_file) as f:
            line_num = 0
            for line in f:
                if line == '\n':
                    line_num -= 1
                elif line_num % 2 == 0:
                    prob_dist = line.strip()[1:-1].split(', ')
                    prob_dists.append(np.asarray(prob_dist).astype(np.float))
                else:
                    token_tags = line.strip()[1:-1].split(', ')
                    tags.append([tag[1:-1] for tag in token_tags])
                line_num += 1
        return tags, np.asarray(prob_dists)
    
    def loadExpTags(self, exptag_file):
        tags = []
        with open(exptag_file, 'r') as f:
            for line in f:
                if line == '\n':
                    continue
                tags.append(line.strip())
        return tags
    
    def calcTagsPerWordBetas(self, betas):
        return [self.calcTagsPerWord(beta) for beta in betas]
    
    def calcTagsPerWord(self, beta):
        tag_maps = self.getTagMaps(beta)
        num_tags = sum([len(tag_map) for tag_map in tag_maps])
        num_correct = sum([1 if self.exptags[i] != '*UNKNOWN*' and self.exptags[i] in tag_maps[i] else 0 for i,_ in enumerate(tag_maps)])
        return float(num_tags) / len(self.tags), num_correct*100.0 / len(self.tags)

    def getTagMaps(self, beta):
        ''' sent_prob_dists: tag probability distributions for tokens in this sentence '''
        tag_maps = []
        for i in range(self.prob_dists.shape[0]):
            token_tags = self.tags[i]
            token_prob_dist = self.prob_dists[i,:]
            token_tag_map = self.getTokenTagMap(token_tags, token_prob_dist, beta)
            tag_maps.append(token_tag_map)
        return tag_maps
    
    def getTokenTagMap(self, token_tags, token_prob_dist, beta):
        token_tag_map = {}
        thresh_prob = beta*token_prob_dist[0]        
        i = 0
        while i < token_prob_dist.shape[0] and token_prob_dist[i] >= thresh_prob:
            tag = token_tags[i]
            prob = token_prob_dist[i]
            tag = tag.replace('\\\\','\\')
            token_tag_map[tag] = prob
            i += 1
        return token_tag_map
    
    def findBetaForMultiTag(self, tags_per_word, low, high):
        lowTagsPerWord = self.calcTagsPerWord(low)
        print(lowTagsPerWord)
        highTagsPerWord = self.calcTagsPerWord(high)
        print(highTagsPerWord)
        medTagsPerWord = self.calcTagsPerWord(0.5*low + 0.5*high)
        iters = 0
        
        while abs(tags_per_word - medTagsPerWord[0]) > 0.01:
            if iters % 5 == 0:
                print(medTagsPerWord)
            if tags_per_word > medTagsPerWord[0]:
                low = 0.5*low + 0.5*high
            else:
                high = 0.5*low + 0.5*high
            medTagsPerWord = self.calcTagsPerWord(0.5*low + 0.5*high)
            iters += 1
        return 0.5*low + 0.5*high

if __name__=='__main__':
    tagprobdist = TagProbDist('../output/dev_predictions_clean.txt', '../output/dev_expected.txt')
#     print(tagprobdist.calcTagsPerWordBetas([0.16, 0.05, 0.0058, 0.00175, 0.000625, 0.000125, 0.000058])) # original betas
    print(tagprobdist.calcTagsPerWordBetas([0.13, 0.04, 0.007, 0.0028, 0.00175, 0.00127, 0.000365, 0.0002])) # match beta levels in publication
#     print(tagprobdist.calcTagsPerWordBetas([0.00175, 0.00055, 0.000135, 2.53e-05, 5.31e-06, 8.63e-07, 1.47e-07, 2.4e-09])) # match beta levels in ht2.config
