package org.stanwood.media.actions;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.stanwood.media.actions.seendb.DatabaseSeenDatabase;
import org.stanwood.media.actions.seendb.FileSeenDatabase;
import org.stanwood.media.actions.seendb.ISeenDatabase;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.setup.DBResource;
import org.stanwood.media.setup.SchemaCheck;
import org.stanwood.media.util.FileHelper;

/**
 * This is used to test the class {@link FileSeenDatabase}
 */
@SuppressWarnings("nls")
public class TestDatabaseSeenDatabase {

	/**
	 * Test that files can be marked as seen and checked
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testDatabase() throws Exception {
		File mediaDir = FileHelper.createTmpDir("config");
		File dbFile = FileHelper.createTempFile("mediaInfo","db"); //$NON-NLS-1$
		DBResource resource = createDBResource(dbFile);

		try {
			ISeenDatabase db = new DatabaseSeenDatabase(resource);

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

			Assert.assertEquals(2,db.getEntries().size());
			Assert.assertEquals(2,((DatabaseSeenDatabase)db).numberOfEntries());
		}
		finally {
			FileHelper.delete(dbFile);
			FileHelper.delete(mediaDir);
		}
	}



	/**
	 * Used to test that reading and writing work correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testReadWrite() throws Exception {
		File mediaDir = FileHelper.createTmpDir("config");
		File dbFile = FileHelper.createTempFile("mediaInfo","db"); //$NON-NLS-1$
		DBResource resource = createDBResource(dbFile);
		try {
			ISeenDatabase db =  new DatabaseSeenDatabase(resource);

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

			db =  new DatabaseSeenDatabase(resource);
			Assert.assertTrue(db.isSeen(mediaDir, test1));
			Assert.assertTrue(db.isSeen(mediaDir, test2));
			Assert.assertFalse(db.isSeen(mediaDir, test3));
			Assert.assertFalse(db.isSeen(mediaDir, test4));

			db.read(new NullProgressMonitor());
			Assert.assertTrue(db.isSeen(mediaDir, test1));
			Assert.assertTrue(db.isSeen(mediaDir, test2));
			Assert.assertFalse(db.isSeen(mediaDir, test3));
			Assert.assertFalse(db.isSeen(mediaDir, test4));
		}
		finally {
			FileHelper.delete(mediaDir);
			FileHelper.delete(dbFile);
		}
	}

	private File createFile(File mediaDir, String name) throws IOException {
		File file = new File(mediaDir,name);
		if (!file.createNewFile() && !file.exists()) {
			throw new IOException("Unable to create file: " + file);
		}
		return file;
	}

	private DBResource createDBResource(File dbFile) {
		DBResource resource = new DBResource();
		resource.setDialect("org.hibernate.dialect.HSQLDialect"); //$NON-NLS-1$
		resource.setUsername("sa"); //$NON-NLS-1$
		resource.setPassword(""); //$NON-NLS-1$
		resource.setUrl("jdbc:hsqldb:file:"+dbFile.getAbsolutePath()); //$NON-NLS-1$
		resource.setResourceId("file-"+dbFile.getAbsolutePath()); //$NON-NLS-1$
		resource.setSchemaCheck(SchemaCheck.UPDATE);
		return resource;
	}

}