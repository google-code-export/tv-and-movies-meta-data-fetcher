package org.stanwood.media.source.xbmc;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.util.FileHelper;

/**
 * This is a test class used to test that the {@link XBMCWebUpdater} class functions as expected.
 */
public class TestUpdater extends XBMCAddonTestBase {

	/**
	 * This test will check that updating the addons works
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testUpdate() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		File addonsDir = FileHelper.createTmpDir("addons");
		try {
			XBMCAddonManager mgr = createAddonManager(addonsDir,Locale.ENGLISH);
			int count = mgr.getUpdater().update();
			Assert.assertEquals("Check number of updated plugins",5,count);

			count = mgr.getUpdater().update();
			Assert.assertEquals("Check number of updated plugins",0,count);



			assertFiles(addonsDir);

			Set<String> addons = mgr.listAddons();
			Assert.assertEquals("Check number of addons registered",5,addons.size());

			XBMCAddon addon = mgr.getAddon("metadata.themoviedb.org");
			Assert.assertEquals("metadata.themoviedb.org",addon.getId());
			Assert.assertEquals("The MovieDB",addon.getName());
			Assert.assertEquals("1.2.4",addon.getVersion().toString());

			Assert.assertNotNull(addon.getScraper(Mode.FILM));

			addon = mgr.getAddon("metadata.tvdb.com");
			Assert.assertEquals("metadata.tvdb.com",addon.getId());
			Assert.assertEquals("The TVDB",addon.getName());
			Assert.assertEquals("1.1.0",addon.getVersion().toString());

			Assert.assertNotNull(addon.getScraper(Mode.TV_SHOW));

		}
		finally {
			FileHelper.delete(addonsDir);
		}
	}

	/**
	 * Used to test that the plugins are listed correctly
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testListPlugins() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		XBMCAddonManager mgr = createAddonManager(Locale.ENGLISH);
		List<AddonDetails> addonDetails = mgr.getUpdater().listAddons();

		Assert.assertEquals(244,addonDetails.size());

		int installed = 0;
		int uninstalled = 0;
		int updateable = 0;

		for (AddonDetails ad : addonDetails) {
			switch (ad.getStatus()) {
				case INSTALLED:
					installed++;
					break;
				case NOT_INSTALLED:
					uninstalled++;
					break;
				case OUT_OF_DATE:
					updateable++;
					break;
			}
		}

		Assert.assertEquals(30,installed);
		Assert.assertEquals(208,uninstalled);
		Assert.assertEquals(6,updateable);
	}

	private void assertFiles(File addonsDir) {
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

}

