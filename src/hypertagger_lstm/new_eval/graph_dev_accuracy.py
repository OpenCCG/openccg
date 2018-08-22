import re
import matplotlib.pyplot as plt
import sys
import os.path

# Takes two parameters: log file path and plot file path

if __name__ == '__main__':
    logfilepath = '/home/reid/projects/research/ccg/' + sys.argv[1]
    pltfilepath = '/home/reid/projects/research/ccg/taggerflow_modified/' + sys.argv[2]
    accuracies = []
    prev_line_dev = False
    
    with open(logfilepath, 'r') as logfile:
        for line in logfile:
            match_dev = re.search('Dev evaluation', line)
            match_acc = re.search('Overall accuracy: [0-9]+\.[0-9]+%', line)
            
            if match_dev:
                prev_line_dev = True
            elif prev_line_dev and match_acc:
                prev_line_dev = False
                acc = float(match_acc.group(0)[18:-1])
                accuracies.append(acc)
    
    plt.plot(accuracies)
    plt.ylabel('Accuracy')
    plt.xlabel('Times Evaluated')
    plt.xlim(xmin=87)
    plt.ylim(ymin=95, ymax=97)
    plt.savefig(pltfilepath)
