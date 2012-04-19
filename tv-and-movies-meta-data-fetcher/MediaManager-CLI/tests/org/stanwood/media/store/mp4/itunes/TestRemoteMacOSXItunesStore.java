package org.stanwood.media.store.mp4.itunes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.testdata.Data;
import org.stanwood.media.testdata.EpisodeData;
import org.stanwood.media.util.FileHelper;

/**
 * This is used to test the {@link RemoteMacOSXItunesStore} with a dummy server
 */
@SuppressWarnings("nls")
public class TestRemoteMacOSXItunesStore extends BaseRemoteMacOSXItunesStoreTest {

	private File nativeDir = null;

	/**
	 * Used to setup the nativeDir used to find native apps and libs
	 */
	public TestRemoteMacOSXItunesStore() {
		if (System.getProperty("NATIVE_DIR")!=null) {
			nativeDir = new File(System.getProperty("NATIVE_DIR"));
		}
	}

	/**
	 * Used to reset the command log before each test
	 * @throws ScriptException Thrown if their is a problem
	 */
	@Before
	public void setup() throws ScriptException {
		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		resetCommandLog();
		dropTables();
	}

	private File createMP4File(File file) throws IOException {
		FileHelper.copy(Data.class.getResourceAsStream("a_video.mp4"),file);
		return file;
	}

