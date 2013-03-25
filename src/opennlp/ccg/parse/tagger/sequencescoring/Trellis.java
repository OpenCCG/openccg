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

import java.util.ArrayList;
import java.util.List;

/**
 * A Trellis for sequence coding (of supertags, e.g.).
 * 
 * @author Dennis N. Mehay
 */
public class Trellis<A> {
    /** The dimensions of the Trellis. */
    private int cols, rows;
    
    /** The actual nuts and bolts of the Trellis. */
    private ArrayList<ArrayList<A>> trellis;

    /** Constructor with passed-in list of lists. */
    public Trellis(List<List<A>> inpt) {
        this.reshape(inpt.size(), inpt.get(0).size(), inpt);
    }
    
    /** Constructor with dimensions. */
    public Trellis(int cols, int rows, A dummy) {
        List<List<A>> tr = new ArrayList<List<A>>(cols);
        for(int i = 0; i < cols; i++) {
            ArrayList<A> tmp = new ArrayList<A>(rows);
            for(int j = 0; j < rows; j++) {
                tmp.add(dummy);
            }
            tr.add(tmp);
        }
        reshape(cols, rows, tr);
    }
    
    /** 
     * Reshape the dimensions (e.g., to accomodate a new sequence with a 
     * particular max beam width 
     */
    public void reshape(int cols, int rows, List<List<A>> inpt) {
        this.cols = cols;
        this.rows = rows;
        
        this.trellis = new ArrayList<ArrayList<A>>(cols);        
        for(List<A> la : inpt) {
            ArrayList<A> row = new ArrayList<A>(rows);
            for(A a : la) {
                row.add(a);
            }
            this.trellis.add(row);            
        }
    }
    
    /** What is the max beam width? */
    public int getWidth() { return rows; }
    /** What is the length of the sequence? */
    public int getLength() { return cols; }
    /** Clear out values in the trellis. */
    public void clear() { 
        for(int i = 0; i < cols; i++) {
            this.trellis.add(new ArrayList<A>(rows));
        }
    }
    /** Get the sequence options at index i. */
    public ArrayList<A> getOptions(int i) { return this.trellis.get(i); }
    
    /** Get a node in the Trellis (referenced by 2D coordinate). */
    public A getCoord(int i, int j) { 
        try {return this.trellis.get(i).get(j); 
        } catch(IndexOutOfBoundsException iobe) {
            return null;
        }
    }
    
    /** Set the value at a node in the Trellis (referenced by 2D coordinate) */
    public void setCoord(int i, int j, A val) { this.trellis.get(i).set(j, val); }
}
