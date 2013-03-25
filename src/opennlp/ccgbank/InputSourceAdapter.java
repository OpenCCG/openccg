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

/*
 * $Id: InputSourceAdapter.java,v 1.1 2009/11/09 19:21:50 mwhite14850 Exp $
 * Copyright (C) 2009 Scott Martin (http://www.coffeeblack.org/contact/)
 */
package opennlp.ccgbank;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;


/**
 * Turns an {@link InputSource} into a {@link StreamSource}. This class wraps
 * an input source for XSLT transformation routines that expect {@link Source}
 * objects.
 * @author <a href="http://www.ling.osu.edu/~scott/">Scott Martin</a>
 * @version $Revision: 1.1 $
 */
class InputSourceAdapter extends StreamSource {
	InputSource inputSource;
	
	InputSourceAdapter(InputSource inputSource) {
		this.inputSource = inputSource;
	}

	/**
	 * @return
	 * @see org.xml.sax.InputSource#getPublicId()
	 */
	@Override
	public String getPublicId() {
		return inputSource.getPublicId();
	}

	/**
	 * @return
	 * @see org.xml.sax.InputSource#getSystemId()
	 */
	@Override
	public String getSystemId() {
		return inputSource.getSystemId();
	}

	/**
	 * @param publicId
	 * @see org.xml.sax.InputSource#setPublicId(java.lang.String)
	 */
	@Override
	public void setPublicId(String publicId) {
		inputSource.setPublicId(publicId);
	}

	/**
	 * @param systemId
	 * @see org.xml.sax.InputSource#setSystemId(java.lang.String)
	 */
	@Override
	public void setSystemId(String systemId) {
		inputSource.setSystemId(systemId);
	}

	/* (non-Javadoc)
	 * @see javax.xml.transform.stream.StreamSource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return inputSource.getByteStream();
	}

	/* (non-Javadoc)
	 * @see javax.xml.transform.stream.StreamSource#getReader()
	 */
	@Override
	public Reader getReader() {
		return inputSource.getCharacterStream();
	}

	/* (non-Javadoc)
	 * @see javax.xml.transform.stream.StreamSource#setInputStream(java.io.InputStream)
	 */
	@Override
	public void setInputStream(InputStream inputStream) {
		inputSource.setByteStream(inputStream);
	}

	/* (non-Javadoc)
	 * @see javax.xml.transform.stream.StreamSource#setReader(java.io.Reader)
	 */
	@Override
	public void setReader(Reader reader) {
		inputSource.setCharacterStream(reader);
	}

	/* (non-Javadoc)
	 * @see javax.xml.transform.stream.StreamSource#setSystemId(java.io.File)
	 */
	@Override
	public void setSystemId(File f) {
		super.setSystemId(f);
		inputSource.setSystemId(super.getSystemId());
	}
	
}
