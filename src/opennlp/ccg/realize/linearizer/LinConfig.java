///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2018 Reid Fu
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

package opennlp.ccg.realize.linearizer;

/** Linearization configuration object */
public class LinConfig {
	private String featOrderName;
	private int parenSubtreeSize;
	private boolean relSeq;
	private String childOrderName;
	
	public LinConfig(String featOrderName, int parenSubtreeSize, boolean relSeq, String childOrderName) {
		this.featOrderName = featOrderName;
		this.parenSubtreeSize = parenSubtreeSize;
		this.relSeq = relSeq;
		this.childOrderName = childOrderName;
	}
	public String featOrderName() {
		return featOrderName;
	}
	public String[] featOrder() {
		return FeatOrders.getFeatOrder(featOrderName);
	}
	public int parenSubtreeSize() {
		return parenSubtreeSize;
	}
	public boolean relSeq() {
		return relSeq;
	}
	public String childOrderName() {
		if(parenSubtreeSize > 0) {
			return childOrderName + "paren";
		}
		return childOrderName;
	}
	public String[] childOrder() {
		return ChildOrders.getChildOrder(childOrderName);
	}
	public Linearizer getLinearizer() {
		if(childOrderName.equals(ChildOrders.ENG)) {
			return new EngLinearizer();
		} else if(childOrderName.equals(ChildOrders.ORACLE)) {
			return new OracleLinearizer();
		}
		return null;
	}
	public String toString() {
		return "(" + featOrderName + ", " + parenSubtreeSize + ", " + relSeq + ", " + childOrderName + ")";
	}
}
