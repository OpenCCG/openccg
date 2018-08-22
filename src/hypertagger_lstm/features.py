import collections
import logging
import cPickle as pickle
import numpy as np
import ccgbank

UNKNOWN_MARKER = "*UNKNOWN*"
OUT_OF_RANGE_MARKER = "*OOR*"

# Should define space and ispace.
class FeatureSpace(object):
    def index(self, f):
        return self.ispace[f]

    def feature(self, i):
        return self.space[i]

    def size(self):
        return len(self.space)
    
    def setSpace(self, space):
        self.space = space
        self.unk_index = len(self.space)-1
        self.ispace = collections.defaultdict(lambda:self.unk_index, {f:i for i,f in enumerate(self.space)})

class SupertagSpace(FeatureSpace):
    def __init__(self, supertags_obj):
        if not isinstance(supertags_obj, list):
            with open(supertags_obj) as f:
                self.space = [line.strip() for line in f.readlines()]
                self.ispace = collections.defaultdict(lambda: -1, {f:i for i,f in enumerate(self.space)})
                none_ind = self.ispace['None']
                if none_ind != -1:
                    self.space[none_ind] = None
        else:
            self.space = supertags_obj
            self.ispace = collections.defaultdict(lambda: -1, {f:i for i,f in enumerate(self.space)})

# Should define embedding_size.
class EmbeddingSpace(FeatureSpace):
    def extract(self, token):
        raise NotImplementedError("Subclasses must implement this!")

class TurianEmbeddingSpace(EmbeddingSpace):
    def __init__(self, embeddings_file, spec_preds=None):
        already_added = set()
        self.embedding_size = None
        self.space = []
        self.embeddings = []
        with open(embeddings_file) as f:
            for i,line in enumerate(f.readlines()):
                splits = line.split()
                word = splits[0]

                if i == 0 and word != UNKNOWN_MARKER:
                    raise ValueError("First embedding in the file should represent the unknown word.")

                word = word.lower()
                if word not in already_added:
                    embedding = [float(s) for s in splits[1:]]
                    if self.embedding_size is None:
                        self.embedding_size = len(embedding)
                    elif self.embedding_size != len(embedding):
                        raise ValueError("Dimensions mismatch. Expected {} but was {}.".format(self.embedding_size, len(embedding)))

                    already_added.add(word)
                    self.space.append(word)
                    self.embeddings.append(embedding)

        # Extra markers and tokens not found in the the embeddings file.
        if spec_preds is not None:
            for pred in spec_preds:
                self.space.insert(1, pred)
                self.embeddings.insert(1, np.random.randn(self.embedding_size).tolist())
        
        self.space.append(ccgbank.START_MARKER)
        self.embeddings.append([0.0] * self.embedding_size)
        self.space.append(ccgbank.END_MARKER)
        self.embeddings.append([0.0] * self.embedding_size)
        self.space.append("")
        self.embeddings.append([0.0] * self.embedding_size)

        self.space = list(self.space)
        self.ispace = collections.defaultdict(lambda:0, {f:i for i,f in enumerate(self.space)})

    def extract(self, token):
        return token.lower()

### ADDED FOR HYPERTAGGER ###
class PickledEmbeddingSpace(EmbeddingSpace):
    def __init__(self, embeddings_file, spec_preds):
        embeddings_map = pickle.load(open(embeddings_file, 'rb'))
        self.space = list(embeddings_map.keys())
        self.embeddings = [embeddings_map[key].tolist() for key in self.space]
        self.embedding_size = len(self.embeddings[0])
        
        self.space.insert(0, UNKNOWN_MARKER)
        self.embeddings.insert(0, np.random.randn(self.embedding_size).tolist())
        for pred in spec_preds:
            self.space.insert(1, pred)
            self.embeddings.insert(1, np.random.randn(self.embedding_size).tolist())
        
        self.space.append(ccgbank.START_MARKER)
        self.embeddings.append([0.0] * self.embedding_size)
        self.space.append(ccgbank.END_MARKER)
        self.embeddings.append([0.0] * self.embedding_size)
        self.space.append("")
        self.embeddings.append([0.0] * self.embedding_size)
        
        self.ispace = collections.defaultdict(lambda:0, {f:i for i,f in enumerate(self.space)})
    
    def extract(self, token):
        return token
### ADDED FOR HYPERTAGGER ###

class WordSpace(EmbeddingSpace):
    def extract(self, token):
        return token.lower()

class PrefixSpace(EmbeddingSpace):
    def __init__(self, n):
        self.n = n
        self.embedding_size = 32

    def extract(self, token):
        if token == ccgbank.START_MARKER or token == ccgbank.END_MARKER:
            return token
        else:
            return token[:self.n] if len(token) >= self.n else OUT_OF_RANGE_MARKER

class SuffixSpace(EmbeddingSpace):
    def __init__(self, n):
        self.n = n
        self.embedding_size = 32

    def extract(self, token):
        if token == ccgbank.START_MARKER or token == ccgbank.END_MARKER:
            return token
        else:
            return token[-self.n:] if len(token) >= self.n else OUT_OF_RANGE_MARKER

class EmpiricalEmbeddingSpace(EmbeddingSpace):
    def __init__(self, sentences, min_count, just_tokens=False):
        counts = collections.Counter()
        if just_tokens:
            for tokens in sentences:
                counts.update((self.extract(t) for t in tokens))
        else:
            for tokens,supertags,weights in sentences():
                counts.update((self.extract(t) for t in tokens))

        self.space = [f for f in counts if counts[f] >= min_count]
        self.default_index = len(self.space)
        self.ispace = collections.defaultdict(lambda:self.default_index, {f:i for i,f in enumerate(self.space)})
        self.space.append(UNKNOWN_MARKER)

class EmpiricalPrefixSpace(EmpiricalEmbeddingSpace, PrefixSpace):
    def __init__(self, n, sentences, min_count=3, just_tokens=False):
        PrefixSpace.__init__(self, n)
        EmpiricalEmbeddingSpace.__init__(self, sentences, min_count, just_tokens)

class EmpiricalSuffixSpace(EmpiricalEmbeddingSpace, SuffixSpace):
    def __init__(self, n, sentences, min_count=3, just_tokens=False):
        SuffixSpace.__init__(self, n)
        EmpiricalEmbeddingSpace.__init__(self, sentences, min_count, just_tokens)

### ADDED FOR HYPERTAGGER ###
class CategoricalFeatureSpace(FeatureSpace):
    def __init__(self, poss_value_set):
        self.embedding_size = 8
        self.space = sorted(poss_value_set)
        self.unk_index = len(self.space)
        self.space.append(UNKNOWN_MARKER)
        self.ispace = collections.defaultdict(lambda:self.unk_index, {f:i for i,f in enumerate(self.space)})

class HypertagFeatureSpace(FeatureSpace):
    def __init__(self, poss_value_set):
        self.embedding_size = 10
        self.space = sorted(poss_value_set)
        self.space.remove(None)
        self.space.insert(0, None)
        self.unk_index = len(self.space)
        self.space.append(UNKNOWN_MARKER)
        self.ispace = collections.defaultdict(lambda:self.unk_index, {f:i for i,f in enumerate(self.space)})
### ADDED FOR HYPERTAGGER ###
