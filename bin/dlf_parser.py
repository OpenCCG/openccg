#
# dlf_parser.py (invoked by ccg-draw-graph) uses graphviz's dot to visualize (D)LF graphs
#
# author: Jonathan Barker (with minor contributions by Michael White)
# license: LGPL
#

from xml.etree.ElementTree import ElementTree
import optparse, sys, codecs, xml, os
from collections import defaultdict

# Parse arguments
op = optparse.OptionParser()

op.add_option("-i", "--input", type="string", help="input source: file or <stdin>(default)", default=sys.stdin)

op.add_option("-m", "--moses", type="string", help="file/directory prefix for moses output", default=None)

op.add_option("-v", "--visualize", type="string", help="file/directory prefix for .pdf output", default=None)

op.add_option("-w", "--wordindices", action="store_true", help="include word indices", default=False)

op.add_option("-c", "--classnames", action="store_true", help="include semantic class names", default=False)

(ops, args) = op.parse_args(sys.argv)

# Parse input file
input_source = ops.input if ops.input is sys.stdin else open(ops.input, "rt")
raw = xml.etree.ElementTree.XML(input_source.read())
snum = "None"
att_id = 0

# Get word number
def wordNum(wid):
    if wid.startswith("x"):
        return -1
    else:
        return int(wid[1:].strip("f"))

# Get node span
def span(nid, graph, w):
    if wordNum(nid) in w:
        return []
    w.append(wordNum(nid))
    for n, e in graph[nid]:
        if wordNum(n) not in w:
            w.append(wordNum(n))
            w.extend(span(n, graph, w))
    return w

# findall wrapper
def findAll(elem, match):
    return max(elem.findall(match), [])

# Class for representing predicates and attributes
class Pred:
    def __init__(self):
        self.attrib = []
        self.one_of = []
        self.opt = []

# Class for representing nodes, contains predicate and attribute information
class Node:
    def __init__(self):
        self.id = ""
        self.className = ""
        self.preds = defaultdict(Pred)

    def addPred(self, pred, attrib, one_of, opt):
        self.preds[pred].attrib.extend(attrib)
        self.preds[pred].one_of.extend(one_of)
        self.preds[pred].opt.extend(opt)

    def moses(self, graph):
        tree = " <tree label=\""
        # label
        pred = self.pred
        if pred is None:
            pred = self.id
        tree += "_".join([pred]+[k.upper()+"_"+v for (k,v) in self.attrib])
        # span
        tree += "\" span=\""
        s = span(self.id, graph, [])
        tree += str(min(s))+"-"+str(max(s))
        tree += "\"/>"
        return tree

    def dot(self):
        dot_node = self.id+" [label=<"
        withClassName = ops.classnames and len(self.className) > 0
        if ops.wordindices:
            dot_node += self.id
            if withClassName:
                dot_node += ":"
        if withClassName:
            dot_node += self.className
        if len(self.preds) > 0:
            if ops.wordindices or withClassName:
                dot_node += ":"
            labels = []
            for pname, p in self.preds.items():
                label = ""
                # pred
                label += "<FONT POINT-SIZE=\"20.0\">"+pname+"</FONT>"
                # att
                atts = []
                if len(p.attrib) > 0:
                    atts.append(",".join(["&lt;"+k.upper()+"&gt;"+v for (k, v) in p.attrib]))
                if len(p.one_of) > 0:
                    atts.append("|".join(["&lt;"+k.upper()+"&gt;"+v for (k, v) in p.one_of]))
                if len(p.opt) > 0:
                    atts.append("("+",".join(["&lt;"+k.upper()+"&gt;"+v for (k, v) in p.opt])+")?")
                if len(atts) > 0:
                    label += "<FONT POINT-SIZE=\"8.0\">"+",".join(atts)+"</FONT>"
                labels.append(label)
            dot_node += " | ".join(labels)
        dot_node += ">];\n"
        return dot_node

    def info(self):
        print "Node id:",self.id
        for pname, p in self.preds:
            print "\tPred:",self.pred
            print "\t\tAttrib:",p.attrib
            print "\t\tOne_of:",p.one_of
            print "\t\tOpt:",p.opt
        print "----------------"

# Returns just the id, stripping the class (if any)
def parseId(str):
    colonIndex = str.find(":")
    if colonIndex > 0: return str[:colonIndex]
    else: return str

# Returns the class from the id, or the empty string if none
def parseClass(str):
    colonIndex = str.find(":")
    if colonIndex > 0: return str[colonIndex+1:]
    else: return ""
            
