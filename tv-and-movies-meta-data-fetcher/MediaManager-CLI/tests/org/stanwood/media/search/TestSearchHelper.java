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
package org.stanwood.media.search;

import org.junit.Assert;
import org.junit.Test;

/**
 * Used to test the class {@link SearchHelper}
 */
@SuppressWarnings("nls")
public class TestSearchHelper {

	/**
	 * Used to test that ignored tokens are removed correctly from search terms
	 */
	@Test
	public void testRemoveIgnoredTokens() {
		StringBuilder term = new StringBuilder("this is a test");
		SearchHelper.removeIgnoredTokens(term);
		Assert.assertEquals("this is a test",term.toString());

		term = new StringBuilder("this is a ac3 1080p test");
		SearchHelper.removeIgnoredTokens(term);
		Assert.assertEquals("this is a   test",term.toString());

		term = new StringBuilder("this is a AC3 test 1080P");
		SearchHelper.removeIgnoredTokens(term);
		Assert.assertEquals("this is a  test ",term.toString());
	}
}
