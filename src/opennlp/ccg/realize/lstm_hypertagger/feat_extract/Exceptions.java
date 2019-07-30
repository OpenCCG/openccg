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

/**
 * Exceptions used in LSTM hypertagger components
 * @author Reid Fu
 */
@SuppressWarnings("serial")
public class Exceptions {
	public static abstract class CCGXMLParseException extends Exception {
		private String sentence;
		private String reason;
		
		public CCGXMLParseException(String sentence, String reason) {
			this.sentence = sentence;
			this.reason = reason;
		}
		public void printSkipMessage() {
			System.err.println("Skipping sentence \"" + sentence + "\": " + reason);
		}
	}
	
	public static class NoPredicatesException extends CCGXMLParseException {
		public NoPredicatesException(String sentence) {
			super(sentence, "No predicates in sentence");
		}
	}
	
	public static class HeadNotPredicateException extends CCGXMLParseException {
		public HeadNotPredicateException(String sentence) {
			super(sentence, "Head word is not predicate");
		}
	}
	
	public static class NoRelationNameException extends CCGXMLParseException {
		public NoRelationNameException(String sentence, String relation) {
			super(sentence, "No relation name for " + relation);
		}
	}
}
