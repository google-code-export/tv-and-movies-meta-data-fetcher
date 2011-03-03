package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.stanwood.media.renamer.Controller;
import org.stanwood.media.util.FileHelper;

/**
 * This is a base class for tests of XBMC Addons
 */
public class XBMCAddonTestBase {

	private final static Log log = LogFactory.getLog(XBMCAddonTestBase.class);

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
		log.info("Test data dir: " + tmpDir);

		updateSiteDir = FileHelper.createTmpDir("updateSite");
		FileHelper.unzip(TestXMBCSourceTVDB.class.getResourceAsStream("updates.zip"),updateSiteDir);
		log.info("Update site dir: " + updateSiteDir);
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

	protected XBMCAddonManager createAddonManager(File addonDir,Locale locale) throws XBMCException {
		return new DummyXBMCAddonManager(new File(updateSiteDir,"addons"),addonDir,locale);
	}

	protected XBMCAddonManager getAddonManager() {
		return addonManager;
	}
}
