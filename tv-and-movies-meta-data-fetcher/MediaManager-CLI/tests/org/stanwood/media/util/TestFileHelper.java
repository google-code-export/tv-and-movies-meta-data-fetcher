package org.stanwood.media.util;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * Used to test the class {@link FileHelper}
 */
@SuppressWarnings("nls")
public class TestFileHelper {

	/**
	 * Used to test that relative paths are converted correctly
	 */
	@Test
	public void testResolveRelativePaths() {
		File f1= new File(File.separator+"this"+File.separator+"is"+File.separator+"a"+File.separator+"test.txt");
		File f2= new File(File.separator+"this"+File.separator+"is"+File.separator+"a"+File.separator+"test.txt");

		Assert.assertEquals(f2.getAbsolutePath(),FileHelper.resolveRelativePaths(f1).getAbsolutePath());

		f1= new File(File.separator+"this"+File.separator+"is"+File.separator+".."+File.separator+"a"+File.separator+"test.txt");
		f2= new File(File.separator+"this"+File.separator+"a"+File.separator+"test.txt");
		Assert.assertEquals(f2.getAbsolutePath(),FileHelper.resolveRelativePaths(f1).getAbsolutePath());
	}
}
