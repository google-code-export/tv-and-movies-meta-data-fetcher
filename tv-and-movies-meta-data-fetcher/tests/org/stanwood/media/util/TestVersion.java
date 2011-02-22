package org.stanwood.media.util;

import junit.framework.Assert;

import org.junit.Test;

public class TestVersion {

	@Test
	public void testCompareVersion() {
		Assert.assertEquals(0,compare("52", "52"));
		Assert.assertEquals(-3,compare("52", "55"));
		Assert.assertEquals(2,compare("52", "34"));
		Assert.assertEquals(0,compare("1.0", "1.0"));
		Assert.assertEquals(-1,compare("1.0", "1.1"));
		Assert.assertEquals(0,compare("1.0.1", "1.0.1"));
		Assert.assertEquals(-1,compare("1.0.1", "1.1"));
		Assert.assertEquals(-17,compare("1.9", "1.10"));
		Assert.assertEquals(40,compare("1.a", "1.9"));
	}

	private int compare(String s1, String s2) {
		Version v1 = new Version(s1);
		Version v2 = new Version(s2);
		return v1.compareTo(v2);

	}
}
