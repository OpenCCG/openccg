///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2009 Dennis N. Mehay
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
package opennlp.ccg.parse.tagger;

/**
 * @author Dennis N. Mehay
 * @version $Revision: 1.1 $, $Date: 2010/09/21 04:12:41 $
 */
public class ProbIndexPair implements Comparable<ProbIndexPair> {
	
    public Double a;
    public Integer b;
    
    public ProbIndexPair(Double a, Integer b) { this.a=a; this.b=b; }
    
    public int compareTo(ProbIndexPair p) { return (-1 * (this.a).compareTo(p.a)); }
    
    public static void main(String[] args) {
        ProbIndexPair p1 = new ProbIndexPair(new Double(4.0), new Integer(5));
        ProbIndexPair p2 = new ProbIndexPair(new Double(3.0), new Integer(5));
        ProbIndexPair p3 = new ProbIndexPair(new Double(2.0), new Integer(5));
        ProbIndexPair p4 = new ProbIndexPair(new Double(4.0), new Integer(5));
        System.out.println("p1 < p2? "+(p1.compareTo(p2)<0));
        System.out.println("p2 < p3? "+(p2.compareTo(p3)<0));
        System.out.println("p1 < p3? "+(p1.compareTo(p3)<0));
        System.out.println("p1 == p4? "+(p1.compareTo(p4)==0));
        System.out.println("p2 > p1? "+(p2.compareTo(p1)>0));
        System.out.println("p3 > p2? "+(p3.compareTo(p2)>0));
        System.out.println("p3 > p1? "+(p3.compareTo(p1)>0));
    }
}
