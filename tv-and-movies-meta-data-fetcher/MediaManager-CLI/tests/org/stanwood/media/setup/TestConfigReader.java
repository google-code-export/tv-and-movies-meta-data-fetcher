package org.stanwood.media.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.FakeSource;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.store.FakeStore;
import org.stanwood.media.util.FileHelper;

/**
 * Test the config reader
 */
@SuppressWarnings("nls")
public class TestConfigReader {


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
			testConfig.append("<mediaManager xmlns=\"http://www.w3schools.com\""+FileHelper.LS);
			testConfig.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+FileHelper.LS);
//			testConfig.append("              xsi:schemaLocation=\"https://tv-and-movies-meta-data-fetcher.googlecode.com/xml/ns/MediaManager-Config https://tv-and-movies-meta-data-fetcher.googlecode.com/svn/trunk/tv-and-movies-meta-data-fetcher/src/org/stanwood/media/xml/schema/MediaInfoFetcher-Config-2.0.xsd\""+FileHelper.LS);
//			testConfig.append("              version=\"2.0\""+FileHelper.LS);
//			testConfig.append("<mediaManager"+FileHelper.LS);
			testConfig.append(">"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\" pattern=\"%e.%x\" default=\"true\" ignoreSeen=\"true\" name=\"TV Shows\">"+FileHelper.LS);
			testConfig.append("    <sources>"+FileHelper.LS);
			testConfig.append("      <source id=\""+FakeSource.class.getName()+"\">"+FileHelper.LS);
			testConfig.append("	       <param name=\"TeSTPaRAm2\" value=\""+new File(File.separator+"testPath"+File.separator+"blah").getAbsolutePath()+"\"/>"+FileHelper.LS);
			testConfig.append("      </source>"+FileHelper.LS);
			testConfig.append("    </sources>"+FileHelper.LS);
			testConfig.append("    <stores>"+FileHelper.LS);
			testConfig.append("	     <store id=\""+FakeStore.class.getName()+"\">"+FileHelper.LS);
			testConfig.append("	       <param name=\"TeSTPaRAm1\" value=\""+new File(File.separator+"testPath"+File.separator+"blah").getAbsolutePath()+"\"/>"+FileHelper.LS);
			testConfig.append("	     </store>"+FileHelper.LS);
			testConfig.append("	     <store id=\""+FakeStore.class.getName()+"\">"+FileHelper.LS);
			testConfig.append("	       <param name=\"TeSTPaRAm4\" value=\""+new File(File.separator+"testPath"+File.separator+"blah4").getAbsolutePath()+"\"/>"+FileHelper.LS);
			testConfig.append("	     </store>"+FileHelper.LS);
			testConfig.append("    </stores>"+FileHelper.LS);
			testConfig.append("    <actions>"+FileHelper.LS);
			testConfig.append("        <action id=\"a.test.action\">"+FileHelper.LS);
			testConfig.append("	           <param name=\"TeSTPaRAm1\" value=\""+new File(File.separator+"testPath"+File.separator+"blah").getAbsolutePath()+"\"/>"+FileHelper.LS);
			testConfig.append("        </action>"+FileHelper.LS);
			testConfig.append("    </actions>"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

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
			Assert.assertTrue(dirConfig.isDefaultForMode());
			Assert.assertEquals(Mode.TV_SHOW,dirConfig.getMode());
			Assert.assertTrue(dirConfig.getIgnoreSeen());
			Assert.assertEquals("TV Shows",dirConfig.getName());

			List<SourceConfig> sources = dirConfig.getSources();
			Assert.assertEquals(1,sources.size());
			Assert.assertEquals("org.stanwood.media.FakeSource",sources.get(0).getID());

			List<StoreConfig> stores = dirConfig.getStores();
			Assert.assertEquals(2,stores.size());
			Assert.assertEquals("org.stanwood.media.store.FakeStore",stores.get(0).getID());
			Assert.assertEquals(new File(File.separator+"testPath"+File.separator+"blah").getAbsolutePath(),stores.get(0).getParams().get("TeSTPaRAm1"));
			Assert.assertEquals("org.stanwood.media.store.FakeStore",stores.get(1).getID());
			Assert.assertEquals(new File(File.separator+"testPath"+File.separator+"blah4").getAbsolutePath(),stores.get(1).getParams().get("TeSTPaRAm4"));

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
			testConfig.append("<mediaManager xmlns=\"http://www.w3schools.com\""+FileHelper.LS);
			testConfig.append("              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+FileHelper.LS);
			testConfig.append(">"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\" pattern=\"%e.%x\"  >"+FileHelper.LS);
			testConfig.append("    <ignore>.*incomming.*</ignore>"+FileHelper.LS);
			testConfig.append("    <ignore>.*blah.*</ignore>"+FileHelper.LS);
			testConfig.append("    <ignore>.*[S|s]amples.*</ignore>"+FileHelper.LS);
			testConfig.append("    <sources>"+FileHelper.LS);
			testConfig.append("      <source id=\""+FakeSource.class.getName()+"\">"+FileHelper.LS);
			testConfig.append("	       <param name=\"TeSTPaRAm2\" value=\"/blahPath/blah\"/>"+FileHelper.LS);
			testConfig.append("      </source>"+FileHelper.LS);
			testConfig.append("    </sources>"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

			ConfigReader configReader = createConfigReader(testConfig);

			try {
				configReader.getMediaDirectory(new File("blah"));
				Assert.fail("Did not detect exception");
			}
			catch (ConfigException e) {
				Assert.assertEquals("Unable to find media directory 'blah' in the configuration",e.getMessage());
			}


			MediaDirConfig dirConfig = configReader.getMediaDirectory(mediaDir);
			Assert.assertFalse(dirConfig.getIgnoreSeen());
			Assert.assertFalse(dirConfig.isDefaultForMode());
			Assert.assertNull(dirConfig.getName());
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
	 * Used to test the reading and writing of resources
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testResources() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+FileHelper.LS);
		testConfig.append("<mediaManager>"+FileHelper.LS);
		testConfig.append("  <resources>"+FileHelper.LS);
		testConfig.append("    <databaseResource id=\"testDB\">"+FileHelper.LS);
		testConfig.append("      <url>jdbc:/test/test</url>"+FileHelper.LS);
		testConfig.append("      <dialect>test.dialect</dialect>"+FileHelper.LS);
		testConfig.append("    </databaseResource>"+FileHelper.LS);
		testConfig.append("    <databaseResource id=\"testDB2\">"+FileHelper.LS);
		testConfig.append("      <url>jdbc:/test/test1</url>"+FileHelper.LS);
		testConfig.append("      <username>testuser1</username>"+FileHelper.LS);
		testConfig.append("      <password>testpass1</password>"+FileHelper.LS);
		testConfig.append("      <dialect>test.dialect1</dialect>"+FileHelper.LS);
		testConfig.append("    </databaseResource>"+FileHelper.LS);
		testConfig.append("  </resources>"+FileHelper.LS);
		testConfig.append("</mediaManager>"+FileHelper.LS);

		ConfigReader config = createConfigReader(testConfig);

		Map<String, DBResource> resources = config.getDatabaseResoruces();
		DBResource resource = resources.get("testDB");
		Assert.assertNotNull(resource);
		Assert.assertEquals("jdbc:/test/test",resource.getUrl());
		Assert.assertEquals("test.dialect",resource.getDialect());
		Assert.assertNull(resource.getUsername());
		Assert.assertNull(resource.getPassword());

		resource = resources.get("testDB2");
		Assert.assertNotNull(resource);
		Assert.assertEquals("jdbc:/test/test1",resource.getUrl());
		Assert.assertEquals("test.dialect1",resource.getDialect());
		Assert.assertEquals("testuser1",resource.getUsername());
		Assert.assertEquals("testpass1",resource.getPassword());

		File tmpFile = FileHelper.createTempFile("config", ".xml");
		config.writeConfig(new NullProgressMonitor(), tmpFile);

		String written = FileHelper.readFileContents(tmpFile);
		Assert.assertEquals(testConfig.toString(), written);

		FileHelper.delete(tmpFile);

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
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\" pattern=\"%z\"  >"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

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
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"BLAH\" pattern=\"%e.%x\"  >"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

			try {
				createConfigReader(testConfig);
				Assert.fail("Did not detect the exception");
			}
			catch (ConfigException e) {
				Assert.assertEquals(e.getMessage(),"Unknown mode 'BLAH' for media directory '"+mediaDir.getAbsolutePath()+"'. Valid modes are: 'FILM', 'TV_SHOW'");
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
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\">"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

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
	public void testXBMCSettings1() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("media");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <XBMCAddons directory=\"/home/blah\" locale=\"fr\" addonSite=\"http://blah.com/addons\"/>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\">"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

			ConfigReader configReader = createConfigReader(testConfig);
			Assert.assertEquals(new File(File.separator+"home"+File.separator+"blah").getAbsolutePath(),configReader.getXBMCAddonDir().getAbsolutePath());
			Assert.assertEquals("http://blah.com/addons",configReader.getXBMCAddonSiteUrl());
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
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\">"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

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
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\">"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

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
		testConfig.append("<mediaManager>"+FileHelper.LS);
		testConfig.append("  <plugins>"+FileHelper.LS);
		testConfig.append("    <plugin jar=\"/home/test/plugin.jar\" class=\"this.is.a.Test\"/>"+FileHelper.LS);
		testConfig.append("  </plugins>"+FileHelper.LS);
		testConfig.append("</mediaManager>"+FileHelper.LS);

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
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\">"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

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
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\">"+FileHelper.LS);
			testConfig.append("    <extensions>"+FileHelper.LS);
			testConfig.append("      <extension>avi</extension>"+FileHelper.LS);
			testConfig.append("      <extension>m4v</extension>"+FileHelper.LS);
			testConfig.append("      <extension>qt</extension>"+FileHelper.LS);
			testConfig.append("    </extensions>"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

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
		testConfig.append("<mediaManager>"+FileHelper.LS);
		testConfig.append("  <global>"+FileHelper.LS);
		testConfig.append("    <configDirectory>/blah/blah1</configDirectory>"+FileHelper.LS);
		testConfig.append("    <native>/This/is/a/test</native>"+FileHelper.LS);
		testConfig.append("  </global>"+FileHelper.LS);
		testConfig.append("</mediaManager>"+FileHelper.LS);

		ConfigReader configReader = createConfigReader(testConfig);
		Assert.assertEquals(new File(File.separator+"blah"+File.separator+"blah1").getAbsolutePath(),configReader.getConfigDir().getAbsolutePath());
		Assert.assertEquals(new File(File.separator+"This"+File.separator+"is"+File.separator+"a"+File.separator+"test").getAbsolutePath(),configReader.getNativeFolder().getAbsolutePath());
	}

	/**
	 * Used to test that configuration is written correctly
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testWriting() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("films");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <plugins>"+FileHelper.LS);
			testConfig.append("    <plugin jar=\"/home/test/plugin.jar\" class=\"this.is.a.Test\"/>"+FileHelper.LS);
			testConfig.append("  </plugins>"+FileHelper.LS);
			testConfig.append("  <XBMCAddons directory=\""+new File(File.separator+"home"+File.separator+"blah")+"\" locale=\"fr\" addonSite=\"http://blah.com/addons\"/>"+FileHelper.LS);
			testConfig.append("  <global>"+FileHelper.LS);
			testConfig.append("    <configDirectory>/blah/blah1</configDirectory>"+FileHelper.LS);
			testConfig.append("    <native>/This/is/a/test</native>"+FileHelper.LS);
			testConfig.append("  </global>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"TV_SHOW\" pattern=\"%e.%x\" ignoreSeen=\"true\" >"+FileHelper.LS);
			testConfig.append("    <ignore>.*incomming.*</ignore>"+FileHelper.LS);
			testConfig.append("    <ignore>.*blah.*</ignore>"+FileHelper.LS);
			testConfig.append("    <ignore>.*[S|s]amples.*</ignore>"+FileHelper.LS);
			testConfig.append("    <extensions>"+FileHelper.LS);
			testConfig.append("      <extension>avi</extension>"+FileHelper.LS);
			testConfig.append("      <extension>m4v</extension>"+FileHelper.LS);
			testConfig.append("      <extension>qt</extension>"+FileHelper.LS);
			testConfig.append("    </extensions>"+FileHelper.LS);
			testConfig.append("    <sources>"+FileHelper.LS);
			testConfig.append("      <source id=\""+FakeSource.class.getName()+"\">"+FileHelper.LS);
			testConfig.append("	       <param name=\"TeSTPaRAm2\" value=\""+new File(File.separator+"blahPath"+File.separator+"blah")+"\"/>"+FileHelper.LS);
			testConfig.append("      </source>"+FileHelper.LS);
			testConfig.append("    </sources>"+FileHelper.LS);
			testConfig.append("    <stores>"+FileHelper.LS);
			testConfig.append("	     <store id=\""+FakeStore.class.getName()+"\">"+FileHelper.LS);
			testConfig.append("	       <param name=\"TeSTPaRAm1\" value=\""+new File(File.separator+"testPath"+File.separator+"blah")+"\"/>"+FileHelper.LS);
			testConfig.append("	     </store>"+FileHelper.LS);
			testConfig.append("    </stores>"+FileHelper.LS);
			testConfig.append("    <actions>"+FileHelper.LS);
			testConfig.append("        <action id=\"a.test.action\">"+FileHelper.LS);
			testConfig.append("	           <param name=\"TeSTPaRAm1\" value=\""+new File(File.separator+"testPath"+File.separator+"blah")+"\"/>"+FileHelper.LS);
			testConfig.append("        </action>"+FileHelper.LS);
			testConfig.append("    </actions>"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

			ConfigReader configReader = createConfigReader(testConfig);
			File tmpFile = FileHelper.createTempFile("config", ".xml");
			configReader.writeConfig(new NullProgressMonitor(), tmpFile);

			Map<String, String> params = new HashMap<String,String>();
			params.put("FILM_DIR", mediaDir.getAbsolutePath());

			FileHelper.delete(tmpFile);

		}
		finally {
			FileHelper.delete(mediaDir);
		}
	}

	/**
	 * Used to set the default sources/stores/actions
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testDefaults() throws Exception {
		File mediaDir = FileHelper.createTmpDir("media");
		try {
			File tvShowsDir = new File(mediaDir,"shows");
			if (!tvShowsDir.mkdir() && !tvShowsDir.exists()) {
				throw new IOException("Unable to create dir: " + tvShowsDir);
			}
			File filmsDir = new File(mediaDir,"films");
			if (!filmsDir.mkdir() && !filmsDir.exists()) {
				throw new IOException("Unable to create dir: " + filmsDir);
			}
			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+filmsDir.getAbsolutePath()+"\" mode=\"FILM\">"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("  <mediaDirectory directory=\""+tvShowsDir.getAbsolutePath()+"\" mode=\"TV_SHOW\">"+FileHelper.LS);
			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

			ConfigReader configReader = createConfigReader(testConfig);
			Collection<File> dirs = configReader.getMediaDirectories();
			Iterator<File> it = dirs.iterator();
			Assert.assertEquals(2,dirs.size());
			MediaDirConfig dir = configReader.getMediaDirectory(it.next());
			Assert.assertEquals(Mode.FILM,dir.getMode());
			Assert.assertEquals(filmsDir,dir.getMediaDir());
			List<SourceConfig> sources = dir.getSources();
			Assert.assertEquals(2,sources.size());
			Assert.assertEquals("org.stanwood.media.source.xbmc.XBMCSource#metadata.themoviedb.org",sources.get(0).getID());
			Assert.assertEquals("org.stanwood.media.source.xbmc.XBMCSource#metadata.imdb.com",sources.get(1).getID());
//			List<StoreConfig> stores = dir.getStores();
//			Assert.assertEquals(2,stores.size());
//			Assert.assertEquals("org.stanwood.media.store.mp4.MP4ITunesStore",stores.get(0).getID());
//			Assert.assertEquals("org.stanwood.media.store.db.FileDatabaseStore",stores.get(1).getID());
			List<ActionConfig> actions = dir.getActions();
			Assert.assertEquals(1,actions.size());
			Assert.assertEquals("org.stanwood.media.actions.rename.RenameAction",actions.get(0).getID());

			dir = configReader.getMediaDirectory(it.next());
			Assert.assertEquals(Mode.TV_SHOW,dir.getMode());
			Assert.assertEquals(tvShowsDir,dir.getMediaDir());
			sources = dir.getSources();
			Assert.assertEquals(1,sources.size());
			Assert.assertEquals("org.stanwood.media.source.xbmc.XBMCSource#metadata.tvdb.com",sources.get(0).getID());
//			stores = dir.getStores();
//			Assert.assertEquals(2,stores.size());
//			Assert.assertEquals("org.stanwood.media.store.mp4.MP4ITunesStore",stores.get(0).getID());
//			Assert.assertEquals("org.stanwood.media.store.db.FileDatabaseStore",stores.get(1).getID());
			actions = dir.getActions();
			Assert.assertEquals(1,actions.size());
			Assert.assertEquals("org.stanwood.media.actions.rename.RenameAction",actions.get(0).getID());

		}
		finally {
			FileHelper.delete(mediaDir);
		}

	}

//	@Test
//	public void testScripts() throws Exception {
//		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
//
//		File mediaDir = FileHelper.createTmpDir("media");
//		try {
//
//			StringBuilder testConfig = new StringBuilder();
//			testConfig.append("<mediaManager>"+FileHelper.LS);
//			testConfig.append("  <scripts>"+FileHelper.LS);
//			testConfig.append("    <script lang=\"jruby\"><![CDATA["+FileHelper.LS);
//			testConfig.append("      def getYear(video)"+FileHelper.LS);
//			testConfig.append("          if (video.getYear()!=nil)"+FileHelper.LS);
//			testConfig.append("              return video.getYear()"+FileHelper.LS);
//			testConfig.append("          end"+FileHelper.LS);
//			testConfig.append("          return nil;"+FileHelper.LS);
//			testConfig.append("      end"+FileHelper.LS);
//			testConfig.append("    ]]></script>"+FileHelper.LS);
//			testConfig.append("    <script lang=\"jruby\" file=\"blah.rb\"/>"+FileHelper.LS);
//			testConfig.append("  </scripts>"+FileHelper.LS);
//			testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\"FILM\">"+FileHelper.LS);
//			testConfig.append("    <pattern type=\"jruby\"><![CDATA[\"#{video.getTitle()}#{getYear(video)}#{getPart(part)}#{File.extname(filename)}\"]]></pattern>"+FileHelper.LS);
//			testConfig.append("  </mediaDirectory>"+FileHelper.LS);
//			testConfig.append("</mediaManager>"+FileHelper.LS);
//
//			ConfigReader configReader = createConfigReader(testConfig);
//		}
//		finally {
//			FileHelper.delete(mediaDir);
//		}
//	}

	/**
	 * Used to test that the watch folders can be configured
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testWatchFolders() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");

		File mediaDir = FileHelper.createTmpDir("New");
		try {

			StringBuilder testConfig = new StringBuilder();
			testConfig.append("<mediaManager>"+FileHelper.LS);
			testConfig.append("  <watchDirectory directory=\""+mediaDir.getAbsolutePath()+"\"/>"+FileHelper.LS);
			testConfig.append("</mediaManager>"+FileHelper.LS);

			ConfigReader configReader = createConfigReader(testConfig);
			Assert.assertEquals(1,configReader.getWatchDirectories().size());
			Assert.assertEquals(mediaDir.getAbsolutePath(),configReader.getWatchDirectories().iterator().next().getWatchDir().getAbsolutePath());

		}
		finally {
			FileHelper.delete(mediaDir);
		}

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

	/**
	 * Used to create a configuration reader
	 * @param testConfig The configuration
	 * @return The configuration reader
	 * @throws IOException Thrown if their is a IO problem
	 * @throws FileNotFoundException Thrown if a file is not found
	 * @throws ConfigException Thrown if their are other problems
	 */
	public static ConfigReader createConfigReader(StringBuilder testConfig)
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