	/**
	 * Used to test that tv shows are correctly inserted and removed from the ruby itunes store
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testEpisode() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		File rawMediaDir = FileHelper.createTmpDir("media");
		try {
			Controller controller = createController(rawMediaDir, Mode.TV_SHOW, "%s %e - %t.%x");
			MediaDirectory mediaDir = controller.getMediaDirectory(rawMediaDir);
			RemoteMacOSXItunesStore store = (RemoteMacOSXItunesStore) controller.getStoreInfo(RemoteMacOSXItunesStore.class.getName()).getExtension(mediaDir.getMediaDirConfig(),0);
			store.init(controller,nativeDir);

			File eurekaDir = new File(rawMediaDir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			File heroesDir = new File(rawMediaDir, "Heroes");
			if (!heroesDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			List<EpisodeData> epsiodes = Data.createEurekaShow(eurekaDir);
			epsiodes.addAll(Data.createHeroesShow(heroesDir));

			for (EpisodeData ed : epsiodes) {
				createMP4File(ed.getFile());
				FileHelper.copy(Data.class.getResourceAsStream("a_video.mp4"),ed.getFile());
				File episodeFile = ed.getFile();
				IEpisode episode = ed.getEpisode();
				ISeason season =  episode.getSeason();
				IShow show=  season.getShow();

				store.cacheShow(rawMediaDir, episodeFile, show);
				store.cacheSeason(rawMediaDir, episodeFile, season);
				store.cacheEpisode(rawMediaDir, episodeFile, episode);
				System.out.println("Cache episode: " + episodeFile);
			}

			File f1=createMP4File(new File(heroesDir,"blah.m4v"));
			File f2=createMP4File(new File(eurekaDir,"1x01 - blah"));
			forceAddTrack(f1.getAbsolutePath(), 123, "Blah 123");
			forceAddTrack(f2.getAbsolutePath(), 124, "Blah 124");
			recacheTracks();
			store.fileDeleted(mediaDir, f1);
			store.renamedFile(rawMediaDir, f2, createMP4File(new File(eurekaDir,"1x01 - Renamed")));
			store.performedActions(mediaDir);

			List<String> commandLog = getCommandLog();
			Assert.assertEquals(9,commandLog.size());
			int msgIndex = 0;
			Assert.assertEquals("getTrackCount() = 2",commandLog.get(msgIndex++));
			Assert.assertEquals("getTracks()",commandLog.get(msgIndex++));
			Assert.assertEquals("removeTracksFromLibrary(Location: '"+rawMediaDir.getAbsolutePath()+"/Heroes/blah.m4v' - Database ID: 123 - Name: 'Blah 123' )",commandLog.get(msgIndex++));
			Assert.assertEquals("removeTracksFromLibrary(Location: '"+rawMediaDir.getAbsolutePath()+"/Eureka/1x01 - blah' - Database ID: 124 - Name: 'Blah 124' )",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary("+rawMediaDir.getAbsolutePath()+"/Eureka/1x02 - blah)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary("+rawMediaDir.getAbsolutePath()+"/Eureka/2x13 - blah)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary("+rawMediaDir.getAbsolutePath()+"/Eureka/000 - blah)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary("+rawMediaDir.getAbsolutePath()+"/Heroes/1x01 - hero)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary("+rawMediaDir.getAbsolutePath()+"/Eureka/1x01 - Renamed)",commandLog.get(msgIndex++));
		}
		finally {
			FileHelper.delete(rawMediaDir);
		}
	}

	/**
	 * Used to test that tv shows are correctly inserted and removed from the ruby itunes store
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testFilePaths() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		File rawMediaDir = FileHelper.createTmpDir("media");
		try {
			Controller controller = createController(rawMediaDir, Mode.TV_SHOW, "%s %e - %t.%x");
			MediaDirectory mediaDir = controller.getMediaDirectory(rawMediaDir);
			RemoteMacOSXItunesStore store = (RemoteMacOSXItunesStore) controller.getStoreInfo(RemoteMacOSXItunesStore.class.getName()).getExtension(mediaDir.getMediaDirConfig(),0);
			store.setParameter("search-pattern", ".*"+rawMediaDir.getName());
			store.setParameter("search-replace", "/media-blah");
			store.setParameter("file-separator", ".");
			store.init(controller,nativeDir);

			File eurekaDir = new File(rawMediaDir, "Eureka");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			File heroesDir = new File(rawMediaDir, "Heroes");
			if (!heroesDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir);
			}

			List<EpisodeData> epsiodes = Data.createEurekaShow(eurekaDir);
			epsiodes.addAll(Data.createHeroesShow(heroesDir));

			for (EpisodeData ed : epsiodes) {
				createMP4File(ed.getFile());
				FileHelper.copy(Data.class.getResourceAsStream("a_video.mp4"),ed.getFile());
				File episodeFile = ed.getFile();
				IEpisode episode = ed.getEpisode();
				ISeason season =  episode.getSeason();
				IShow show=  season.getShow();

				store.cacheShow(rawMediaDir, episodeFile, show);
				store.cacheSeason(rawMediaDir, episodeFile, season);
				store.cacheEpisode(rawMediaDir, episodeFile, episode);
			}

			recacheTracks();
			store.performedActions(mediaDir);

			List<String> commandLog = getCommandLog();
			System.out.println("------------------");
			for (String s : commandLog) {
				System.out.println(s);
			}
			System.out.println("------------------");
			Assert.assertEquals(7,commandLog.size());
			int msgIndex = 0;
			Assert.assertEquals("getTrackCount() = 0",commandLog.get(msgIndex++));
			Assert.assertEquals("getTracks()",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary(.media-blah.Eureka.1x01 - blah)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary(.media-blah.Eureka.1x02 - blah)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary(.media-blah.Eureka.2x13 - blah)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary(.media-blah.Eureka.000 - blah)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary(.media-blah.Heroes.1x01 - hero)",commandLog.get(msgIndex++));

		}
		finally {
			FileHelper.delete(rawMediaDir);
		}
	}

	/**
	 * Used to test that films are correctly inserted and removed from the ruby itunes store
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testFilm() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		File rawMediaDir = FileHelper.createTmpDir("media");
		try {
			Controller controller = createController(rawMediaDir, Mode.TV_SHOW, "%s %e - %t.%x");
			MediaDirectory mediaDir = controller.getMediaDirectory(rawMediaDir);
			RemoteMacOSXItunesStore store = (RemoteMacOSXItunesStore) controller.getStoreInfo(RemoteMacOSXItunesStore.class.getName()).getExtension(mediaDir.getMediaDirConfig(),0);
			store.init(controller,nativeDir);

			File filmFile1 = new File(rawMediaDir,"The Usual Suspects part1.avi");
			FileHelper.copy(Data.class.getResourceAsStream("a_video.mp4"),filmFile1);

			File filmFile2 = new File(rawMediaDir,"The Usual Suspects part2.avi");
			FileHelper.copy(Data.class.getResourceAsStream("a_video.mp4"),filmFile2);

			Film film = Data.createFilm();

			store.cacheFilm(rawMediaDir, filmFile1, film,1);
			store.cacheFilm(rawMediaDir, filmFile2, film,2);
			store.fileUpdated(mediaDir, new File(rawMediaDir,"The Usual Suspects part1.avi"));
			store.performedActions(mediaDir);

			List<String> commandLog = getCommandLog();
			Assert.assertEquals(3,commandLog.size());
			int msgIndex=0;
			Assert.assertEquals("addFilesToLibrary("+rawMediaDir.getAbsolutePath()+"/The Usual Suspects part1.avi)",commandLog.get(msgIndex++));
			Assert.assertEquals("addFilesToLibrary("+rawMediaDir.getAbsolutePath()+"/The Usual Suspects part2.avi)",commandLog.get(msgIndex++));
			Assert.assertEquals("refreshTracks(Location: '"+rawMediaDir.getAbsolutePath()+"/The Usual Suspects part1.avi' - Database ID: 0 - Name: 'Test 0' )",commandLog.get(msgIndex++));
		}
		finally {
			FileHelper.delete(rawMediaDir);
		}
	}

	private Controller createController(File mediaDir,Mode mode,String pattern) throws Exception {
		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<mediaManager>"+FileHelper.LS);
		testConfig.append("  <plugins>"+FileHelper.LS);
		testConfig.append("    <plugin class=\""+RemoteMacOSXItunesStoreInfo.class.getName()+"\"/>"+FileHelper.LS);
		testConfig.append("  </plugins>"+FileHelper.LS);
		testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\""+mode+"\" pattern=\""+pattern+"\"  >"+FileHelper.LS);
		testConfig.append("    <stores>"+FileHelper.LS);
		testConfig.append("	     <store id=\""+RemoteMacOSXItunesStore.class.getName()+"\">"+FileHelper.LS);
		testConfig.append("         <param name=\"hostname\" value=\"localhost\"/>"+FileHelper.LS);
		testConfig.append("         <param name=\"port\" value=\""+getPort()+"\"/>"+FileHelper.LS);
		testConfig.append("         <param name=\"username\" value=\""+USER+"\"/>"+FileHelper.LS);
		testConfig.append("         <param name=\"password\" value=\""+PASSWORD+"\"/>"+FileHelper.LS);
		testConfig.append("      </store>"+FileHelper.LS);
		testConfig.append("    </stores>"+FileHelper.LS);
		testConfig.append("  </mediaDirectory>"+FileHelper.LS);
		testConfig.append("</mediaManager>"+FileHelper.LS);

		File configFile = FileHelper.createTmpFileWithContents(testConfig);
		InputStream is = null;
		try {
			is = new FileInputStream(configFile);
			ConfigReader configReader = new ConfigReader(is);
			configReader.parse();
			Controller controller = new Controller(configReader);
			controller.init(false);
			controller.getMediaDirectory(mediaDir);
			return controller;
		}
		finally {
			if (is!=null) {
				is.close();
			}

		}
	}
}
