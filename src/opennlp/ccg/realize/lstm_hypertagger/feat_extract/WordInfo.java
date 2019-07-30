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

package opennlp.ccg.realize.lstm_hypertagger.feat_extract;

/** Contains supertag, POS, and lemma for some word */
public class WordInfo {
	private String supertag;
	private String pos;
	private String lemma;
	
	public WordInfo(String supertag, String pos, String lemma) {
		this.supertag = supertag;
		this.pos = pos;
		this.lemma = lemma;
	}
	public String getSupertag() {
		return supertag;
	}
	public String getPos() {
		return pos;
	}
	public String getLemma() {
		return lemma;
	}
	public String toString() {
		return "(" + lemma + ", " + pos + ", " + supertag + ")";
	}
}
