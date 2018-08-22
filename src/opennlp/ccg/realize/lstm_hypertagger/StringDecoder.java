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

package opennlp.ccg.realize.lstm_hypertagger;

public class StringDecoder {
	public static String decode(String str) {
		str = str.replace("&amp;","&");
        str = str.replace("&gt;",">");
        str = str.replace("&lt;","<");
        str = str.replace("&apos;","\'");
        str = str.replace("&quot;","\"");
        str = str.replace("&#45;", "-");
        str = str.replace("&#58;", ":");
		return str;
	}
}
