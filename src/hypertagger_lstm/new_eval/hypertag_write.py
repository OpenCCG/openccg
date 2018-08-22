import numpy as np

class HypertagWriter(object):
    def __init__(self, hypertag_space, model_type, counted, num_tags, filepath):
        '''
        hypertag_space: List of possible hypertags
        num_tags: Maximum number of tags to store. If 0, store all tags.
        filepath: Filepath to write to
        '''
        self.hypertag_space = hypertag_space
        self.model_type = model_type
        self.counted = counted
        self.num_tags = num_tags
        self.filepath = filepath
    
    def writeTags(self, scores, sent_lens):
        num_sents = scores.shape[0]
        if self.num_tags == 0:
            sort_indices = np.argsort(-scores, 2)
        else:
            sort_indices = np.argsort(-scores, 2)[:,:,:self.num_tags]
        
        with open(self.filepath, 'w') as f:
            for i in range(num_sents):
                for j in range(sent_lens[i]):
                    counted_in_acc = int(self.counted[i,j])
                    if counted_in_acc == 1:
                        token_scores = [scores[i,j,k] for k in sort_indices[i,j,:]]
                        tags = [self.hypertag_space[k] for k in sort_indices[i,j,:]]
                        f.write(str(token_scores) + '\n')                  
                        f.write(str(tags) + '\n')
                f.write('\n')
