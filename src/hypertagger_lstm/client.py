import sys
import rpyc

if __name__=='__main__':
    port_num = int(sys.argv[1])
    annot_seq = sys.argv[2]
    
    c = rpyc.connect('localhost', port_num)
    results = c.root.get_hypertag_results(annot_seq)
    print(results)