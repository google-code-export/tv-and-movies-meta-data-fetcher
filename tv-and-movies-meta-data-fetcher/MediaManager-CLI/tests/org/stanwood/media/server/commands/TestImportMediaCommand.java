/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.server.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.manager.TestNFOFilms;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.Mode;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.LoggingStore;
import org.stanwood.media.store.LoggingStoreInfo;
import org.stanwood.media.util.FileHelper;

/**
 * This test is used to check the class {@link ImportMediaCommand}. It checks that
 * media is imported correctly.
 */
@SuppressWarnings("nls")
public class TestImportMediaCommand extends XBMCAddonTestBase {

	/**
	 * Test the user is told that their was no media to import when the watch dir is empty
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testNoMediaFiles() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");
		File showDir = FileHelper.createTmpDir("showDir");

		ConfigReader config = createTestConfig(watchDir,filmDir,showDir,null,null);
		Controller controller = new Controller(config);
		controller.init(false);

		ImportMediaCommand cmd = new ImportMediaCommand(controller);
		cmd.setUseDefaults(true);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();
		ICommandResult result = cmd.execute(logger, new NullProgressMonitor());

		Assert.assertTrue(logger.getResult().toString().contains("INFO:Unable to find any media files"));
		Assert.assertNull(result);
	}

	/**
	 * Test that the script events occur even it import media command detects and error
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testScriptEvents() throws Exception {
		LogSetupHelper.forceReset();
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		LogSetupHelper.initLogging(stdout,stderr);

		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");
		File showDir = FileHelper.createTmpDir("showDir");

		StringBuilder extraConfig = new StringBuilder();
		extraConfig.append("  <scripts>"+FileHelper.LS);
		extraConfig.append("    <file language=\"jruby\" location=\""+new File(TestImportMediaCommand.class.getResource("testScript.rb").toURI()).getAbsolutePath()+"\"/>"+FileHelper.LS);
		extraConfig.append("  </scripts>"+FileHelper.LS);

		ConfigReader config = createTestConfig(watchDir,filmDir,showDir,null,extraConfig.toString());
		Controller controller = new Controller(config);
		controller.init(false);

		ImportMediaCommand cmd = new ImportMediaCommand(controller);
		cmd.setUseDefaults(true);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();
		ICommandResult result = cmd.execute(logger, new NullProgressMonitor());

		Assert.assertTrue(logger.getResult().toString().contains("INFO:Unable to find any media files"));
		Assert.assertNull(result);
		System.out.println("-----");
		System.out.println(stdout);
		System.out.println("-----");
		Assert.assertTrue(stdout.toString().contains("onEventPreMediaImport("+watchDir.getAbsolutePath()+")"));
		Assert.assertTrue(stdout.toString().contains("onEventPostMediaImport("+watchDir.getAbsolutePath()+")"));
	}

	/**
	 * Test the media is imported into a empty directory. This does not execute the actions.
	 * So files should not be marked as seen untill the media dir is managed.
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testMediaImportIntoEmptyMediaDirsDontExecuteActions() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		LoggingStore.clearEvents();
		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");
		File showDir = FileHelper.createTmpDir("showDir");
		ConfigReader config = createTestConfig(watchDir,filmDir,showDir,LoggingStore.class.getName(),null);
		Controller controller = new Controller(config);
		controller.init(false);

		Assert.assertEquals(0,LoggingStore.getEvents().size());

		ImportMediaCommand cmd = new ImportMediaCommand(controller);
		cmd.setExecuteActions(false);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();

		File f = new File(watchDir,"Heroes S02E01 - Blah Blah Blah.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		f = new File(watchDir,"Heroes S01E01 - Blah Blah Blah.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		f = new File(watchDir,"iron.man.2009.dvdrip.xvid-amiable.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}

		ICommandResult result = cmd.execute(logger, new NullProgressMonitor());
		Assert.assertNotNull(result);

		List<String> files = FileHelper.listFilesAsStrings(watchDir);
		Collections.sort(files);
		Assert.assertEquals(0,files.size());

		files = FileHelper.listFilesAsStrings(showDir);
		Collections.sort(files);
		Assert.assertEquals(2,files.size());
		Assert.assertEquals(new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(showDir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - Four Months Later....avi").getAbsolutePath(),files.get(1));

		Assert.assertFalse(controller.getSeenDB().isSeen(showDir, new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi")));

		files = FileHelper.listFilesAsStrings(filmDir);
		Collections.sort(files);
		Assert.assertEquals(1,files.size());
		Queue<String> events = LoggingStore.getEvents();
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("upgrade()",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("upgrade()",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("searchMedia(Heroes,TV_SHOW,null,"+showDir.getAbsolutePath()+
				            ","+new File(watchDir,"Heroes S01E01 - Blah Blah Blah.avi")+") -> null",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("cacheShow()",events.remove());
		Assert.assertEquals("getSeason()",events.remove());
		Assert.assertEquals("cacheSeason()",events.remove());
		Assert.assertEquals("getEpisode()",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("searchMedia(Heroes,TV_SHOW,null,"+showDir.getAbsolutePath()+","+
						   new File(watchDir,"Heroes S02E01 - Blah Blah Blah.avi")+") -> 79501:org.stanwood.media.source.xbmc.XBMCSource#metadata.tvdb.com - (http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip) - (null)",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("getSeason()",events.remove());
		Assert.assertEquals("cacheSeason()",events.remove());
		Assert.assertEquals("getEpisode()",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("searchMedia(iron man,FILM,null,"+filmDir.getAbsolutePath()+"," +
				            new File(watchDir,"iron.man.2009.dvdrip.xvid-amiable.avi")+") -> null",events.remove());
		Assert.assertEquals("getFilm()",events.remove());
		Assert.assertEquals("cacheEpisode("+showDir.getAbsolutePath()+","+showDir.getAbsolutePath()+File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi)",events.remove());
		Assert.assertEquals("cacheEpisode("+showDir.getAbsolutePath()+","+showDir.getAbsolutePath()+File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - Four Months Later....avi)",events.remove());
		Assert.assertEquals("cacheFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"Iron Man (2008).avi)",events.remove());

		Assert.assertTrue(events.isEmpty());

		ExtensionInfo<? extends IStore> loggingStoreInfo = controller.getStoreInfo(LoggingStore.class.getName());

		MediaDirectory dir = controller.getMediaDirectory(filmDir);
		LoggingStore logginStore = (LoggingStore) loggingStoreInfo.getExtension(controller,dir.getMediaDirConfig(),0);
		IFilm film = logginStore.getFilm(dir, new File(filmDir,"Iron Man (2008).avi"));
		Assert.assertNotNull(film);
		Assert.assertEquals(1,film.getFiles().size());
		Assert.assertEquals(new File(filmDir,"Iron Man (2008).avi"),film.getFiles().get(0).getLocation());
		Assert.assertEquals(new File(watchDir,"iron.man.2009.dvdrip.xvid-amiable.avi"),film.getFiles().get(0).getOrginalLocation());

		dir = controller.getMediaDirectory(showDir);
		logginStore = (LoggingStore) loggingStoreInfo.getExtension(controller,dir.getMediaDirConfig(),0);
		IEpisode episode = logginStore.getEpisode(dir, new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi"));
		Assert.assertEquals(new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi").getAbsolutePath(),episode.getFiles().get(0).getLocation().getAbsolutePath());
		Assert.assertEquals(new File(watchDir,"Heroes S01E01 - Blah Blah Blah.avi").getAbsolutePath(),episode.getFiles().get(0).getOrginalLocation().getAbsolutePath());

		Assert.assertEquals(0,controller.getSeenDB().getEntries().size());

		ManageMediaCommand mmCommand = new ManageMediaCommand(controller);
		List<File>mediaDirs = new ArrayList<File>();
		mediaDirs.add(filmDir);
		mediaDirs.add(showDir);
		result = mmCommand.execute(logger, new NullProgressMonitor());
		Assert.assertNotNull(result);

		Assert.assertEquals("getFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"Iron Man (2008).avi)",events.remove());
		Assert.assertEquals("init()",events.remove());

		Assert.assertEquals("getFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"Iron Man (2008).avi)",events.remove());
		Assert.assertEquals("performedActions("+filmDir.getAbsolutePath()+")",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("performedActions("+showDir.getAbsolutePath()+")",events.remove());

		Assert.assertTrue(events.isEmpty());

		Assert.assertEquals(3,controller.getSeenDB().getEntries().size());
		Assert.assertTrue(controller.getSeenDB().isSeen(showDir, new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi")));
	}

	/**
	 * Used to test media can be imported into empty media dirs. After importing,
	 * check that managing the media does not effect it. This also tests the processes
	 * effect on stores.
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testMediaImportIntoEmptyMediaDirsExecuteActions() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		LoggingStore.clearEvents();
		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");
		File showDir = FileHelper.createTmpDir("showDir");
		ConfigReader config = createTestConfig(watchDir,filmDir,showDir,LoggingStore.class.getName(),null);
		Controller controller = new Controller(config);
		controller.init(false);

		Assert.assertEquals(0,LoggingStore.getEvents().size());

		ImportMediaCommand cmd = new ImportMediaCommand(controller);
		cmd.setExecuteActions(true);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();

		File f = new File(watchDir,"Heroes S02E01 - Blah Blah Blah.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		f = new File(watchDir,"Heroes S01E01 - Blah Blah Blah.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}
		f = new File(watchDir,"iron.man.2009.dvdrip.xvid-amiable.avi");
		if (!f.createNewFile()) {
			throw new IOException("Unable to create file : " + f.getAbsolutePath());
		}

		ICommandResult result = cmd.execute(logger, new NullProgressMonitor());
		Assert.assertNotNull(result);

		List<String> files = FileHelper.listFilesAsStrings(watchDir);
		Collections.sort(files);
		Assert.assertEquals(0,files.size());

		files = FileHelper.listFilesAsStrings(showDir);
		Collections.sort(files);
		Assert.assertEquals(2,files.size());
		Assert.assertEquals(new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(showDir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - Four Months Later....avi").getAbsolutePath(),files.get(1));

		Assert.assertTrue(controller.getSeenDB().isSeen(showDir, new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi")));

		files = FileHelper.listFilesAsStrings(filmDir);
		Collections.sort(files);
		Assert.assertEquals(1,files.size());
		Queue<String> events = LoggingStore.getEvents();
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("upgrade()",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("upgrade()",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("searchMedia(Heroes,TV_SHOW,null,"+showDir.getAbsolutePath()+
	            ","+new File(watchDir,"Heroes S01E01 - Blah Blah Blah.avi")+") -> null",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("cacheShow()",events.remove());
		Assert.assertEquals("getSeason()",events.remove());
		Assert.assertEquals("cacheSeason()",events.remove());
		Assert.assertEquals("getEpisode()",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("searchMedia(Heroes,TV_SHOW,null,"+showDir.getAbsolutePath()+","+
				   new File(watchDir,"Heroes S02E01 - Blah Blah Blah.avi")+") -> 79501:org.stanwood.media.source.xbmc.XBMCSource#metadata.tvdb.com - (http://www.thetvdb.com/api/1D62F2F90030C444/series/79501/all/en.zip) - (null)",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("getSeason()",events.remove());
		Assert.assertEquals("cacheSeason()",events.remove());
		Assert.assertEquals("getEpisode()",events.remove());
		Assert.assertEquals("getShow()",events.remove());
		Assert.assertEquals("searchMedia(iron man,FILM,null,"+filmDir.getAbsolutePath()+"," +
	            new File(watchDir,"iron.man.2009.dvdrip.xvid-amiable.avi")+") -> null",events.remove());
		Assert.assertEquals("getFilm()",events.remove());
		Assert.assertEquals("cacheEpisode("+showDir.getAbsolutePath()+","+showDir.getAbsolutePath()+File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi)",events.remove());
		Assert.assertEquals("cacheEpisode("+showDir.getAbsolutePath()+","+showDir.getAbsolutePath()+File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"2x01 - Four Months Later....avi)",events.remove());
		Assert.assertEquals("cacheFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"Iron Man (2008).avi)",events.remove());
		Assert.assertEquals("init()",events.remove());
		if (events.peek().equals("performedActions("+showDir.getAbsolutePath()+")")) {
			Assert.assertEquals("performedActions("+showDir.getAbsolutePath()+")",events.remove());
			Assert.assertEquals("init()",events.remove());
			Assert.assertEquals("getFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"Iron Man (2008).avi)",events.remove());
			Assert.assertEquals("performedActions("+filmDir.getAbsolutePath()+")",events.remove());
		}
		else {
			Assert.assertEquals("getFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"Iron Man (2008).avi)",events.remove());
			Assert.assertEquals("performedActions("+filmDir.getAbsolutePath()+")",events.remove());
			Assert.assertEquals("init()",events.remove());
			Assert.assertEquals("performedActions("+showDir.getAbsolutePath()+")",events.remove());
		}

		Assert.assertTrue(events.isEmpty());

		ExtensionInfo<? extends IStore> loggingStoreInfo = controller.getStoreInfo(LoggingStore.class.getName());

		MediaDirectory dir = controller.getMediaDirectory(filmDir);
		LoggingStore logginStore = (LoggingStore) loggingStoreInfo.getExtension(controller,dir.getMediaDirConfig(),0);
		IFilm film = logginStore.getFilm(dir, new File(filmDir,"Iron Man (2008).avi"));
		Assert.assertNotNull(film);
		Assert.assertEquals(1,film.getFiles().size());
		Assert.assertEquals(new File(filmDir,"Iron Man (2008).avi"),film.getFiles().get(0).getLocation());
		Assert.assertEquals(new File(watchDir,"iron.man.2009.dvdrip.xvid-amiable.avi"),film.getFiles().get(0).getOrginalLocation());

		dir = controller.getMediaDirectory(showDir);
		logginStore = (LoggingStore) loggingStoreInfo.getExtension(controller,dir.getMediaDirConfig(),0);
		IEpisode episode = logginStore.getEpisode(dir, new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi"));
		Assert.assertEquals(new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi").getAbsolutePath(),episode.getFiles().get(0).getLocation().getAbsolutePath());
		Assert.assertEquals(new File(watchDir,"Heroes S01E01 - Blah Blah Blah.avi").getAbsolutePath(),episode.getFiles().get(0).getOrginalLocation().getAbsolutePath());

		ManageMediaCommand mmCommand = new ManageMediaCommand(controller);
		List<File>mediaDirs = new ArrayList<File>();
		mediaDirs.add(filmDir);
		mediaDirs.add(showDir);
		result = mmCommand.execute(logger, new NullProgressMonitor());
		Assert.assertNotNull(result);

		Assert.assertEquals("getFilm("+filmDir.getAbsolutePath()+","+filmDir.getAbsolutePath()+File.separator+"Iron Man (2008).avi)",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("performedActions("+filmDir.getAbsolutePath()+")",events.remove());
		Assert.assertEquals("init()",events.remove());
		Assert.assertEquals("performedActions("+showDir.getAbsolutePath()+")",events.remove());

		Assert.assertTrue(events.isEmpty());

		Assert.assertTrue(controller.getSeenDB().isSeen(showDir, new File(showDir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"1x01 - Genesis.avi")));

	}

	/**
	 * Used to that films can be correctly imported when it's using a NFO file to workout
	 * what the film is. This checks that the part numbers are also correct. This also tests
	 * that XBMC addons can be installed as the IMDB sources are needed. None media files will
	 * be deleted in this test.
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testImportFilmWithNFOFileRemoveNonMedia() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");
		File showDir = FileHelper.createTmpDir("showDir");
		ConfigReader config = createTestConfig(watchDir,filmDir,showDir,null,null);
		Controller controller = new Controller(config);
		controller.init(false);

		ImportMediaCommand cmd = new ImportMediaCommand(controller);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();

		// Install IMDB Source
		XBMCInstallAddonsCommand installAddonsCmd = new XBMCInstallAddonsCommand(controller);
		List<String>addons = new ArrayList<String>();
		addons.add("metadata.imdb.com");
		installAddonsCmd.setAddons(addons);
		Assert.assertNotNull(installAddonsCmd.execute(logger, new NullProgressMonitor()));
		XBMCUpdateAddonsCommand updateAddonsCmd = new XBMCUpdateAddonsCommand(controller);
		Set<String> updateAddons = new HashSet<String>();
		updateAddons.add("metadata.imdb.com");
		updateAddons.add("metadata.common.themoviedb.org");
		updateAddonsCmd.setAddons(updateAddons);
		Assert.assertNotNull(updateAddonsCmd.execute(logger, new NullProgressMonitor()));
		Assert.assertTrue(logger.getResult().contains("Installed plugin 'metadata.common.imdb.com"));

		// Create test NFO folder
		TestNFOFilms.createFiles(watchDir);
		FileHelper.delete(new File(watchDir,"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]"+File.separator+"Sample"+File.separator+"ironman.avi"));

		// Import media
		cmd.setDeleteNonMedia(true);
		Assert.assertNotNull(cmd.execute(logger, new NullProgressMonitor()));

		List<String> files = FileHelper.listFilesAsStrings(watchDir);
		Collections.sort(files);
		Assert.assertEquals(0,files.size());

		files = FileHelper.listFilesAsStrings(showDir);
		Collections.sort(files);
		Assert.assertEquals(0,files.size());

		files = FileHelper.listFilesAsStrings(filmDir);
		Collections.sort(files);
		Assert.assertEquals(new File(filmDir,File.separator+"Iron Man (2008) Part 1.avi").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(filmDir,File.separator+"Iron Man (2008) Part 2.avi").getAbsolutePath(),files.get(1));
		Assert.assertEquals(2,files.size());
	}

	/**
	 * Used to that films can be correctly imported when it's using a NFO file to workout
	 * what the film is. This checks that the part numbers are also correct. This also tests
	 * that XBMC addons can be installed as the IMDB sources are needed.
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testImportFilmWithNFOFile() throws Exception {
		cleanup();
		setupTestFile();
		setup();

		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File watchDir = FileHelper.createTmpDir("watchdir");
		File filmDir = FileHelper.createTmpDir("filmDir");
		File showDir = FileHelper.createTmpDir("showDir");
		ConfigReader config = createTestConfig(watchDir,filmDir,showDir,null,null);
		Controller controller = new Controller(config);
		controller.init(false);

		ImportMediaCommand cmd = new ImportMediaCommand(controller);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();

		// Install IMDB Source
		XBMCInstallAddonsCommand installAddonsCmd = new XBMCInstallAddonsCommand(controller);
		List<String>addons = new ArrayList<String>();
		addons.add("metadata.imdb.com");
		installAddonsCmd.setAddons(addons);
		Assert.assertNotNull(installAddonsCmd.execute(logger, new NullProgressMonitor()));
		XBMCUpdateAddonsCommand updateAddonsCmd = new XBMCUpdateAddonsCommand(controller);
		Set<String> updateAddons = new HashSet<String>();
		updateAddons.add("metadata.imdb.com");
		updateAddons.add("metadata.common.themoviedb.org");
		updateAddonsCmd.setAddons(updateAddons);
		Assert.assertNotNull(updateAddonsCmd.execute(logger, new NullProgressMonitor()));
		Assert.assertTrue(logger.getResult().contains("Installed plugin 'metadata.common.imdb.com"));

		// Create test NFO folder
		TestNFOFilms.createFiles(watchDir);

		// Import media
//		cmd.setDeleteNonMedia(true);
		Assert.assertNotNull(cmd.execute(logger, new NullProgressMonitor()));

		List<String> files = FileHelper.listFilesAsStrings(watchDir);
		Collections.sort(files);
		Assert.assertEquals(new File(watchDir,File.separator+"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]"+File.separator+"Read This Guide Now.txt").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(watchDir,File.separator+"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]"+File.separator+"Sample"+File.separator+"ironman.avi").getAbsolutePath(),files.get(1));
		Assert.assertEquals(new File(watchDir,File.separator+"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]"+File.separator+"kkid.nfo").getAbsolutePath(),files.get(2));
		Assert.assertEquals(new File(watchDir,File.separator+"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]"+File.separator+"www.Torrentday.com.txt").getAbsolutePath(),files.get(3));
		Assert.assertEquals(4,files.size());

		files = FileHelper.listFilesAsStrings(showDir);
		Collections.sort(files);
		Assert.assertEquals(0,files.size());

		files = FileHelper.listFilesAsStrings(filmDir);
		Collections.sort(files);
		Assert.assertEquals(new File(filmDir,File.separator+"Iron Man (2008) Part 1.avi").getAbsolutePath(),files.get(0));
		Assert.assertEquals(new File(filmDir,File.separator+"Iron Man (2008) Part 2.avi").getAbsolutePath(),files.get(1));
		Assert.assertEquals(2,files.size());
	}

	private static void appendMediaDirectory(StringBuilder testConfig,File mediaDir,Mode mode,String sourceId,Map<String,String> sourceParams,String storeId,String dummy,String ... actions) {
		boolean ignoreSeen = true;
		String pattern = ConfigReader.DEFAULT_TV_FILE_PATTERN;
		if (mode == Mode.FILM) {
			pattern = ConfigReader.DEFAULT_FILM_FILE_PATTERN;
		}
		testConfig.append("  <mediaDirectory default=\"true\" directory=\""+mediaDir.getAbsolutePath()+"\" mode=\""+mode.toString()+"\" pattern=\""+pattern+"\" ignoreSeen=\""+ignoreSeen+"\" >"+FileHelper.LS);
		testConfig.append("    <sources>"+FileHelper.LS);
		if (sourceId!=null) {
			testConfig.append("      <source id=\""+sourceId+"\">"+FileHelper.LS);
			if (sourceParams!=null) {
				for (Entry<String,String> e : sourceParams.entrySet()) {
					testConfig.append("      <param name=\""+e.getKey()+"\" value=\""+e.getValue()+"\"/>"+FileHelper.LS);
				}
			}

			testConfig.append("      </source>"+FileHelper.LS);
		}
		testConfig.append("    </sources>"+FileHelper.LS);

		testConfig.append("    <stores>"+FileHelper.LS);
		if (storeId!=null) {
			testConfig.append("      <store id=\""+storeId+"\"/>"+FileHelper.LS);
		}
		testConfig.append("    </stores>"+FileHelper.LS);

		testConfig.append("    <actions>"+FileHelper.LS);
		if (actions!=null) {
			for (String action : actions) {
				testConfig.append("      <action id=\""+action+"\"/>"+FileHelper.LS);
			}
		}
		testConfig.append("    </actions>"+FileHelper.LS);
		testConfig.append("  </mediaDirectory>"+FileHelper.LS);
	}

	/**
	 * Used to create a test configuration
	 * @param watchDir The watch folder
	 * @param filmDir The film folder
	 * @param showDir The TV folder
	 * @param store The store ID to add to media dir's, or null to not add one
	 * @param extra Any extra XML to added to the configuration under &lt;mediaManager&gt;, or null not to add any
	 * @return The configuration
	 * @throws IOException Thrown if their is a IO error
	 * @throws ConfigException Thrown if their is a configuration error
	 */
	public static ConfigReader createTestConfig(File watchDir,File filmDir,File showDir,String store,String extra) throws IOException, ConfigException {
		File configDir = FileHelper.createTmpDir("configDir");
		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<mediaManager>"+FileHelper.LS);
		testConfig.append("  <global>"+FileHelper.LS);
		testConfig.append("    <configDirectory>"+configDir.getAbsolutePath()+"</configDirectory>"+FileHelper.LS);
		testConfig.append("  </global>"+FileHelper.LS);
		if (extra!=null) {
			testConfig.append(extra);
		}
		testConfig.append("  <plugins>"+FileHelper.LS);
		testConfig.append("    <plugin class=\""+LoggingStoreInfo.class.getName()+"\"/>"+FileHelper.LS);
		testConfig.append("  </plugins>"+FileHelper.LS);
		testConfig.append("  <watchDirectory directory=\""+watchDir.getAbsolutePath()+"\"/>"+FileHelper.LS);
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters", "false");
		appendMediaDirectory(testConfig, filmDir, Mode.FILM, XBMCSource.class.getName()+"#metadata.themoviedb.org",params,store,"",RenameAction.class.getName());
		params = new HashMap<String,String>();
		appendMediaDirectory(testConfig, showDir, Mode.TV_SHOW, XBMCSource.class.getName()+"#metadata.tvdb.com",params,store,"",RenameAction.class.getName());
		testConfig.append("</mediaManager>"+FileHelper.LS);

		File configFile = createConfigFileWithContents(testConfig);

		InputStream is = null;
		try {
			is = new FileInputStream(configFile);
			ConfigReader configReader = new ConfigReader(is);
			configReader.parse();
			AbstractLauncher.setConfig(configReader);
			return configReader;
		}
		finally {
			if (is!=null) {
				is.close();
			}
		}
	}


	private static File createConfigFileWithContents(StringBuilder testConfig) throws IOException {
		File configFile = FileHelper.createTempFile("config", ".xml");
		FileHelper.appendContentsToFile(configFile, testConfig);
		return configFile;
	}
}
