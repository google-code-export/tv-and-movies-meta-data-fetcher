package org.stanwood.media.cli.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.FakeSource;
import org.stanwood.media.Helper;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.ISource;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.xmlstore.XMLStore2;
import org.stanwood.media.util.FileHelper;


public class TestCLIMediaManager extends XBMCAddonTestBase {

	private final static String LS = System.getProperty("line.separator");

	protected static int exitCode;

	/**
	 * Used to setup the exit handler
	 * @throws Exception Thrown if their are any problems
	 */
	@Before
	public void setUp() throws Exception {
		CLIMediaManager.setExitHandler(new IExitHandler() {
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
		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		// Create test files
		File dir = FileHelper.createTmpDir("show");
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		setupTestController(dir,pattern,Mode.TV_SHOW,XBMCSource.class,new HashMap<String,String>(),XMLStore2.class,RenameAction.class);
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
			String args[] = new String[] {"-d",dir.getAbsolutePath(),"--log_config","INFO"};
			CLIMediaManager.main(args);

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
			Helper.assertXMLEquals(TestCLIMediaManager.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			setupTestController(dir,pattern,Mode.TV_SHOW,null,null,XMLStore2.class,RenameAction.class);

			// Do the renaming
			CLIMediaManager.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			Assert.assertEquals(new File(dir,".mediaInfoFetcher-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));

			Helper.assertXMLEquals(TestCLIMediaManager.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

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
//		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");

		// Create test files
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		File dir = FileHelper.createTmpDir("show");
		setupTestController(dir,pattern,Mode.TV_SHOW,FakeSource.class,new HashMap<String,String>(),XMLStore2.class,RenameAction.class);
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
			String args[] = new String[] {"-d",dir.getAbsolutePath(),"--log_config","INFO"};
			CLIMediaManager.main(args);

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
			Helper.assertXMLEquals(TestCLIMediaManager.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			setupTestController(dir,pattern,Mode.TV_SHOW,null,null,XMLStore2.class,RenameAction.class);

			// Do the renaming
			CLIMediaManager.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			Assert.assertEquals(new File(dir,".mediaInfoFetcher-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));

			Helper.assertXMLEquals(TestCLIMediaManager.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			Assert.assertEquals("Check exit code",0,exitCode);
		} finally {
			FileHelper.delete(dir);
		}

	}

	/**
	 * This will test that films are renamed correctly
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testRecursiveFilmRename() throws Exception {
//		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		// Create test files
		File dir = FileHelper.createTmpDir("movies");
		File filmsDir = new File(dir, "Films");
		try {

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

			f = new File(filmsDir,"iron.man.2009.dvdrip.xvid-amiable.avi");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}

			setupTestController(filmsDir,"%t{ Part %p}.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null,RenameAction.class);

			// Do the renaming
			String args[] = new String[] {"-d",filmsDir.getAbsolutePath(),"--log_config","INFO"};
			CLIMediaManager.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Collections.sort(files);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man Part 1.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man Part 2.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man.avi").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);

			setupTestController(filmsDir,"%t{ Part %p}.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null,RenameAction.class);
			// Do the renaming
			CLIMediaManager.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Collections.sort(files);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man Part 1.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man Part 2.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man.avi").getAbsolutePath(),files.get(2));

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
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
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
			String args[] = new String[] {"-d",dir.getAbsolutePath(),"--log_config","INFO"};
			setupTestController(dir,pattern,Mode.TV_SHOW,XBMCSource.class,new HashMap<String,String>(),null,RenameAction.class);
			CLIMediaManager.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);

			// Do the renaming
			setupTestController(dir,pattern,Mode.TV_SHOW,XBMCSource.class,new HashMap<String,String>(),null,RenameAction.class);
			CLIMediaManager.main(args);

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
	 * Used to setup the controller ready for testing
	 * @param mediaDir The media directory been tested
	 * @param pattern The pattern to use with rename operations
	 * @param mode The mode of the test
	 * @param source The source to use, or null if none are used
	 * @param sourceParams The params of the source
	 * @param store The store to use, or null if none are used
	 * @throws Exception Thrown if their is a problem
	 */
	public void setupTestController(File mediaDir,String pattern,Mode mode,Class<? extends ISource> source,Map<String,String> sourceParams,Class<? extends IStore> store,Class<? extends IAction> ... actions) throws Exception{
		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<mediaManager>"+LS);
		testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\""+mode.toString()+"\" pattern=\""+pattern+"\"  >"+LS);
		if (source!=null) {
			testConfig.append("    <sources>"+LS);
			testConfig.append("      <source id=\""+source.getName()+"\">"+LS);
			if (sourceParams!=null) {
				for (Entry<String,String> e : sourceParams.entrySet()) {
					testConfig.append("      <param key=\""+e.getKey()+"\" value=\""+e.getValue()+"\">"+LS);
				}
			}

			testConfig.append("      </source>"+LS);
			testConfig.append("    </sources>"+LS);
		}
		if (store!=null) {
			testConfig.append("    <stores>"+LS);
			testConfig.append("      <store id=\""+store.getName()+"\"/>"+LS);
			testConfig.append("    </stores>"+LS);
		}
		if (actions!=null) {
			for (Class<? extends IAction> action : actions) {
				testConfig.append("    <actions>"+LS);
				testConfig.append("      <action id=\""+action.getName()+"\"/>"+LS);
				testConfig.append("    </actions>"+LS);
			}
		}

		testConfig.append("  </mediaDirectory>"+LS);
		testConfig.append("</mediaManager>"+LS);

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
		File configFile = FileHelper.createTempFile("config", ".xml");
		FileHelper.appendContentsToFile(configFile, testConfig);
		return configFile;
	}
}
