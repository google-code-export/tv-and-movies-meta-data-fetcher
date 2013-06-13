/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.store.mp4.jaudiotagger;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jaudiotagger.audio.mp4.atom.Mp4BoxHeader;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.mp4.atom.Mp4DataBox;
import org.jaudiotagger.tag.mp4.field.Mp4DiscNoField;

public class JATMp4DiscNoField extends Mp4DiscNoField{

	private static final int NONE_VALUE_INDEX = 0;
    private static final int DISC_NO_INDEX = 1;
    private static final int DISC_TOTAL_INDEX = 2;
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("(\\d+)/(\\d+)");

	public JATMp4DiscNoField(int discNo) {
		super(discNo);
	}

	public JATMp4DiscNoField(int discNo, int total) {
		super(discNo, total);
		// TODO Auto-generated constructor stub
	}

	public JATMp4DiscNoField(String id, ByteBuffer data)
			throws UnsupportedEncodingException {
		super(id, data);
		// TODO Auto-generated constructor stub
	}

	public JATMp4DiscNoField(String discValue) throws FieldDataInvalidException {
		super(discValue);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void build(ByteBuffer data) throws UnsupportedEncodingException
    {
        //Data actually contains a 'Data' Box so process data using this
        Mp4BoxHeader header = new Mp4BoxHeader(data);
        Mp4DataBox databox = new Mp4DataBox(header, data);
        dataSize = header.getDataLength();
        numbers = databox.getNumbers();
        if (numbers == null) {
        	Matcher m = NUMBERS_PATTERN.matcher(databox.getContent());
        	if (m.matches()) {
        		numbers = new ArrayList<Short>();
                numbers.add(new Short("0")); //$NON-NLS-1$
                numbers.add(Short.parseShort(m.group(1)));
                numbers.add(Short.parseShort(m.group(2)));
        	}
        }

        //Disc number always hold four values, we can discard the first one and last one, the second one is the disc no
        //and the third is the total no of discs so only use if not zero
        StringBuffer sb = new StringBuffer();
        if ((numbers.size() > DISC_NO_INDEX) && (numbers.get(DISC_NO_INDEX) > 0))
        {
            sb.append(numbers.get(DISC_NO_INDEX));
        }
        if ((numbers.size() > DISC_TOTAL_INDEX) && (numbers.get(DISC_TOTAL_INDEX) > 0))
        {
            sb.append("/").append(numbers.get(DISC_TOTAL_INDEX));
        }
        content = sb.toString();
    }
}
