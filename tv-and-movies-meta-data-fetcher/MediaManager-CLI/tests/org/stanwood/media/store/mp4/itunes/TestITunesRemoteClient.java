package org.stanwood.media.store.mp4.itunes;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.List;

import javax.script.ScriptException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.util.FileHelper;

/**
 * This is used to test the java client that connects to the ruby server used
 * to control itunes. The server is a dummy server that just logs the commands instead
 * of talking to itunes
 */
@SuppressWarnings("nls")
public class TestITunesRemoteClient extends BaseRemoteMacOSXItunesStoreTest {

	private ITunesRemoteClient client;

	/**
	 * Create the client
	 * @throws ScriptException
	 */
	@Before
	public void connectClient() throws ScriptException {
		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		resetCommandLog();
		dropTables();
		client = new ITunesRemoteClient();
	}

	/**
	 * Used to destory the client
	 */
	@After
	public void tearDown() {
		client.disconnect();
	}

	/**
	 * Client up the client
	 * @throws StoreException Trown if their is a problem
	 */
	@After
	public void disconenctClient() throws StoreException {
		client.disconnect();
		client = null;
	}

	/**
	 * Used to test connecting to the server
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testConnect() throws Exception {
		System.out.println("---- testConnect() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		client.sendCommand(ITunesRemoteClient.CMD_HELO, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);

		Assert.assertEquals(0,getCommandLog().size());
		System.out.println("---- testConnect() - done");
	}

	/**
	 * Used to test that we can login to the server
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testLogin() throws Exception {
		System.out.println("---- testLogin() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		client.login(USER , PASSWORD);

		Assert.assertEquals(0,getCommandLog().size());
		System.out.println("---- testLogin() - done");
	}

	/**
	 * Used to test that the user can't log on if the password is wrong
	 * @throws Throwable Thrown if their are any problems
	 */
	@Test(timeout=10000)
	public void testInvalidPassword() throws Throwable {
		System.out.println("---- testInvalidPassword() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		try {
			client.login(USER , "blah1");
			Assert.fail("Exception not detected");
		}
		catch (StoreException e ) {

		}
		Assert.assertEquals(0,getCommandLog().size());
		client.disconnect();
//		tearDownServer();
//		setupServer();
		System.out.println("---- testInvalidPassword() - done");
	}

	/**
	 * Used to test that the user can't log on if the user name is wrong
	 * @throws Throwable Thrown if their are any problems
	 */
	@Test(timeout=10000)
	public void testInvalidUsername() throws Throwable {
		System.out.println("---- testInvalidUsername() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		try {
			client.login("blah" , PASSWORD);
			Assert.fail("Exception not detected");
		}
		catch (StoreException e ) {

		}
		Assert.assertEquals(0,getCommandLog().size());
		client.disconnect();
//		tearDownServer();
//		setupServer();
		System.out.println("---- testInvalidUsername() - done");
	}


