import sys
import xml.etree.ElementTree as et
from nltk.translate.bleu_score import corpus_bleu
from nltk.tokenize.treebank import TreebankWordDetokenizer

if __name__ == '__main__':
    ref_file = sys.argv[1]
    test_file = sys.argv[2]    
    ref_tree = et.parse(ref_file)
    test_tree = et.parse(test_file)    
    refs = []
    tests = []
    
    # Load ref docs
    for doc in ref_tree.getroot():
        for seg in doc:
            sent = seg.text.split()
            refs.append([sent])
    print(len(refs))
    
    # Load test docs
    for doc in test_tree.getroot():
        for seg in doc:
            sent = seg.text.split()
            tests.append(sent)
    print(len(tests))
    
    # Calculate BLEU
    score = corpus_bleu(refs, tests)
    print(score)
    
    # Print files
    detok = TreebankWordDetokenizer()
    with open('/home/reid/projects/research/ccg/openccg/ccgbank/logs/detok_ref.txt', 'w') as f:
        for ref in refs:
            ref_txt = ' '.join(ref[0]) # detok.detokenize(ref[0])
            f.write(ref_txt + '\n')
    
    with open('/home/reid/projects/research/ccg/openccg/ccgbank/logs/detok_test.txt', 'w') as f:
        for test in tests:
            test_txt = ' '.join(test) # detok.detokenize(test)
            f.write(test_txt + '\n')
