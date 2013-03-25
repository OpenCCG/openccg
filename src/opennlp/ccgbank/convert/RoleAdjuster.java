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

package opennlp.ccgbank.convert;

/**
 * Utility class for adjusting roles and augmenting cat names with roles. 
 */ 
public class RoleAdjuster {

	/** Returns the adjusted argument roles, making guesses at the missing roles. */
	public String getAdjustedRoles(String cat, String roles) {
		// aux things like "have to"
		if (roles.equals("null e") || roles.equals("e e")) {
			if (cat.indexOf("np[thr]") < 0)
				return "Arg0 Arg1";
			else 
				return "e Arg1";
		}
		// vp mods
		if (roles.startsWith("null e")) {
			return "null ArgM" + roles.substring("null e".length());
		}
		// missing subjects, mostly
		if (roles.startsWith("null")) {
			String rest = roles.substring("null".length());
			return addMissingArg(cat, rest);
		}
		// various
		if (roles.startsWith("e")) {
			String rest = roles.substring("e".length());
			// mods
			if (cat.indexOf("_~") > 0)
				return "ArgM" + rest;
			// leave expletives unchanged
			if (cat.indexOf("np[expl]") > 0 || cat.indexOf("np[thr]") > 0)
				return roles;
			// otherwise add standard guess
			return addMissingArg(cat, rest);
		}
		// otherwise unchanged
		return roles;
	}

	// add guess at missing arg
	private String addMissingArg(String cat, String rest) {
		// distinguish passive
		if (cat.startsWith("s[pss]")) {
			if (rest.indexOf("Arg1") < 0)
				return "Arg1" + rest;
			else 
				return "Arg2" + rest;
		}
		// otherwise Arg0 or Arg1
		if (rest.indexOf("Arg0") < 0)
			return "Arg0" + rest;
		else 
			return "Arg1" + rest;
	}
	
	/** Returns the cat name augmented with the given argument roles. */
	public String getCatPlusRoles(String cat, String roles) {
		return cat + ":" + roles.replaceAll(" ", "+");
	}
}
