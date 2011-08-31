package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.stanwood.media.Controller;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.util.FileHelper;

/**
 * This is a base class for tests of XBMC Addons
 */
@SuppressWarnings("nls")
public class XBMCAddonTestBase {

	private static File tmpDir;
	private static File updateSiteDir;
	private XBMCAddonManager addonManager;

	/**
	 * Used to setup the scraper for use within the test
	 * @throws Exception Thrown if their are any problems spotted by the test
	 */
	@BeforeClass
	public static void setupTestFile() throws Exception {
//		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		tmpDir = FileHelper.createTmpDir("xbmc");
		FileHelper.unzip(TestXMBCSourceTVDB.class.getResourceAsStream("xbmc-addons.zip"),tmpDir);
		System.out.println("Test data dir: " + tmpDir);

		updateSiteDir = FileHelper.createTmpDir("updateSite");
		FileHelper.unzip(TestXMBCSourceTVDB.class.getResourceAsStream("updates.zip"),updateSiteDir);
		System.out.println("Update site dir: " + updateSiteDir);
	}

	/**
	 * Make sure that the addon manage been used is the {@link DummyXBMCAddonManager}
	 * @throws XBMCException Thrown if their are any problems
	 */
	@Before
	public void setup() throws XBMCException {
		addonManager = createAddonManager(new File(tmpDir,"addons"),Locale.ENGLISH);
		Controller.setXBMCAddonManager(addonManager);
	}

	protected void removeAddons() throws IOException {
		FileHelper.delete(tmpDir);
		if (!tmpDir.mkdir() && !tmpDir.exists()) {
			throw new IOException("Unable to create directory: "+ tmpDir);
		}
	}

	/**
	 * Used to clean up after the tests in the class have finished
	 * @throws IOException Thrown if their is a problem
	 */
	@AfterClass
	public static void cleanup() throws IOException {
		FileHelper.delete(tmpDir);
		FileHelper.delete(updateSiteDir);
	}

	protected XBMCAddonManager createAddonManager(final File addonDir,final Locale locale) throws XBMCException {
		ConfigReader config = new ConfigReader(null) {
			@Override
			public File getXBMCAddonDir() {
				return addonDir;
			}

			@Override
			public Locale getXBMCLocale() {
				return locale;
			}

		};
		return new DummyXBMCAddonManager(config,new File(updateSiteDir,"addons"));
	}

	protected XBMCAddonManager createAddonManager(final Locale locale) throws XBMCException {
		return createAddonManager(new File(tmpDir,"addons"), locale);
	}

	protected XBMCAddonManager getAddonManager() {
		return addonManager;
	}
}