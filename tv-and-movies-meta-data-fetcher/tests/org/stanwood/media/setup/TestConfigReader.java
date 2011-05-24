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
import org.stanwood.media.FakeSource;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.store.FakeStore;
import org.stanwood.media.util.FileHelper;

/**
 * Test the config reader
 */
public class TestConfigReader {

	private final static String LS = System.getProperty("line.separator");

	/**
	 * Used to test the configuration reader works correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testConfig() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("films");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager xmlns=\"http://www.w3schools.com\""+LS);
			testConfig.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+LS);
//			testConfig.append("              xsi:schemaLocation=\"https://tv-and-movies-meta-data-fetcher.googlecode.com/xml/ns/MediaManager-Config https://tv-and-movies-meta-data-fetcher.googlecode.com/svn/trunk/tv-and-movies-meta-data-fetcher/src/org/stanwood/media/xml/schema/MediaInfoFetcher-Config-2.0.xsd\""+LS);
//			testConfig.append("              version=\"2.0\""+LS);
//			testConfig.append("<mediaManager"+LS);
			testConfig.append(">"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\" pattern=\"%e.%x\"  >"+LS);
			testConfig.append("    <sources>"+LS);
			testConfig.append("      <source id=\""+FakeSource.class.getName()+"\">"+LS);
			testConfig.append("	       <param name=\"TeSTPaRAm2\" value=\"/blahPath/blah\"/>"+LS);
			testConfig.append("      </source>"+LS);
			testConfig.append("    </sources>"+LS);
			testConfig.append("    <stores>"+LS);
			testConfig.append("	     <store id=\""+FakeStore.class.getName()+"\">"+LS);
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
			Assert.assertEquals("org.stanwood.media.FakeSource",sources.get(0).getID());

			List<StoreConfig> stores = dirConfig.getStores();
			Assert.assertEquals(1,stores.size());
			Assert.assertEquals("org.stanwood.media.store.FakeStore",stores.get(0).getID());

			List<ActionConfig> actions = dirConfig.getActions();
			Assert.assertEquals(1,actions.size());
			Assert.assertEquals("a.test.action",actions.get(0).getID());

			Assert.assertNull(dirConfig.getIgnorePatterns());
		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * A test to check that the ignore patterns are read correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testIgnorePatterns() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("films");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager xmlns=\"http://www.w3schools.com\""+LS);
			testConfig.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+LS);
			testConfig.append(">"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\" pattern=\"%e.%x\"  >"+LS);
			testConfig.append("    <ignore>.*incomming.*</ignore>"+LS);
			testConfig.append("    <ignore>.*blah.*</ignore>"+LS);
			testConfig.append("    <ignore>.*[S|s]amples.*</ignore>"+LS);
			testConfig.append("    <sources>"+LS);
			testConfig.append("      <source id=\""+FakeSource.class.getName()+"\">"+LS);
			testConfig.append("	       <param name=\"TeSTPaRAm2\" value=\"/blahPath/blah\"/>"+LS);
			testConfig.append("      </source>"+LS);
			testConfig.append("    </sources>"+LS);
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

			Assert.assertNotNull(dirConfig.getIgnorePatterns());
			Assert.assertEquals(3,dirConfig.getIgnorePatterns().size());
			Assert.assertEquals(".*incomming.*",dirConfig.getIgnorePatterns().get(0).pattern());
			Assert.assertEquals(".*blah.*",dirConfig.getIgnorePatterns().get(1).pattern());
			Assert.assertEquals(".*[S|s]amples.*",dirConfig.getIgnorePatterns().get(2).pattern());
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
				Assert.assertEquals(e.getMessage(),"Unknown mode 'BLAH' for media directory '"+mediaDir.getAbsolutePath()+"'. Valid modes are 'FILM', 'TV_SHOW'");
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
			Assert.assertEquals("%sx%e - %t.%x", configReader.getMediaDirectory(mediaDir).getPattern());

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
			Assert.assertEquals(new File(homeDir,".mediaManager"+File.separator+"xbmc"+File.separator+"addons").getAbsolutePath(),
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
			Assert.assertEquals("%t{ (%y)}{ Part %p}.%x", configReader.getMediaDirectory(mediaDir).getPattern());

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

	/**
	 * Used to test that a media directories can have the correct extensions
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testDefaultExtensions() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("media");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\">"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			ConfigReader configReader = createConfigReader(testConfig);
			List<String> exts = configReader.getMediaDirectory(mediaDir).getExtensions();
			Assert.assertEquals(10,exts.size());

			Assert.assertEquals("avi",exts.get(0));
			Assert.assertEquals("mkv",exts.get(1));
			Assert.assertEquals("mov",exts.get(2));
			Assert.assertEquals("mpg",exts.get(3));
			Assert.assertEquals("mpeg",exts.get(4));
			Assert.assertEquals("mp4",exts.get(5));
			Assert.assertEquals("m4v",exts.get(6));
			Assert.assertEquals("srt",exts.get(7));
			Assert.assertEquals("sub",exts.get(8));
			Assert.assertEquals("divx",exts.get(9));
		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to test that a media directories can have the valid extensions configured
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testConfiguredExtensions() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("media");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\">"+LS);
			testConfig.append("    <extensions>"+LS);
			testConfig.append("      <extension>avi</extension>"+LS);
			testConfig.append("      <extension>m4v</extension>"+LS);
			testConfig.append("      <extension>qt</extension>"+LS);
			testConfig.append("    </extensions>"+LS);
			testConfig.append("  </mediaDirectory>"+LS);
			testConfig.append("</mediaManager>"+LS);

			ConfigReader configReader = createConfigReader(testConfig);
			List<String> exts = configReader.getMediaDirectory(mediaDir).getExtensions();
			Assert.assertEquals(3,exts.size());

			Assert.assertEquals("avi",exts.get(0));
			Assert.assertEquals("m4v",exts.get(1));
			Assert.assertEquals("qt",exts.get(2));
		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to test that the configuration directory is read correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testGlobalSettings() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<mediaManager>"+LS);
		testConfig.append("  <global>"+LS);
		testConfig.append("    <configDirectory>/blah/blah1</configDirectory>"+LS);
		testConfig.append("  </global>"+LS);
		testConfig.append("</mediaManager>"+LS);

		ConfigReader configReader = createConfigReader(testConfig);
		Assert.assertEquals("/blah/blah1",configReader.getConfigDir().getAbsolutePath());
	}

//	/**
//	 * Used to test parsing of default config file
//	 * @throws Exception  Thrown if their are any problems
//	 */
//	@Test
//	public void testParseDefaultConfigFile() throws Exception {
//		ConfigReader configReader = null;
//		InputStream is = null;
//		try {
//			is = ConfigReader.class.getResourceAsStream("defaultConfig.xml");
//			configReader = new ConfigReader(is);
//			configReader.parse();
//		}
//		finally {
//			if (is!=null) {
//				is.close();
//			}
//		}
//
//	}

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
