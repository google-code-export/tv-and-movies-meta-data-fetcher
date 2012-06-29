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
package org.stanwood.media.actions.rename;

import org.junit.Assert;
import org.junit.Test;

/**
 * Used to test that file names are correctly normalised
  */
@SuppressWarnings("nls")
public class TestNormalizeFilename {

	/**
	 * Test filenames that don't need to be changed
	 */
	@Test
	public void testValidFileName() {
		Assert.assertEquals("blahfdgdhffgh",PatternMatcher.normalizeText("blahfdgdhffgh"));
	}

	/**
	 * Test filenames with characters that are not allowed in filenames
	 */
	@Test
	public void testNonAllowedInFilenames() {
		Assert.assertEquals("jkhjk--.sfdgg",PatternMatcher.normalizeText("jkhjk:/!sfdgg"));
	}

	/**
	 * Test that accented characters get replaced
	 */
	@Test
	public void testAccents() {
		Assert.assertEquals("Finale",PatternMatcher.normalizeText("Finalé"));
		Assert.assertEquals("hello A",PatternMatcher.normalizeText("口水雞 hello Ä"));
		Assert.assertEquals("AAACEeo",PatternMatcher.normalizeText("ÃÄÅÆÇÈØèæöø©®"));
	}

	/**
	 * Test that invalid punctuation is replaced
	 */
	@Test
	public void testInvalidPunctuation() {
		Assert.assertEquals("''Blah''",PatternMatcher.normalizeText("՚՛՜՝“‘Blah’”"));
	}
}