# Method for parsing <node>
def parseNode(node, graph, nodes):
    n = nodes[node.get("id")]
    n.id = parseId(node.get("id"))
    n.className = parseClass(node.get("id"))
    attrib = [(k, v) for (k, v) in node.items() if k not in ["id", "pred"]]
    if node.get("pred") is not None:
        n.addPred(node.get("pred"), attrib, [], [])
    nodes[n.id] = n
    
    for elem in list(node):
        if elem.tag == "rel":
            parseRel(elem, n.id, graph, nodes, "")
        elif elem.tag == "one-of":
            parseOneOf(elem, n, attrib, node.get("pred"), graph, nodes)
        elif elem.tag == "opt":
            parseOpt(elem, n, graph, nodes)
        elif elem.tag == "node":
            parseNode(elem, graph, nodes)
        else:
            print snum+": Unexpected tag <"+elem.tag+"> after <node>"
            quit()

# Method for parsing <opt>
def parseOpt(opt, node, graph, nodes):
    for elem in list(opt):
        if elem.tag == "atts":
            for pname, p in node.preds.items():
                node.addPred(pname, [], [], [(k, v) for (k, v) in elem.items() if k not in ["id", "pred"]])
        elif elem.tag == "rel":
            parseRel(elem, node.id, graph, nodes, "style=dotted, ")
        else:
            print snum+": Unexpected tag <"+elem.tag+"> after <node>"
            quit()

# Method for parsing <one-of>
def parseOneOf(oneof, node, attrib, pred, graph, nodes):
    global att_id
    num_att = 0
    for elem in list(oneof):
        if elem.tag == "atts":
            if pred is not None:
                node.addPred(pred, [], [(k, v) for (k, v) in elem.items() if k not in ["id", "pred"]], [])
            else:
                node.addPred(elem.get("pred"), [], [(k, v) for (k, v) in elem.items() if k not in ["id", "pred"]], [])
            if len(list(elem)) > 0:
                num_att += 1
                new_att = Node()
                new_att.id = "att"+str(att_id)
                att_id += 1
                new_att.addPred(str(num_att), [], [], [])
                nodes[new_att.id] = new_att
                graph[node.id].append((new_att.id, " [style=dashed];\n"))
                for rel in list(elem):
                    parseRel(rel, new_att.id, graph, nodes, "")
        elif elem.tag == "rel":
            num_att += 1
            new_att = Node()
            new_att.id = "att"+str(att_id)
            att_id += 1
            new_att.addPred(str(num_att), [], [], [])
            nodes[new_att.id] = new_att
            graph[node.id].append((new_att.id, " [style=dashed];\n"))
            parseRel(elem, new_att.id, graph, nodes, "")
        else:
            print snum+": Unexpected tag <"+elem.tag+"> after <one-of>"
            quit()

# Method for parsing <rel>
def parseRel(rel, nid, graph, nodes, style):
    # <rel>
    for subnode in list(rel):
        if subnode.tag == "node":
            edge_label = " ["+style+"label = \""+rel.get("name")+"\"];\n"
            if subnode.get("id") is None:
                graph[nid].append((parseId(subnode.get("idref")), edge_label))
            else:
                graph[nid].append((parseId(subnode.get("id")), edge_label))
                parseNode(subnode, graph, nodes)
        elif subnode.tag == "one-of":
            subnode.set("name", rel.get("name"))
            parseRel(subnode, nid, graph, nodes, "style=dashed, ")
        else:
            print snum+": Unexpected tag <"+subnode.tag+"> after <rel>"
            quit()
# <item> 
item_no = 0
for item in findAll(raw, "item"):
    item_no += 1
    if item.get("numOfParses") == "0":
        print "Removing "+item.get("info")
    else:
        snum = item.get("info")
        # <lf>
        lf_num = 0
        for lf in findAll(item, "lf"):
            graph = defaultdict(list)
            nodes = defaultdict(Node)
            
            # <node>
            for node in list(lf):
                if node.tag == "node":
                    parseNode(node, graph, nodes)
                else:
                    print snum+": Unexpected tag <"+node.tag+"> after <lf>"
                    quit()

            # Plot the graph with GraphViz
            if ops.visualize != None:
                viz_name = ""
                if type(item.get("info")) != type("string"):
                    viz_name = ops.visualize+".item"+str(item_no)+"."+str(lf_num)
                else:
                    viz_name = ops.visualize+"."+item.get("info")+"."+str(lf_num)
                viz = codecs.open(viz_name+".dot", "w", "utf-8")
                viz.write("digraph lf {\n")
                for (k, v) in nodes.items():
                    viz.write(v.dot())
                for (left, rights) in graph.items():
                    for right in rights:
                        viz.write(left+"->"+right[0]+right[1])
                viz.write("}\n")
                viz.close()
                os.system("dot -Tpdf "+viz_name+".dot -o "+viz_name+".pdf")
                os.system("rm "+viz_name+".dot")
        lf_num += 1
