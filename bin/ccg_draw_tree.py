#
# ccg_draw_tree uses nltk.Tree to draw a tree from a CCGbank .auto file, 
# or to draw two trees from two .auto files
#

import sys

if len(sys.argv) == 1 or sys.argv[1] == '-h' or sys.argv[1] == '--help':
    print 'Usage: ccg_draw_tree <autofile> <deriv_id> (<autofile>) (<derivid>)'
    sys.exit(0)

autofile = sys.argv[1]
deriv_id = sys.argv[2]

autofile2 = None
deriv_id2 = None

if len(sys.argv) >= 4: 
    autofile2 = sys.argv[3]
    deriv_id2 = deriv_id
    if len(sys.argv) >= 5:
        deriv_id2 = sys.argv[4]

def get_deriv(autofile, deriv_id):
    print 'reading ' + deriv_id + ' from ' + autofile
    found_it = False
    file = open(autofile, 'rU')
    for line in file:
        if found_it == True:
            return line
        if line[0:2] == 'ID':
            if line.split()[0].split('=')[1] == deriv_id:
                found_it = True
    raise NameError('could not find ' + deriv_id + '!')

deriv = get_deriv(autofile, deriv_id)
deriv2 = None
if autofile2 != None:
    deriv2 = get_deriv(autofile2, deriv_id2)

print 'importing nltk.Tree'

from nltk import Tree
from nltk.draw.tree import draw_trees

ccgbank_node_pattern = r'<T.*?>'
ccgbank_leaf_pattern = r'<L.*?>'

# nb: the parens around leaves ends up creating blank nodes above leaves
def parse_ccgbank_node(s):
    if s =='': return ''
    return s.split(' ')[1]

def parse_ccgbank_leaf(s):
    tokens = s.split(' ')
    return Tree(tokens[1], [tokens[4]])

def excise_empty_nodes(t):
    if not isinstance(t,Tree): return t
    if t.node == '': return excise_empty_nodes(t[0])
    return Tree(t.node, [excise_empty_nodes(st) for st in t])

# nb: returns tree with blank nodes excised
def parse_ccgbank_tree(s):
    t = Tree.parse(s, 
                   parse_node=parse_ccgbank_node, 
                   parse_leaf=parse_ccgbank_leaf, 
                   node_pattern=ccgbank_node_pattern, 
                   leaf_pattern=ccgbank_leaf_pattern)
    return excise_empty_nodes(t)

print
print 'parsing: ' + deriv
t = parse_ccgbank_tree(deriv)
print t

t2 = None
if deriv2 != None:
    print
    print 'parsing: ' + deriv2
    t2 = parse_ccgbank_tree(deriv2)
    print t2

print
if t2 == None:
    print 'drawing tree'
    draw_trees(t)
else:
    print 'drawing trees'
    draw_trees(t,t2)
