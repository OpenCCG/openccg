///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 Jason Baldridge, Gann Bierner and 
//                      University of Edinburgh (Michael White)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////
package opennlp.ccg.parse;

import opennlp.ccg.synsem.*;
import opennlp.ccg.grammar.*;

import java.io.Serializable;
import java.util.*;

/**
 * Record the steps taken in a derivation.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.12 $, $Date: 2009/12/16 22:39:32 $
 */
public class DerivationHistory implements Serializable, Comparable<DerivationHistory> {

	private static final long serialVersionUID = 2867339743258182859L;
	
	private Sign[] _inputs;
    private Sign _output;
    private Rule _rule;
    private boolean _noHistory = false;
    private transient int _complexity = -1;
    
    /** Constructor for a sign with no prior history. */
    public DerivationHistory(Sign output) {
        _noHistory = true;
        _output = output;
    }
    
    /** Constructor for a sign created by rule. */
    public DerivationHistory(Sign[] inputs, Sign output, Rule rule) {
        _inputs = new Sign[inputs.length];
        for (int i=0; i < inputs.length; i++) {
            _inputs[i] = inputs[i];
        }
        _output = output;
        _rule = rule;
    }


    /** Returns true iff the history is empty. */
    public boolean isEmpty() { return _noHistory; }
    
    /** Returns the inputs (or null if none). */
    public Sign[] getInputs() { return _inputs; }
    
    /** Returns the output. */
    public Sign getOutput() { return _output; }
    
    /** Returns the rule. */
    public Rule getRule() { return _rule; }
    
    
    /** Returns the derivation history in vertical list form. */
    public String toString() {
        return toString(maxRuleLen());
    }
    
    // returns the derivation history given the max rule len, for alignment
    private String toString(int maxRuleLen) {
        StringBuffer sb = new StringBuffer();
        // lex item
        if (_noHistory) {
            sb.append("(lex) ");
            for (int i = 5; i < maxRuleLen; i++) { sb.append(' '); }
            sb.append(_output.toString()).append('\n');
            return sb.toString();
        }
        // inputs
        for (int i=0; i < _inputs.length; i++) {
            sb.append(_inputs[i].getDerivationHistory().toString(maxRuleLen));
        }
        // type-changing rule (possibly)
        String ruleName = _rule.name();
        TypeChangingRule tcr = Grammar.theGrammar.rules.getTypeChangingRule(ruleName);
        if (tcr != null) {
            sb.append("(gram) ");
            for (int i = 6; i < maxRuleLen; i++) { sb.append(' '); }
            sb.append(tcr.toString()).append('\n');
        }
        // this rule and result
        sb.append('(').append(ruleName).append(") ");
        for (int i = (ruleName.length() + 2); i < maxRuleLen; i++) { sb.append(' '); }
        sb.append(_output.toString()).append('\n');
        // done
        return sb.toString();
    }
    
    // returns the max length of rule names (including parens)
    private int maxRuleLen() {
        if (_noHistory) { return 6; }
        int max = 0;
        for (int i=0; i < _inputs.length; i++) {
            max = Math.max(max, _inputs[i].getDerivationHistory().maxRuleLen());
        }
        max = Math.max(max, _rule.name().length() + 2);
        return max;
    }
    
    /** Returns the complexity of the derivation, as the sum of 
        the number of steps, plus the number of composition or 
        substitution steps, plus the number of crossing steps. */
    public int complexity() {
    	if (_complexity > 0) return _complexity;
        if (_noHistory) return 0;
        int retval = 1;
        String ruleName = _rule.name();
        if (ruleName.length() > 1 && (ruleName.charAt(0) == '>' || ruleName.charAt(0) == '<')) {
            if (ruleName.charAt(1) == 'B' || ruleName.charAt(1) == 'S') {
                retval++;
                if (ruleName.length() == 3 && ruleName.charAt(2) == 'x') retval++;
            }
        }
        for (int i=0; i < _inputs.length; i++) {
            retval += _inputs[i].getDerivationHistory().complexity();
        }
        _complexity = retval;
        return retval;
    }
    
    /** Returns whether the derivation contains a unary rule cycle. */
    public boolean containsCycle() {
    	if (_noHistory || _inputs.length != 1) return false;
    	List<Rule> rulesSeen = new ArrayList<Rule>(4);
    	rulesSeen.add(_rule);
    	return _inputs[0].getDerivationHistory().containsCycle(rulesSeen);
    }
    
    // recursive cycle check
    private boolean containsCycle(List<Rule> rulesSeen) {
    	if (_noHistory || _inputs.length != 1) return false;
    	if (rulesSeen.contains(_rule)) return true;
    	rulesSeen.add(_rule);
    	return _inputs[0].getDerivationHistory().containsCycle(rulesSeen);
    }

    /** Recursively compares derivation histories by their complexity. */
	public int compareTo(DerivationHistory dh) {
		int c1 = complexity(); int c2 = dh.complexity();
		if (c1 < c2) return -1;
		if (c1 > c2) return 1;
		if (_noHistory) return 0;
		if (_inputs.length < dh._inputs.length) return -1;
		if (_inputs.length > dh._inputs.length) return 1;
		for (int i=0; i < _inputs.length; i++) {
			int cmp = _inputs[i].getDerivationHistory().compareTo(dh._inputs[i].getDerivationHistory());
			if (cmp != 0) return cmp;
		}
		return 0;
	}
}

