///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003 Jason Baldridge, Gann Bierner and 
//                    University of Edinburgh (Michael White)
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

package opennlp.ccg.grammar;

import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;
import java.util.*;

import org.jdom.Element;


/**
 * Interface for categorial rules.
 *
 * @author Gann Bierner
 * @author Jason Baldridge
 * @author Michael White
 * @version $Revision: 1.5 $, $Date: 2009/11/30 20:36:15 $
 */
public interface Rule {

    /**
     * Apply this rule to some input categories.
     *
     * @param inputs the input categories to try to combine
     * @return the Category (or categories) resulting from using this Rule to combine the
     *         inputs
     * @exception UnifyFailure if the inputs cannot be combined by this Rule
     **/
    public List<Category> applyRule(Category[] inputs) throws UnifyFailure;

    /**
     * The number of arguments this rule takes.  For example, the arity of the
     * forward application rule of categorial grammar (X/Y Y => Y) is 2.
     *
     * @return the number of arguments this rule takes
     **/
    public int arity();

    /**
     * Returns the interned name of this rule.
     */
    public String name();
    
    /**
     * Returns the rule group which contains this rule.
     */
    public RuleGroup getRuleGroup();
    
    /**
     * Sets this rule's rule group.
     */
    public void setRuleGroup(RuleGroup ruleGroup);
    
    /** Returns an XML element representing the rule. */
    public Element toXml();
}

