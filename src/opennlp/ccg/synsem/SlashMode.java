///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002 Jason Baldridge
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

package opennlp.ccg.synsem;

import java.io.Serializable;
import org.jdom.Element;

import opennlp.ccg.unify.*;

/**
 * A mode that can decorate a categorial slash.
 * 
 * @author Jason Baldridge
 * @version $Revision: 1.5 $, $Date: 2009/07/17 04:23:30 $
 */
public final class SlashMode implements Modality, Serializable {

	private static final long serialVersionUID = -2387797559890373347L;

	public static final byte All = 0;

	public static final byte ApplicationOnly = 1;

	public static final byte Associative = 2;

	public static final byte Permutative = 3;

	public static final byte PermutativeRight = 4;

	public static final byte PermutativeLeft = 5;

	public static final byte APRight = 6;

	public static final byte APLeft = 7;

	private byte _mode;

	public SlashMode(Element el) {
		String m = el.getAttributeValue("mode");
		if (m == null)
			m = el.getAttributeValue("m");
		if (m == null) {
			m = ".";
		}

		_mode = byteVal(m);
	}

	public SlashMode() {
		this(".");
	}

	public SlashMode(String m) {
		_mode = byteVal(m);
	}

	private SlashMode(byte m) {
		_mode = m;
	}

	public Object copy() {
		return new SlashMode(_mode);
	}

	/** Returns a hash code based on the mode. */
	public int hashCode() { return 31 * _mode; }
	
	/** Returns whether this slash mode equals the given object based on the mode. */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj.getClass() != this.getClass()) return false;
		SlashMode m = (SlashMode) obj;
		return _mode == m._mode;
	}
	
//	public boolean equals(SlashMode m) {
//		return _mode == m._mode;
//	}

	public boolean occurs(Variable var) {
		return false;
	}

	public void unifyCheck(Object o) throws UnifyFailure {
		if (!(o instanceof VarModality || (o instanceof SlashMode && modesMatch(
				_mode, ((SlashMode) o)._mode)))) {
			throw new UnifyFailure();
		}
	}

	public Object unify(Object o, Substitution sub) throws UnifyFailure {
		if (o instanceof VarModality) {
			return ((VarModality) o).unify(this, sub);
		} else if (o instanceof SlashMode) {
			if (modesMatch(_mode, ((SlashMode) o)._mode)) {
				return copy();
			} else {
				throw new UnifyFailure();
			}
		} else {
			throw new UnifyFailure();
		}
	}

	public Object fill(Substitution sub) throws UnifyFailure {
		return copy();
	}

	public String toString(byte slashDir) {
		if (slashDir == getDirection()) {
			switch (_mode) {
			case PermutativeRight:
				return "x";
			case PermutativeLeft:
				return "x";
			case APRight:
				return "";
			case APLeft:
				return "";
			default:
				return toString();
			}
		} else {
			return toString();
		}
	}

	public String toString() {
		switch (_mode) {
		case All:
			return ".";
		case ApplicationOnly:
			return "*";
		case Associative:
			return "^";
		case Permutative:
			return "x";
		case PermutativeRight:
			return "x>";
		case PermutativeLeft:
			return "<x";
		case APRight:
			return ">";
		case APLeft:
			return "<";
		default:
			return ".";
		}
	}

	public String toTeX(byte slashDir) {
		if (slashDir == getDirection()) {
			switch (_mode) {
			case PermutativeRight:
				return "x";
			case PermutativeLeft:
				return "x";
			case APRight:
				return "";
			case APLeft:
				return "";
			default:
				return toTeX();
			}
		} else {
			return toTeX();
		}
	}

	public String toTeX() {
		switch (_mode) {
		case All:
			return ".";
		case ApplicationOnly:
			return "*";
		case Associative:
			return "\\diamond";
		case Permutative:
			return "x";
		case PermutativeRight:
			return "x>";
		case PermutativeLeft:
			return "<x";
		case APRight:
			return ">";
		case APLeft:
			return "<";
		default:
			return ".";
		}
	}

	private static byte byteVal(String m) {
		if (m.equals(".")) {
			return All;
		} else if (m.equals(">")) {
			return APRight;
		} else if (m.equals("<")) {
			return APLeft;
		} else if (m.equals("*")) {
			return ApplicationOnly;
		} else if (m.equals("^")) {
			return Associative;
		} else if (m.equals("x")) {
			return Permutative;
		} else if (m.equals("x>")) {
			return PermutativeRight;
		} else if (m.equals("<x")) {
			return PermutativeLeft;
		} else {
			return All;
		}
	}

	public byte getDirection() {
		switch (_mode) {
		case PermutativeRight:
			return Slash.R;
		case PermutativeLeft:
			return Slash.L;
		case APRight:
			return Slash.R;
		case APLeft:
			return Slash.L;
		default:
			return Slash.B;
		}
	}

	private static boolean modesMatch(byte m1, byte m2) {
		boolean doMatch;
		if (m1 == m2) {
			doMatch = true;
		} else if (m1 == All || m2 == All) {
			doMatch = true;
		} else if (m1 == ApplicationOnly && m2 == ApplicationOnly) {
			doMatch = true;
		} else if (m1 == ApplicationOnly || m2 == ApplicationOnly) {
			doMatch = false;
		} else if (m1 == Associative) {
			if (m2 == APRight || m2 == APLeft) {
				doMatch = true;
			} else {
				doMatch = false;
			}
		} else if (m1 == APRight) {
			if (m2 == PermutativeRight || m2 == Permutative
					|| m2 == Associative) {
				doMatch = true;
			} else {
				doMatch = false;
			}
		} else if (m1 == APLeft) {
			if (m2 == PermutativeLeft || m2 == Permutative || m2 == Associative) {
				doMatch = true;
			} else {
				doMatch = false;
			}
		} else if (m1 == PermutativeRight) {
			if (m2 == APRight || m2 == Permutative) {
				doMatch = true;
			} else {
				doMatch = false;
			}
		} else if (m1 == PermutativeLeft || m2 == Permutative) {
			if (m2 == APLeft) {
				doMatch = true;
			} else {
				doMatch = false;
			}
		} else if (m1 == Permutative) {
			if (m2 == APLeft || m2 == APRight || m2 == PermutativeRight
					|| m2 == PermutativeLeft) {
				doMatch = true;
			} else {
				doMatch = false;
			}
		} else {
			doMatch = false;
		}
		// System.out.println(m1 +" :: "+m2 + " => " + doMatch);
		return doMatch;
	}
}
