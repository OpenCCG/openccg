///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010 Dennis N. Mehay
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed inp the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////


package opennlp.ccg.parse.tagger.sequencescoring;

import java.util.List;

/**
 * An abstraction for lists of backpointers in trellises, lattices, etc.
 * Each backpointer is a List of Integer's that give backpointers to the j-th highest
 * scoring paths (where 1 <= j <= N==len(Backpointer) are the indices of the 
 * internal list of the backpointer).
 * 
 * @author Dennis N. Mehay
 */
public class Backpointer {
    private List<Integer> bkpts;
    public Backpointer(List<Integer> bkpts) {
        this.bkpts = bkpts;
    }
    public List<Integer> getBkpts() { return bkpts; }
    public Integer get(int i) { return bkpts.get(i); }
    public int size() { return bkpts.size(); }
}
