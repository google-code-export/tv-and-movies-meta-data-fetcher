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

@SuppressWarnings("nls")
public class TestNormalizeFilename {

	@Test
	public void testValidFileName() {
		Assert.assertEquals("blahfdgdhffgh",PatternMatcher.normalizeText("blahfdgdhffgh"));
	}

	@Test
	public void testNonAllowedInFilenames() {
		Assert.assertEquals("jkhjk--.sfdgg",PatternMatcher.normalizeText("jkhjk:/!sfdgg"));
	}

	@Test
	public void testAccents() {
		Assert.assertEquals("Finale",PatternMatcher.normalizeText("Finalé"));
		Assert.assertEquals("??? hello A",PatternMatcher.normalizeText("口水雞 hello Ä"));
		Assert.assertEquals("AAA?CE?e?o???",PatternMatcher.normalizeText("ÃÄÅÆÇÈØèæöø©®"));
	}

	@Test
	public void testInvalidPunucation() {
		Assert.assertEquals("????\"'Blah'\"",PatternMatcher.normalizeText("՚՛՜՝“‘Blah’”"));
	}
}
