package org.stanwood.media.actions;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.actions.seendb.FileSeenDatabase;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.util.FileHelper;

/**
 * This is used to test the class {@link FileSeenDatabase}
 */
@SuppressWarnings("nls")
public class TestSeenDatabase {

	/**
	 * Test that files can be marked as seen and checked
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testDatabase() throws Exception {
		File configDir = FileHelper.createTmpDir("config");
		File mediaDir = FileHelper.createTmpDir("config");
		try {
			FileSeenDatabase db = new FileSeenDatabase(configDir);

			db.markAsSeen(mediaDir,createFile(mediaDir,"test1.avi"));
			File test2 = createFile(mediaDir,"test2.avi");
			db.markAsSeen(mediaDir,test2);
			createFile(mediaDir,"test3.avi");
			createFile(mediaDir,"test4.avi");

			Assert.assertTrue(db.isSeen(mediaDir, new File(mediaDir,"test1.avi")));
			Assert.assertTrue(db.isSeen(mediaDir, test2));
			Assert.assertFalse(db.isSeen(mediaDir, new File(mediaDir,"test3.avi")));
			Assert.assertFalse(db.isSeen(mediaDir, new File(mediaDir,"test4.avi")));

			long orgTime = test2.lastModified();
			if (!test2.setLastModified(orgTime+1000)) {
				throw new IOException("Unable to set time");
			}
			Assert.assertTrue(orgTime!=test2.lastModified());

			Assert.assertTrue(db.isSeen(mediaDir, new File(mediaDir,"test1.avi")));
			Assert.assertFalse(db.isSeen(mediaDir, test2));
			Assert.assertFalse(db.isSeen(mediaDir, new File(mediaDir,"test3.avi")));
			Assert.assertFalse(db.isSeen(mediaDir, new File(mediaDir,"test4.avi")));
		}
		finally {
			FileHelper.delete(configDir);
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to test that reading and writing work correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testReadWrite() throws Exception {
		File configDir = FileHelper.createTmpDir("config");
		File mediaDir = FileHelper.createTmpDir("config");
		try {
			FileSeenDatabase db = new FileSeenDatabase(configDir);

			File test1 = createFile(mediaDir,"test1.avi");
			db.markAsSeen(mediaDir,test1);
			File test2 = createFile(mediaDir,"test2.avi");
			File test3 = createFile(mediaDir,"test3.avi");
			File test4 =createFile(mediaDir,"test4.avi");

			Assert.assertTrue(db.isSeen(mediaDir, new File(mediaDir,"test1.avi")));
			long orgTime = test2.lastModified();
			if (!test2.setLastModified(orgTime+1000)) {
				throw new IOException("Unable to set time");
			}
			db.markAsSeen(mediaDir,test2);

			Assert.assertTrue(orgTime!=test2.lastModified());
			db.write(new NullProgressMonitor());

			db = new FileSeenDatabase(configDir);
			Assert.assertFalse(db.isSeen(mediaDir, test1));
			Assert.assertFalse(db.isSeen(mediaDir, test2));
			Assert.assertFalse(db.isSeen(mediaDir, test3));
			Assert.assertFalse(db.isSeen(mediaDir, test4));

			db.read(new NullProgressMonitor());
			Assert.assertTrue(db.isSeen(mediaDir, test1));
			Assert.assertTrue(db.isSeen(mediaDir, test2));
			Assert.assertFalse(db.isSeen(mediaDir, test3));
			Assert.assertFalse(db.isSeen(mediaDir, test4));
		}
		finally {
			FileHelper.delete(configDir);
			FileHelper.delete(mediaDir);
		}
	}

	private File createFile(File mediaDir, String name) throws IOException {
		File file = new File(mediaDir,name);
		if (!file.createNewFile() && !file.exists()) {
			throw new IOException("Unable to create file: " + file);
		}
		return file;
	}


}
