package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.util.FileHelper;

/**
 * This is a base class for tests of XBMC Addons
 */
public class XBMCAddonTestBase {

	private static File tmpDir;
	private XBMCAddonManager addonManager;

	/**
	 * Used to setup the scraper for use within the test
	 * @throws Exception Thrown if their are any problems spotted by the test
	 */
	@BeforeClass
	public static void setupTestFile() throws Exception {
		tmpDir = FileHelper.createTmpDir("xbmc");
		FileHelper.unzip(TestXMBCSourceTVDB.class.getResourceAsStream("xbmc-addons.zip"),tmpDir);
	}

	/**
	 * Make sure that the addon manage been used is the {@link DummyXBMCAddonManager}
	 * @throws XBMCException Thrown if their are any problems
	 */
	@Before
	public void setup() throws XBMCException {
		addonManager = createAddonManager(new File(tmpDir,"addons"),Locale.ENGLISH);
		ConfigReader.setManager(addonManager);
	}

	/**
	 * Used to clean up after the tests in the class have finished
	 * @throws IOException Thrown if their is a problem
	 */
	@AfterClass
	public void cleanup() throws IOException {
		FileHelper.delete(tmpDir);
	}

	protected XBMCAddonManager createAddonManager(File addonDir,Locale locale) throws XBMCException {
		return new DummyXBMCAddonManager(addonDir,locale);
	}

	protected XBMCAddonManager getAddonManager() {
		return addonManager;
	}
}
