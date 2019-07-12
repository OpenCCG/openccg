/* Generated By:JJTree: Do not edit this line. SimpleNode.java */

package opennlp.ccgbank.parse;

import java.util.List;

import opennlp.ccgbank.parse.CCGbankDerivation;
import opennlp.ccgbank.parse.CCGbankDerivationTreeConstants;

public class SimpleNode implements Node {
    //Javacc generated variables
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected CCGbankDerivation parser;

    //User defined variables

    // lex, sense, role triples
    public static class LexSenseRole {
        public String lex, sense, role;

        public LexSenseRole(String lex, String sense, String role) {
            this.lex = lex;
            this.sense = sense;
            this.role = role;
        }
    }

    //CCGbank id
    public String header;

    //The serial no of the gold standard parse
    String parseNo = "";

    //Node type eg:-Treenode,Leafnode,atomcat etc
    public String type = "";

    //Traps any feature which is leftover
    public String leftover;

    //Headedness info 0 or 1
    public String head = "";

    //No:of daughters of a node
    public String dtr = "";

    //Category Specification
    public String cat = "";

    //Category Specification without co-indexation info in leafnodes
    public String catRedundant = "";

    //Lexical information
    public String lex = "";

    //Part of speech info. eg: RB, IN etc
    public String pos = "";

    // The roles (or rel) that the node plays
    public List<LexSenseRole> nodeRoles = null;

    // The arg roles of a verbal cat
    public List<String> argRoles = null;

    //First token in the node scope
    Token first_token;

    //Final token in the node scope
    Token last_token;

    //Function which produces the content of the node.
    public String print() throws Exception {

        Token p = first_token;

        while (p != last_token) {
            cat = cat + p.image;
            p = p.next;
        }

        return cat + last_token.image;
    }


    //The remaining part incl comments is Javacc generated.

    public SimpleNode(int i) {
        id = i;
    }

    public SimpleNode(CCGbankDerivation p, int i) {
        this(i);
        parser = p;
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return the leftover
     */
    public String getLeftover() {
        return leftover;
    }

    public int getid(){
        return id;
    }


    public void jjtOpen() {
    }

    public void jjtClose() {
    }


    public void jjtSetParent(Node n) {
        parent = n;
    }

    public Node jjtGetParent() {
        return parent;
    }

    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild(int i) {
        return children[i];
    }

    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    /* You can override these two methods in subclasses of SimpleNode to
       customize the way the node appears when the tree is dumped.  If
       your output uses more than one line you should override
       toString(String), otherwise overriding toString() is probably all
       you need to do. */
    @Override
    public String toString() {
        return CCGbankDerivationTreeConstants.jjtNodeName[id];
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

  /* Override this method if you want to customize how the node dumps
     out its children. */

    public void dump(String prefix) {
        System.out.println(toString(prefix));
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode) children[i];
                if (n != null) {
                    n.dump(prefix + " ");
                }
            }
        }
    }
}

