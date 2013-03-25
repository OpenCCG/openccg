//////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Scott Martin
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
package opennlp.ccg.alignment;

/**
 * A set of {@link Enum} constants for describing the status of a {@linkplain Mapping mapping}, either as
 * {@link #POSSIBLE} or {@link #SURE}.
 * <p>
 * Statuses have a corresponding {@linkplain #getAbbreviation() abbreviated form} for use when mappings are
 * formatted and parsed as strings. The enum constants are arranged in order of strength of surety, so that 
 * {@link Enum#ordinal()} returns numbers in order of increasing surety.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 * @see MappingFormat
 */
public enum Status {
	
	/**
	 * The status of a mapping that is only possible, not sure.
	 */
	POSSIBLE,
	
	/**
	 * The status of a sure mapping (not just possible).
	 */
	SURE;
	
	/**
	 * An abbreviated form for this status, for use in parsing and formatting.
	 * @see MappingFormat
	 */
	final String abbreviation;
	
	private Status() {
		this.abbreviation = name().substring(0, 1);
	}
	
	/**
	 * Gets the abbreviated form of this status, &quot;S&quot; for {@link #SURE}
	 * and &quot;P&quot; for {@link #POSSIBLE}.
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * Gives the status constant corresponding to the given abbreviation.
	 * @param abbreviation The abbreviated form to find a status constant for.
	 * @return A status constant if one is found whose {@link #getAbbreviation()} is equal to the specified 
	 * abbreviation, otherwise <code>null</code>.
	 */
	public static Status forAbbreviation(String abbreviation) {
		for(Status s : values()) {
			if(s.abbreviation.equals(abbreviation)) {
				return s;
			}
		}
		
		return null;
	}
}