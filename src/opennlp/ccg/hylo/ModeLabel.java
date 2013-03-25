///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2002-4 Jason Baldridge and University of Edinburgh (Michael White)
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

package opennlp.ccg.hylo;

import opennlp.ccg.synsem.*;
import opennlp.ccg.unify.*;
import org.jdom.*;

/**
 * A modality label.
 * Types are not currently supported.
 *
 * @author      Jason Baldridge
 * @author      Michael White
 * @version     $Revision: 1.5 $, $Date: 2009/07/17 04:23:30 $
 **/
public final class ModeLabel extends HyloAtom implements Mode {

	private static final long serialVersionUID = -4101305505903588678L;

	public ModeLabel(String name) {
        super(name);
    }

    public LF copy() {
        return new ModeLabel(_name);
    }

    public Object unify(Object u, Substitution sub) throws UnifyFailure {
        if (equals(u)) return this;
        return super.unify(u, sub);
    }
    
    /**
     * Returns an XML representation of this LF (not currently supported).
     * Throws a runtime exception.
     */
    public Element toXml() {
        throw new RuntimeException("toXml() not currently supported for ModeLabel.");
    }
}
