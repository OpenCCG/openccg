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
package opennlp.ccg.hylo.graph;

import java.util.HashMap;
import java.util.Map;

import opennlp.ccg.hylo.Mode;

/**
 * A label for an {@linkplain LFEdge LF edge}. LF edge labels are drawn from 
 * {@link Mode}s, so this class encapsulates one.
 * <p>
 * Since certain modes reoccur frequently, this class maintains a cache of
 * modes mapped to edge labels, accessible via {@link #forMode(Mode)}.
 * 
 * @author <a href="http://www.ling.ohio-state.edu/~scott/">Scott Martin</a>
 */
public class LFEdgeLabel {

	final Mode mode;
	
	private static Map<Mode, LFEdgeLabel> labelCache;
	
	/**
	 * Creates an LF edge label with the specified mode.
	 * @param mode The mode representing the type of relation between the vertices.
	 * @throws IllegalArgumentException if <tt>mode</tt> is <tt>null</tt>.
	 */
	public LFEdgeLabel(Mode mode) {
		if(mode == null) {
			throw new IllegalArgumentException("mode is null");
		}
		
		this.mode = mode;
	}

	/**
	 * Gets a cached LF edge label for the specified mode, if one is available. If none
	 * has been created yet, a new LF edge label is created and cached for later use.
	 * Access to the cached LF edge labels is synchronized to avoid threading isues.
	 * 
	 * @param mode The mode to create a label for.
	 * @return Either a cached LF edge label corresponding to the specified mode, if one
	 * is available, or a newly created one.
	 */
	public static LFEdgeLabel forMode(Mode mode) {
		synchronized(LFEdgeLabel.class) {
			LFEdgeLabel l = null;
			
			if(labelCache == null) {
				 labelCache = new HashMap<Mode, LFEdgeLabel>();
			}
			else {			
				l = labelCache.get(mode);
			}
			
			if(l == null) {
				l = new LFEdgeLabel(mode);
				labelCache.put(mode, l);
			}
			
			return l;
		}
	}
	
	/**
	 * Gets the name of this label, as specified by its underlying 
	 * {@linkplain Mode mode}.
	 * @return The value of <tt>getMode().getName()</tt>.
	 */
	public String getName() {
		return mode.getName();
	}
	
	/**
	 * Gets the mode underlying this edge label.
	 * @return The mode specified at creation.
	 * @see #LFEdgeLabel(Mode)
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * Computes a hash code for this LF edge label based on the hash code of its
	 * underlying mode.
	 */
	@Override
	public int hashCode() {
		return 31 * mode.hashCode();
	}

	/**
	 * Tests whether this LF edge label is equivalent to another by comparing their
	 * modes, using their {@link Mode#equals(Object)} methods.
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof LFEdgeLabel) && mode.equals(((LFEdgeLabel)obj).mode);
	}

	/**
	 * Gets a string representation of this LF edge label.
	 * @return The value of <tt>getName()</tt>.
	 */
	@Override
	public String toString() {
		return getName();
	}
}