	/**
	 * Test commands that should not work when not logged in
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testCommandsDontWorkWhenNotLoggedIn() throws Exception {
		System.out.println("---- testCommandsDontWorkWhenNotLoggedIn() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		client.sendCommand(ITunesRemoteClient.CMD_REMOVE_DEAD_FILES, 500,ITunesRemoteClient.DEFAULT_TIMEOUT);

		Assert.assertEquals(0,getCommandLog().size());
		System.out.println("---- testCommandsDontWorkWhenNotLoggedIn() - done");
	}

	/**
	 * Used to test the user can quit when not logged in
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testQuitNotLoggedIn() throws Exception {
		System.out.println("---- testQuitNotLoggedIn() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		client.sendCommand(ITunesRemoteClient.CMD_QUIT, 221,ITunesRemoteClient.DEFAULT_TIMEOUT);
		System.out.println("---- testQuitNotLoggedIn() - done");
	}

	/**
	 * Used to test the user can quit when not logged in
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testQuitLoggedIn() throws Exception {
		System.out.println("---- testQuitLoggedIn() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		client.login(USER , PASSWORD);
		client.sendCommand(ITunesRemoteClient.CMD_QUIT, 221,ITunesRemoteClient.DEFAULT_TIMEOUT);
		System.out.println("---- testQuitLoggedIn() - done");
	}

	/**
	 * Used to test that adding files works correctly
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testAddFiles() throws Exception {
		System.out.println("---- testAddFiles() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		client.login(USER , PASSWORD);
		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah1", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah/blah2", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_ADD_FILES, 220,ITunesRemoteClient.NO_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_HELO, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);

		List<String> commandLog = getCommandLog();
		Assert.assertEquals(3,commandLog.size());
		Assert.assertEquals("addFilesToLibrary(/blah)",commandLog.get(0));
		Assert.assertEquals("addFilesToLibrary(/blah1)",commandLog.get(1));
		Assert.assertEquals("addFilesToLibrary(/blah/blah2)",commandLog.get(2));
		System.out.println("---- testAddFiles() - done");

		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah1", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah/blah2", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_REMOVE_FILES, 220,ITunesRemoteClient.NO_TIMEOUT);
	}

	private File[] createTestFiles(File rootDir) throws IOException {
		File file1 = new File(rootDir,"blah.m4v");
		if (!file1.createNewFile() && !file1.exists()) {
			throw new IOException("Unable to create file: " + file1);
		}
		File file2 = new File(rootDir,"blah1");
		if (!file2.createNewFile() && !file2.exists()) {
			throw new IOException("Unable to create file: " + file2);
		}
		File file3 = new File(rootDir,"blah"+File.separator+"blah2");
		if (!file3.getParentFile().mkdir() && !file3.getParentFile().exists()) {
			throw new IOException("Unable to create directory: " + file3.getParentFile());
		}
		if (!file3.createNewFile() && !file3.exists()) {
			throw new IOException("Unable to create file: " + file3);
		}
		return new File[]{file1,file2,file3};
	}


	/**
	 * Used to test that files can be removed
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testRemoveFiles() throws Exception {
		System.out.println("---- testRemoveFiles() - start");
		File testDir = FileHelper.createTmpDir("test");
		try {

			client.connect(Inet4Address.getByName("localhost"), getPort());
			client.login(USER , PASSWORD);
			File[] files = createTestFiles(testDir);
			for (int i=0;i<files.length;i++) {
				forceAddTrack(files[i].getAbsolutePath(),i,"Test "+i);
			}
			recacheTracks();
			for (int i=0;i<files.length;i++) {
				client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+files[i].getAbsolutePath(), 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
			}
			client.sendCommand(ITunesRemoteClient.CMD_REMOVE_FILES, 220,ITunesRemoteClient.NO_TIMEOUT);
			client.sendCommand(ITunesRemoteClient.CMD_HELO, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);

			List<String> commandLog = getCommandLog();
			System.out.println("------------------------");
			for (String s : commandLog) {
				System.out.println(s);
			}
			System.out.println("------------------------");
			Assert.assertEquals(5,commandLog.size());
			int index=0;
			Assert.assertEquals("getTrackCount() = 3",commandLog.get(index++));
			Assert.assertEquals("getTracks()",commandLog.get(index++));
			for (int i=0;i<files.length;i++) {
				Assert.assertEquals("removeTracksFromLibrary(Location: '"+files[i]+"' - Database ID: "+i+" - Name: 'Test "+i+"' )",commandLog.get(index++));
			}
		}
		finally {
			FileHelper.delete(testDir);
		}
		System.out.println("---- testRemoveFiles() - done");

	}

	/**
	 * Used to test that files can be removed
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testRefreshFiles() throws Exception {
		System.out.println("---- testRefreshFiles() - start");
		File testDir = FileHelper.createTmpDir("test");
		try {

			client.connect(Inet4Address.getByName("localhost"), getPort());
			client.login(USER , PASSWORD);
			File[] files = createTestFiles(testDir);
			for (int i=0;i<files.length;i++) {
				forceAddTrack(files[i].getAbsolutePath(),i,"Test "+i);
			}
			recacheTracks();
			for (int i=0;i<files.length;i++) {
				client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+files[i].getAbsolutePath(), 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
			}
			client.sendCommand(ITunesRemoteClient.CMD_REFRESH_FILES, 220,ITunesRemoteClient.NO_TIMEOUT);
			client.sendCommand(ITunesRemoteClient.CMD_HELO, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);

			List<String> commandLog = getCommandLog();
			for (String s : commandLog) {
				System.out.println(s);
			}
			Assert.assertEquals(5,commandLog.size());
			int index=0;
			Assert.assertEquals("getTrackCount() = 3",commandLog.get(index++));
			Assert.assertEquals("getTracks()",commandLog.get(index++));
			for (int i=0;i<files.length;i++) {
				Assert.assertEquals("refreshTracks(Location: '"+files[i]+"' - Database ID: "+i+" - Name: 'Test "+i+"' )",commandLog.get(index++));
			}

			for (int i=0;i<files.length;i++) {
				client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+files[i].getAbsolutePath(), 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
			}
			client.sendCommand(ITunesRemoteClient.CMD_REMOVE_FILES, 220,ITunesRemoteClient.NO_TIMEOUT);
		}
		finally {
			FileHelper.delete(testDir);
		}
		System.out.println("---- testRefreshFiles() - done");
	}

	/**
	 * Used to test that the pending files can be cleared
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testClearFiles1() throws Exception {
		System.out.println("---- testClearFiles1() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		client.login(USER , PASSWORD);
		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah1", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_FILE+":/blah/blah2", 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_CLEAR_FILES, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_HELO, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);

		List<String> commandLog = getCommandLog();
		Assert.assertEquals(0,commandLog.size());
		System.out.println("---- testClearFiles1() - done");
	}

	/**
	 * Used to test that the pending files can be cleared when none are registered
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testClearFiles2() throws Exception {
		System.out.println("---- testClearFiles2() - start");
		client.connect(Inet4Address.getByName("localhost"), getPort());
		client.login(USER , PASSWORD);
		client.sendCommand(ITunesRemoteClient.CMD_CLEAR_FILES, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
		client.sendCommand(ITunesRemoteClient.CMD_HELO, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);

		List<String> commandLog = getCommandLog();
		Assert.assertEquals(0,commandLog.size());
		System.out.println("---- testClearFiles2() - done");
	}

	/**
	 * Used to test that dead files can be removed
	 * @throws Exception Thrown if their is a problem
	 */
	@Test(timeout=10000)
	public void testRemoveDeadFiles() throws Exception {
		System.out.println("---- testRemoveDeadFiles() - start");
		File testDir = FileHelper.createTmpDir("test");
		try {

			client.connect(Inet4Address.getByName("localhost"), getPort());
			client.login(USER , PASSWORD);
			File[] files = createTestFiles(testDir);
			for (int i=0;i<files.length;i++) {
				forceAddTrack(files[i].getAbsolutePath(),i,"Test "+i);
			}
			if (!files[0].delete() && files[0].exists()) {
				throw new IOException("Unable to delete file: " + files[0]);
			}
			recacheTracks();
			for (int i=0;i<files.length;i++) {
				client.sendCommand(ITunesRemoteClient.CMD_FILE+":"+files[i].getAbsolutePath(), 220,ITunesRemoteClient.DEFAULT_TIMEOUT);
			}
			client.sendCommand(ITunesRemoteClient.CMD_REMOVE_DEAD_FILES, 220,ITunesRemoteClient.NO_TIMEOUT);
			client.sendCommand(ITunesRemoteClient.CMD_HELO, 220,ITunesRemoteClient.DEFAULT_TIMEOUT);

			List<String> commandLog = getCommandLog();
			System.out.println("------------------------");
			for (String s : commandLog) {
				System.out.println(s);
			}
			System.out.println("------------------------");
			Assert.assertEquals(3,commandLog.size());
			int index=0;
			Assert.assertEquals("getTrackCount() = 3",commandLog.get(index++));
			Assert.assertEquals("getTracks()",commandLog.get(index++));
			Assert.assertEquals("removeTracksFromLibrary(Location: '"+files[0]+"' - Database ID: "+0+" - Name: 'Test "+0+"' )",commandLog.get(index++));
		}
		finally {
			FileHelper.delete(testDir);
		}
		System.out.println("---- testRemoveDeadFiles() - done");
	}
}
