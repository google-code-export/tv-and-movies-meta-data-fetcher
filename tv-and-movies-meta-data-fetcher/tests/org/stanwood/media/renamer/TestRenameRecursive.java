package org.stanwood.media.renamer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.stanwood.media.Helper;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.xmlstore.XMLStore2;
import org.stanwood.media.util.FileHelper;

/**
 * This test class is used to test the rescrive renaming of media files
 */
public class TestRenameRecursive extends XBMCAddonTestBase {

	private final static String LS = System.getProperty("line.separator");

	protected static int exitCode;

	/**
	 * Used to setup the exit handler
	 * @throws Exception Thrown if their are any problems
	 */
	@Before
	public void setUp() throws Exception {
		Main.setExitHandler(new IExitHandler() {
			@Override
			public void exit(int exitCode) {
				setExitCode(exitCode);
			}
		});

		setExitCode(0);
	}

	private static void setExitCode(int code) {
		exitCode = code;
	}

	/**
	 * Used to tidy up the controller before closing the test
	 * @throws Exception Thrown if their are any problems
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Used to test recursive renaming of media using a source, but no stores.
	 * @throws Exception Thrown if their are any errors
	 */
	@Test
	public void testRecursiveSourceAndStoreRename() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		setupTestController(XBMCSource.class,new HashMap<String,String>(),XMLStore2.class);
		// Create test files
		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Heroes");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory : " + eurekaDir.getAbsolutePath());
			}

			File f = new File(eurekaDir,"101 - Blah Blah Blah.avi");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			f = new File(eurekaDir,"S01E02 - Hello this is a test.mkv");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			f = new File(eurekaDir,"s02e02 - Hello this is a test.mpg");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}

			// Do the renaming
			String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
			String args[] = new String[] {"-R","-p",pattern,"-d",dir.getAbsolutePath(),"--log_config","INFO"};
			Main.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			// .show.xml
			Assert.assertEquals(new File(dir,".mediaInfoFetcher-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));

			Assert.assertEquals("Check exit code",0,exitCode);
			Map<String,String>params = new HashMap<String,String>();
			params.put("rootMediaDir", dir.getAbsolutePath());
			Helper.assertXMLEquals(TestRenameRecursive.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			setupTestController(null,null,XMLStore2.class);

			// Do the renaming
			Main.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			Assert.assertEquals(new File(dir,".mediaInfoFetcher-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));

			Helper.assertXMLEquals(TestRenameRecursive.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			Assert.assertEquals("Check exit code",0,exitCode);
		} finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * This test makes sure that the store is used and not the source
	 * @throws Exception If their are problems
	 */
	@Test
	public void testRecursiveStoreRename() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		setupTestController(FakeSource.class,new HashMap<String,String>(),XMLStore2.class);

		// Create test files
		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Heroes");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory: " + eurekaDir.getAbsolutePath());
			}

			File f = new File(eurekaDir,"101 - Blah Blah Blah.avi");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			f = new File(eurekaDir,"S01E02 - Hello this is a test.mkv");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			f = new File(eurekaDir,"s02e02 - Hello this is a test.mpg");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}

			// Do the renaming
			String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
			String args[] = new String[] {"-R","-p",pattern,"-d",dir.getAbsolutePath(),"--log_config","INFO"};
			Main.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			// .show.xml
			Assert.assertEquals(new File(dir,".mediaInfoFetcher-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));

			Assert.assertEquals("Check exit code",0,exitCode);
			Map<String,String>params = new HashMap<String,String>();
			params.put("rootMediaDir", dir.getAbsolutePath());
			Helper.assertXMLEquals(TestRenameRecursive.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			setupTestController(null,null,XMLStore2.class);

			// Do the renaming
			Main.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			Assert.assertEquals(new File(dir,".mediaInfoFetcher-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));

			Helper.assertXMLEquals(TestRenameRecursive.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			Assert.assertEquals("Check exit code",0,exitCode);
		} finally {
			FileHelper.delete(dir);
		}

	}

	@Ignore
	@Test
	public void testRecursiveFilmRename() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		setupTestController(XBMCSource.class,new HashMap<String,String>(),null);
		// Create test files
		File dir = FileHelper.createTmpDir("movies");
		try {
			File filmsDir = new File(dir, "Films");
			if (!filmsDir.mkdir() && !filmsDir.exists()) {
				throw new IOException("Unable to create dir: " + filmsDir);
			}
			File f = new File(filmsDir,"Iron.man.cd1.avi");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}

			File subDir = new File(filmsDir, "blah");
			if (!subDir.mkdir() && !subDir.exists()) {
				throw new IOException("Unable to create dir: " + filmsDir);
			}
			f = new File(filmsDir,"Iron man.cd2.avi");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}

			// Do the renaming
			String args[] = new String[] {"-R","-d",filmsDir.getAbsolutePath(),"--log_config","INFO"};
			Main.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(2,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man.avi").getAbsolutePath(),files.get(0));

			Assert.assertEquals("Check exit code",0,exitCode);

			// Do the renaming
			Main.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(2,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);

		} finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * Used to test recursive renaming of media using a source, but no stores.
	 * @throws Exception Thrown if their are any errors
	 */
	@Test
	public void testRecursiveSourceRename() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		// Create test files
		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Heroes");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directoru : " + eurekaDir.getAbsolutePath());
			}

			File f = new File(eurekaDir,"101 - Blah Blah Blah.avi");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			f = new File(eurekaDir,"S01E02 - Hello this is a test.mkv");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			f = new File(eurekaDir,"s02e02 - Hello this is a test.mpg");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}

			// Do the renaming
			String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
			String args[] = new String[] {"-R","-p",pattern,"-d",dir.getAbsolutePath(),"--log_config","INFO"};
			setupTestController(XBMCSource.class,new HashMap<String,String>(),null);
			Main.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);

			// Do the renaming
			setupTestController(XBMCSource.class,new HashMap<String,String>(),null);
			Main.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);
		} finally {
			FileHelper.delete(dir);
		}
	}

	/**
	 * Create test controller
	 * @param source The source to use with the controller , or null if none
	 * @param store The store to use with the controller, or null if none
	 * @param sourceParams Parameters that should be added to the source
	 * @throws Exception Thrown if their are any problems
	 */
	private static void setupTestController(Class<? extends ISource> source,Map<String,String> sourceParams,Class<? extends IStore> store) throws Exception{
		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<config>"+LS);
		if (source!=null) {
			testConfig.append("  <sources>"+LS);
			testConfig.append("    <source id=\""+source.getName()+"\">"+LS);
			if (sourceParams!=null) {
				for (Entry<String,String> e : sourceParams.entrySet()) {
					testConfig.append("    <param key=\""+e.getKey()+"\" value=\""+e.getValue()+"\">"+LS);
				}
			}

			testConfig.append("    </source>"+LS);
			testConfig.append("  </sources>"+LS);
		}
		if (store!=null) {
			testConfig.append("  <stores>"+LS);
			testConfig.append("    <store id=\""+store.getName()+"\"/>"+LS);
			testConfig.append("  </stores>"+LS);
		}

		testConfig.append("</config>"+LS);
		System.out.println(testConfig.toString());

		File configFile = createConfigFileWithContents(testConfig);

		InputStream is = null;
		try {
			is = new FileInputStream(configFile);
			ConfigReader configReader = new ConfigReader(is);
			configReader.parse();
			AbstractLauncher.config = configReader;
		}
		finally {
			if (is!=null) {
				is.close();
			}
		}
	}

	private static File createConfigFileWithContents(StringBuilder testConfig) throws IOException {
		File configFile = File.createTempFile("config", ".xml");
		configFile.deleteOnExit();
		FileHelper.appendContentsToFile(configFile, testConfig);
		return configFile;
	}

}
