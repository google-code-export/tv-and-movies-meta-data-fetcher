package org.stanwood.media.renamer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.FileHelper;
import org.stanwood.media.setup.ConfigReader;

/**
 * This test class is used to test the rescrive renaming of media files
 */
public class TestRenameRecursive {

	private final static String LS = System.getProperty("line.separator");

	protected static int exitCode;

	@Before
	public void setUp() throws Exception {
		Main.doInit = false;
		Main.exitHandler = new IExitHandler() {
			@Override
			public void exit(int exitCode) {
				TestRenamer.exitCode = exitCode;
			}
		};

		exitCode = 0;
//		Controller.initWithDefaults();
		setupTestController();
	}

	@After
	public void tearDown() throws Exception {
		Main.doInit = false;
		Controller.destoryController();
	}

	@Test
	public void testRecursiveSourceRename() throws Exception {
		// Create test files
		File dir = FileHelper.createTmpDir("show");
		try {
			File eurekaDir = new File(dir, "Eureka");
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
			Main.main(args);

			// Check that things were renamed correctly
			List<String>files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Eureka"+File.separator+"Season 1"+File.separator+"01 - Pilot.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Eureka"+File.separator+"Season 1"+File.separator+"02 - Many Happy Returns.mkv").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Eureka"+File.separator+"Season 2"+File.separator+"02 - Try, Try Again.mpg").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);

			// Do the renaming
			Main.main(args);

			// Check things are still correct
			files = FileHelper.listFilesAsStrings(dir);
			Assert.assertEquals(3,files.size());
			Assert.assertEquals(new File(dir,File.separator+"Eureka"+File.separator+"Season 1"+File.separator+"01 - Pilot.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(dir,File.separator+"Eureka"+File.separator+"Season 1"+File.separator+"02 - Many Happy Returns.mkv").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(dir,File.separator+"Eureka"+File.separator+"Season 2"+File.separator+"02 - Try, Try Again.mpg").getAbsolutePath(),files.get(2));

			Assert.assertEquals("Check exit code",0,exitCode);
		} finally {
			FileHelper.deleteDir(dir);
		}

	}

	private void setupTestController() throws Exception{
		StringBuilder testConfig = new StringBuilder();
		testConfig.append("<config>"+LS);
		testConfig.append("    <sources>"+LS);
		testConfig.append("  <source id=\"org.stanwood.media.source.DummyTVComSource\"/>"+LS);
		testConfig.append("  </sources>"+LS);
		testConfig.append("</config>"+LS);

		File configFile = TestController.createConfigFileWithContents(testConfig);

		ConfigReader configReader = new ConfigReader(configFile);
		configReader.parse();
		Controller.destoryController();
		Controller.initFromConfigFile(configReader);
	}

}
