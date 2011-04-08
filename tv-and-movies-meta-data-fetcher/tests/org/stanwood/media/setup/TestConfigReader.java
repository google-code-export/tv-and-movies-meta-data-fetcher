package org.stanwood.media.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.util.FileHelper;

/**
 * Test the config reader
 */
public class TestConfigReader {

	private final static String LS = System.getProperty("line.separator");

	@Test
	public void testConfig() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("films");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager xmlns=\"http://www.w3schools.com\""+LS);
			testConfig.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+LS);
			testConfig.append("              xsi:schemaLocation=\"https://tv-and-movies-meta-data-fetcher.googlecode.com/xml/ns/MediaInfoFetcher-Config https://tv-and-movies-meta-data-fetcher.googlecode.com/svn/trunk/tv-and-movies-meta-data-fetcher/src/org/stanwood/media/xml/schema/MediaInfoFetcher-Config-2.0.xsd\""+LS);
//			testConfig.append("              version=\"2.0\""+LS);
//			testConfig.append("<mediaManager"+LS);
			testConfig.append(">"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\" pattern=\"%e.%x\"  >"+LS);
			testConfig.append("    <sources>"+LS);
			testConfig.append("      <source id=\"org.stanwood.media.renamer.FakeSource\">"+LS);
			testConfig.append("	       <param name=\"TeSTPaRAm2\" value=\"/blahPath/blah\"/>"+LS);
			testConfig.append("      </source>"+LS);
			testConfig.append("    </sources>"+LS);
			testConfig.append("    <stores>"+LS);
			testConfig.append("	     <store id=\"org.stanwood.media.store.FakeStore\">"+LS);
			testConfig.append("	       <param name=\"TeSTPaRAm1\" value=\"/testPath/blah\"/>"+LS);
			testConfig.append("	     </store>"+LS);
			testConfig.append("    </stores>"+LS);
			testConfig.append("    <actions>"+LS);
			testConfig.append("        <action id=\"a.test.action\">"+LS);
			testConfig.append("	           <param name=\"TeSTPaRAm1\" value=\"/testPath/blah\"/>"+LS);
			testConfig.append("        </action>"+LS);
			testConfig.append("    </actions>"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			ConfigReader configReader = createConfigReader(testConfig);

			try {
				configReader.getMediaDirectory(new File("blah"));
				Assert.fail("Did not detect exception");
			}
			catch (ConfigException e) {
				Assert.assertEquals("Unable to find media directory 'blah' in the configuration",e.getMessage());
			}


			MediaDirConfig dirConfig = configReader.getMediaDirectory(mediaDir);
			Assert.assertNotNull(dirConfig);
			Assert.assertEquals("%e.%x",dirConfig.getPattern());
			Assert.assertEquals(Mode.TV_SHOW,dirConfig.getMode());

			List<SourceConfig> sources = dirConfig.getSources();
			Assert.assertEquals(1,sources.size());
			Assert.assertEquals("org.stanwood.media.renamer.FakeSource",sources.get(0).getID());

			List<StoreConfig> stores = dirConfig.getStores();
			Assert.assertEquals(1,stores.size());
			Assert.assertEquals("org.stanwood.media.store.FakeStore",stores.get(0).getID());

			List<ActionConfig> actions = dirConfig.getActions();
			Assert.assertEquals(1,actions.size());
			Assert.assertEquals("a.test.action",actions.get(0).getID());
		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to check the configuration reader spots that a invalid pattern was used
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testInvalidPattern() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("films");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\" pattern=\"%z\"  >"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			try {
				createConfigReader(testConfig);
				Assert.fail("Did not detect the exception");
			}
			catch (ConfigException e) {
				Assert.assertEquals(e.getMessage(),"Invalid pattern '%z' for media directory '"+mediaDir.getAbsolutePath()+"'");
			}

		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}


	/**
	 * Used to check the configuration reader spots that a invalid mode was used
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testUnkownMode() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("films");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"BLAH\" pattern=\"%e.%x\"  >"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			try {
				createConfigReader(testConfig);
				Assert.fail("Did not detect the exception");
			}
			catch (ConfigException e) {
				Assert.assertEquals(e.getMessage(),"Unkown mode 'BLAH' for media directory '"+mediaDir.getAbsolutePath()+"'");
			}

		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to test that the configuration reader gets the correct pattern when non is given and the mode is set to TV show
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testNoPatternTV() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("media");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\">"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			ConfigReader configReader = createConfigReader(testConfig);
			Assert.assertEquals("%s %e - %t.%x", configReader.getMediaDirectory(mediaDir).getPattern());

		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to test that the correct XBMCSettings are read from the configuration file
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testXBMCSettings() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("media");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <XBMCAddons directory=\"/home/blah\" locale=\"fr\"/>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\">"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			ConfigReader configReader = createConfigReader(testConfig);
			Assert.assertEquals("/home/blah",configReader.getXBMCAddonDir().getAbsolutePath());
			Assert.assertEquals(Locale.FRENCH,configReader.getXBMCLocale());
		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to test that the correct XBMC settings are returned when none are set in the config file
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testDefaultXBMCSettings() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("media");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\">"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			File homeDir = new File(System.getProperty("user.home"));
			ConfigReader configReader = createConfigReader(testConfig);
			Assert.assertEquals(new File(homeDir,".mediaInfo"+File.separator+"xbmc"+File.separator+"addons").getAbsolutePath(),
					            configReader.getXBMCAddonDir().getAbsolutePath());
			Assert.assertEquals(Locale.ENGLISH,configReader.getXBMCLocale());
		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to test that the configuration reader gets the correct pattern when non is given and the mode is set to FILM
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testNoPatternFilm() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("media");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\">"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			ConfigReader configReader = createConfigReader(testConfig);
			Assert.assertEquals("%t.%x", configReader.getMediaDirectory(mediaDir).getPattern());

		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to test that the configuration parse can read plugins correctly
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testPlguins() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<mediaManager>"+LS);
		testConfig.append("  <plugins>"+LS);
		testConfig.append("    <plugin jar=\"/home/test/plugin.jar\" class=\"this.is.a.Test\"/>"+LS);
		testConfig.append("  </plugins>"+LS);
		testConfig.append("</mediaManager>"+LS);

		ConfigReader configReader = createConfigReader(testConfig);
		Assert.assertNotNull(configReader.getPlugins());
		Assert.assertEquals(1,configReader.getPlugins().size());
		Assert.assertEquals("/home/test/plugin.jar",configReader.getPlugins().get(0).getJar());
		Assert.assertEquals("this.is.a.Test",configReader.getPlugins().get(0).getPluginClass());
	}


	private ConfigReader createConfigReader(StringBuilder testConfig)
	throws IOException, FileNotFoundException, ConfigException {
		File configFile = FileHelper.createTmpFileWithContents(testConfig);
		ConfigReader configReader = null;
		InputStream is = null;
		try {
			is = new FileInputStream(configFile);
			configReader = new ConfigReader(is);
			configReader.parse();
		}
		finally {
			if (is!=null) {
				is.close();
			}
		}
		return configReader;
	}
}
