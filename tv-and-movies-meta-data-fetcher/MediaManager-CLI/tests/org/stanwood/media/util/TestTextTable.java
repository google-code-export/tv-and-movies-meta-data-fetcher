package org.stanwood.media.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Used to test the class {@link TextTable}
 */
@SuppressWarnings("nls")
public class TestTextTable {

	/**
	 * Test the table formatting
	 */
	@Test
	public void testTableFormat() {
		TextTable table = new TextTable(new String[] {"row1","Description","blah"});
		table.addRow(new String[] {"dfsdf","sgfghfghgfh","gdfgdfg"});
		table.addRow(new String[] {"hgfhgfh","fghgfhfgh","gfhjj"});
		table.addRow(new String[] {"","fghfghfgh","fgdfg"});
		table.addRow(new String[] {"","","fghfghfgh"});
		table.addRow(new String[] {"fghfghgfh","fghfghghf","gfhfgh"});

		StringBuilder actual = new StringBuilder();
		table.printTable(actual);

		StringBuilder expected = new StringBuilder();
		expected.append("row1      | Description | blah     \n");
		expected.append("===================================\n");
		expected.append("dfsdf     | sgfghfghgfh | gdfgdfg  \n");
		expected.append("hgfhgfh   | fghgfhfgh   | gfhjj    \n");
		expected.append("          | fghfghfgh   | fgdfg    \n");
		expected.append("          |             | fghfghfgh\n");
		expected.append("fghfghgfh | fghfghghf   | gfhfgh   \n");
		Assert.assertEquals(expected.toString(), actual.toString());
	}
}
