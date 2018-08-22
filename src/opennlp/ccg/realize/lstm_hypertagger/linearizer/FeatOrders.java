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

package opennlp.ccg.realize.lstm_hypertagger.linearizer;

public class FeatOrders {
	public static final String[] FULL = {"XC","ZD","ZM","ZN","ZP","ZT","FO","NA",
			"CN","CT","A0N","A1N","A2N","A3N","A4N","A5N","A0P","A1P","A2P","A3P","A4P","A5P",
			"X0D","X1D","X2D","X3D","X4D","X5D","MP","XM","RN","PP"};
	public static final String ALL_REL = "allrel";
	public static final String[] ALL_REL_ORDER = {"XC","ZD","ZM","ZN","ZP","ZT","FO","NA","PR#5","AT#5"};
	public static final String ALL_REL_PNAME = "allrel_pname";
	public static final String[] ALL_REL_PNAME_ORDER = {"XC","ZD","ZM","ZN","ZP","ZT","FO","NA","PR#5","AT#5","RN#5"};
	public static final String ALL_REL_CNAME = "allrel_cname";
	public static final String[] ALL_REL_CNAME_ORDER = {"XC","ZD","ZM","ZN","ZP","ZT","FO","NA","PR#5","AT#5","AN#5"};
	
	public static String[] getFeatOrder(String featOrderName) {
		if(featOrderName.equals(ALL_REL)) {
			return ALL_REL_ORDER;
		} else if(featOrderName.equals(ALL_REL_PNAME)) {
			return ALL_REL_PNAME_ORDER;
		} else if(featOrderName.equals(ALL_REL_CNAME)) {
			return ALL_REL_CNAME_ORDER;
		} else {
			return FULL;
		}
	}
}
