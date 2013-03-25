///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2005-2009 Scott Martin, Rajakrishan Rajkumar and Michael White
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

/*
 * $Id: CCGBankTaskTestbed.java,v 1.3 2010/12/09 04:58:12 mwhite14850 Exp $ 
 */
package opennlp.ccgbank;

import java.io.File;

import org.apache.tools.ant.Task;


/**
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.3 $
 *
 */
public class CCGBankTaskTestbed extends Task {
	
	boolean debugDerivations = false, showSem = false;
	File text, factors, combos, preds, treeAuxFile;
	
	
	/**
	 * @return the combos
	 */
	public File getCombos() {
		return combos;
	}

	
	/**
	 * @return the debugDerivations
	 */
	public boolean isDebugDerivations() {
		return debugDerivations;
	}

	
	/**
	 * @return the showSem
	 */
	public boolean isShowsSem() {
		return showSem;
	}

	
	/**
	 * @return the factors
	 */
	public File getFactors() {
		return factors;
	}

	
	/**
	 * @return the preds
	 */
	public File getPreds() {
		return preds;
	}

	
	/**
	 * @return the text
	 */
	public File getText() {
		return text;
	}

	//Get the file which stores info about the id info of treenodes
	public File getTree() {
		return treeAuxFile;
	}
	
	/**
	 * @param combos the combos to set
	 */
	public void setCombos(File combos) {
		this.combos = combos;
	}
	
	/**
	 * @param debugDerivations the debugDerivations to set
	 */
	public void setDebugDerivations(boolean debugDerivations) {
		this.debugDerivations = debugDerivations;
	}
	
	/**
	 * @param showSem the showSem to set
	 */
	public void setShowSem(boolean showSem) {
		this.showSem = showSem;
	}
	
	/**
	 * @param factors the factors to set
	 */
	public void setFactors(File factors) {
		this.factors = factors;
	}
	
	/**
	 * @param preds the preds to set
	 */
	public void setPreds(File preds) {
		this.preds = preds;
	}
	
	/**
	 * @param text the text to set
	 */
	public void setText(File text) {
		this.text = text;
	}
	
	public void setTree(File treeAuxFile) {
		this.treeAuxFile = treeAuxFile;
	}
}
