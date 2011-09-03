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
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.store.xmlstore.XMLStore2;
import org.stanwood.media.util.FileHelper;

/**
 * This is a test class used to test the class {@link CLIMediaManager}.
 */
@SuppressWarnings("nls")
public class TestCLIMediaManager extends XBMCAddonTestBase {

	private final static String LS = System.getProperty("line.separator");

	static int exitCode;

	/**
	 * Used to setup the exit handler
	 * @throws Exception Thrown if their are any problems
	 */
	@Before
	public void setUpApp() throws Exception {
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
	@SuppressWarnings("unchecked")
	@Test
	public void testRecursiveSourceAndStoreRename() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");
		// Create test files
		File dir = FileHelper.createTmpDir("show");
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters", "false");
		setupTestController(false,dir,pattern,Mode.TV_SHOW,XBMCSource.class.getName()+"#metadata.tvdb.com",params,XMLStore2.class.getName(),"",RenameAction.class.getName());
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
			String args[] = new String[] {"-d",dir.getAbsolutePath(),"--log_config","NOINIT","--noupdate"};
			CLIMediaManager.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			// .show.xml
			Assert.assertEquals(new File(dir,".mediaManager-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));

			Assert.assertEquals("Check exit code",0,exitCode);
			params.put("rootMediaDir", dir.getAbsolutePath());
			Helper.assertXMLEquals(TestCLIMediaManager.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			setupTestController(false,dir,pattern,Mode.TV_SHOW,null,null,XMLStore2.class.getName(),"",RenameAction.class.getName());

			// Do the renaming
			CLIMediaManager.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			Assert.assertEquals(new File(dir,".mediaManager-xmlStore.xml").getAbsolutePath(),files.get(0));
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
	@SuppressWarnings("unchecked")
	@Test
	public void testRecursiveStoreRename() throws Exception {
//		LogSetupHelper.initLogingInternalConfigFile("debug.log4j.properties");

		// Create test files
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		File dir = FileHelper.createTmpDir("show");

		setupTestController(false,dir,pattern,Mode.TV_SHOW,FakeSource.class.getName(),new HashMap<String,String>(),XMLStore2.class.getName(),"",RenameAction.class.getName());
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
			String args[] = new String[] {"-d",dir.getAbsolutePath(),"--log_config","INFO","--noupdate"};
			CLIMediaManager.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			// .show.xml
			Assert.assertEquals(new File(dir,".mediaManager-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));

			Assert.assertEquals("Check exit code",0,exitCode);
			Map<String,String>params = new HashMap<String,String>();
			params.put("rootMediaDir", dir.getAbsolutePath());
			Helper.assertXMLEquals(TestCLIMediaManager.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			setupTestController(false,dir,pattern,Mode.TV_SHOW,null,null,XMLStore2.class.getName(),"",RenameAction.class.getName());

			// Do the renaming
			CLIMediaManager.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(4,files.size());
			Assert.assertEquals(new File(dir,".mediaManager-xmlStore.xml").getAbsolutePath(),files.get(0));
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
	@SuppressWarnings("unchecked")
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

			Map<String,String>params = new HashMap<String,String>();
			params.put("posters", "false");
			setupTestController(false,filmsDir,"%t{ Part %p}.%x",Mode.FILM,XBMCSource.class.getName()+"#metadata.themoviedb.org",params,null,"",RenameAction.class.getName());

			// Do the renaming
			String args[] = new String[] {"-d",filmsDir.getAbsolutePath(),"--log_config","INFO","--noupdate"};
			CLIMediaManager.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Collections.sort(files);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man Part 1.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man Part 2.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Films"+File.separator+"Iron Man.avi").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);

			setupTestController(false,filmsDir,"%t{ Part %p}.%x",Mode.FILM,XBMCSource.class.getName()+"#metadata.tvdb.com",params,null,"",RenameAction.class.getName());
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
	@SuppressWarnings("unchecked")
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
			String args[] = new String[] {"-d",dir.getAbsolutePath(),"--log_config","INFO","--noupdate"};

			setupTestController(false,dir,pattern,Mode.TV_SHOW,XBMCSource.class.getName()+"#metadata.tvdb.com",new HashMap<String,String>(),null,"",RenameAction.class.getName());
			CLIMediaManager.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);

			// Do the renaming
			setupTestController(false,dir,pattern,Mode.TV_SHOW,XBMCSource.class.getName()+"#metadata.tvdb.com",new HashMap<String,String>(),null,"",RenameAction.class.getName());
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
	 * This is used to check that when a file is renamed it's not put into the seen database, but
	 * the other files are put into the database.
	 * @throws Exception Thrown if their is a problem
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSeenDatabase() throws Exception {
		LogSetupHelper.initLogingInternalConfigFile("info.log4j.properties");
		// Create test files
		File dir = FileHelper.createTmpDir("show");
		String pattern = "%n"+File.separator+"Season %s"+File.separator+"%e - %t.%x";
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters", "false");
		ConfigReader config = setupTestController(true,dir,pattern,Mode.TV_SHOW,XBMCSource.class.getName()+"#metadata.tvdb.com",params,XMLStore2.class.getName(),"",RenameAction.class.getName());
		try {
			File eurekaDir = new File(dir, "Heroes");
			if (!eurekaDir.mkdir()) {
				throw new IOException("Unable to create directory : " + eurekaDir.getAbsolutePath());
			}

			File f = new File(eurekaDir,"101 - Blah Blah Blah.avi");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			if (!f.setLastModified(0) && f.lastModified()!=0) {
				Assert.fail("Unable to set last modified date");
			}

			f = new File(eurekaDir,"S01E02 - Hello this is a test.mkv");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			if (!f.setLastModified(0) && f.lastModified()!=0) {
				Assert.fail("Unable to set last modified date");
			}

			f = new File(eurekaDir,"s02e02 - Hello this is a test.mpg");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			if (!f.setLastModified(0) && f.lastModified()!=0) {
				Assert.fail("Unable to set last modified date");
			}
			f = new File(eurekaDir,"s05e02 - This episode should never be found.mpg");
			if (!f.createNewFile()) {
				throw new IOException("Unable to create file : " + f.getAbsolutePath());
			}
			if (!f.setLastModified(0) && f.lastModified()!=0) {
				Assert.fail("Unable to set last modified date");
			}

			// Do the renaming
			String args[] = new String[] {"-d",dir.getAbsolutePath(),"--log_config","NOINIT","--noupdate"};
			CLIMediaManager.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(5,files.size());
			// .show.xml
			Assert.assertEquals(new File(dir,".mediaManager-xmlStore.xml").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"01 - Genesis.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 1"+File.separator+"02 - Don't Look Back.mkv").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"Season 2"+File.separator+"02 - Lizards.mpg").getAbsolutePath(),files.get(3));
			Assert.assertEquals(new File(dir,File.separator+"Heroes"+File.separator+"s05e02 - This episode should never be found.mpg").getAbsolutePath(),files.get(4));

			Assert.assertEquals("Check exit code",0,exitCode);

			params.put("rootMediaDir", dir.getAbsolutePath());
			Helper.assertXMLEquals(TestCLIMediaManager.class.getResourceAsStream("expected-rename-output.xml"), new FileInputStream(files.get(0)),params);

			File seenFile = new File(config.getConfigDir(),"seenFiles.xml");
			Assert.assertTrue(seenFile.exists());

			params.put("rootMediaDir", dir.getAbsolutePath());
			Helper.assertXMLEquals(TestCLIMediaManager.class.getResourceAsStream("expected-seendb.xml"), new FileInputStream(seenFile),params);
			FileHelper.displayFile(seenFile, System.out);
		} finally {
			FileHelper.delete(dir);
		}

	}

	/**
	 * Used to setup the controller ready for testing
	 * @param ignoreSeen True if seen files should be ignored
	 * @param mediaDir The media directory been tested
	 * @param pattern The pattern to use with rename operations
	 * @param mode The mode of the test
	 * @param sourceId The ID of the source to use or null if none are used
	 * @param sourceParams The params of the source
	 * @param storeId The ID of the store to use store to use, or null if none are used
	 * @param dummy Does nothing
	 * @param actions The actions to perform
	 * @return The configuration reader
	 * @throws Exception Thrown if their is a problem
	 */
	public static ConfigReader setupTestController(boolean ignoreSeen,File mediaDir,String pattern,Mode mode,String sourceId,Map<String,String> sourceParams,String storeId,String dummy,String ... actions) throws Exception{
		File configDir = FileHelper.createTmpDir("configDir");
		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<mediaManager>"+LS);
		testConfig.append("  <global>"+LS);
		testConfig.append("    <configDirectory>"+configDir.getAbsolutePath()+"</configDirectory>"+LS);
		testConfig.append("  </global>"+LS);
		testConfig.append("  <mediaDirectory directory=\""+mediaDir.getAbsolutePath()+"\" mode=\""+mode.toString()+"\" pattern=\""+pattern+"\" ignoreSeen=\""+ignoreSeen+"\" >"+LS);
		if (sourceId!=null) {
			testConfig.append("    <sources>"+LS);
			testConfig.append("      <source id=\""+sourceId+"\">"+LS);
			if (sourceParams!=null) {
				for (Entry<String,String> e : sourceParams.entrySet()) {
					testConfig.append("      <param name=\""+e.getKey()+"\" value=\""+e.getValue()+"\"/>"+LS);
				}
			}

			testConfig.append("      </source>"+LS);
			testConfig.append("    </sources>"+LS);
		}
		if (storeId!=null) {
			testConfig.append("    <stores>"+LS);
			testConfig.append("      <store id=\""+storeId+"\"/>"+LS);
			testConfig.append("    </stores>"+LS);
		}
		if (actions!=null) {
			for (String action : actions) {
				testConfig.append("    <actions>"+LS);
				testConfig.append("      <action id=\""+action+"\"/>"+LS);
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
			AbstractLauncher.setConfig(configReader);
			return configReader;
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
