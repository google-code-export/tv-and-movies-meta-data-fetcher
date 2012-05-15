/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.info;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Used to test the class {@link ResolutionFormat}
 */
public class TestResolutionFormat {

	/**
	 * Used to check that the formations are detected correctly
	 */
	@Test
	public void testFormats() {
		 ResolutionFormat format = ResolutionFormat.getFormat(1280, 720, false);
		 Assert.assertEquals(format, ResolutionFormat.Format_720p);

		 format = ResolutionFormat.getFormat(1280, 720, true);
		 Assert.assertEquals(format, ResolutionFormat.Format_720i);

		 format = ResolutionFormat.getFormat(640, 352, false);
		 Assert.assertEquals(format, ResolutionFormat.Format_480p);

		 format = ResolutionFormat.getFormat(1280, 542, false);
		 Assert.assertEquals(format, ResolutionFormat.Format_720p);
		 Assert.assertEquals("16:9",format.getRatio().getDescription()); //$NON-NLS-1$

		 format = ResolutionFormat.getFormat(640, 352, false);
		 Assert.assertEquals(format, ResolutionFormat.Format_480p);
	}
}
