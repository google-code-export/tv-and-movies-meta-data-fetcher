package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.util.FileHelper;

public class TestUpdater {

	private static File tmpDir;
	private XBMCAddonManager addonManager;

	/**
	 * Used to setup the scraper for use within the test
	 * @throws Exception Thrown if their are any problems spotted by the test
	 */
	@BeforeClass
	public static void setupTestFile() throws Exception {
		tmpDir = FileHelper.createTmpDir("updateSite");
		FileHelper.unzip(TestXMBCSourceTVDB.class.getResourceAsStream("updates.zip"),tmpDir);
	}

	/**
	 * Used to clean up after the tests in the class have finished
	 * @throws IOException Thrown if their is a problem
	 */
	@AfterClass
	public static void cleanup() throws IOException {
		FileHelper.delete(tmpDir);
	}

	@Test
	public void testUpdate() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		File addonsDir = FileHelper.createTmpDir("addons");
		try {
			XBMCWebUpdater updater = new XBMCWebUpdater();
			DummyXBMCAddonManager mgr = new DummyXBMCAddonManager(addonsDir,Locale.ENGLISH);
			mgr.setUpdateSite(new File(tmpDir,"addons"));
			updater.setAddonManager(mgr);
			updater.update(addonsDir);

			List<String> files = FileHelper.listFilesAsStrings(addonsDir);
			Collections.sort(files);

			Assert.assertEquals(45, files.size());
			int index=0;
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"addon.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.common.hdtrailers.net/addon.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.common.hdtrailers.net/hdtrailers.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.common.imdb.com/addon.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.common.imdb.com/changelog.txt");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.common.imdb.com/imdb.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.common.themoviedb.org/addon.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.common.themoviedb.org/tmdb.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/addon.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/changelog.txt");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/icon.png");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Bulgarian/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Chinese (Simple)/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Dutch/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/English/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/French/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/German/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Hungarian/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Korean/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Polish/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Portuguese/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Romanian/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Russian/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Spanish/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/language/Swedish/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/resources/settings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.themoviedb.org/tmdb.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/addon.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/changelog.txt");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/icon.png");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Chinese (Simple)/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Dutch/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/English/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Finnish/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/French/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/German/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Hungarian/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Korean/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Polish/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Portuguese/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Russian/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Spanish/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/language/Swedish/strings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/resources/settings.xml");
			Assert.assertEquals(files.get(index++),addonsDir+File.separator+"metadata.tvdb.com/tvdb.xml");
		}
		finally {
			FileHelper.delete(addonsDir);
		}
	}

}
