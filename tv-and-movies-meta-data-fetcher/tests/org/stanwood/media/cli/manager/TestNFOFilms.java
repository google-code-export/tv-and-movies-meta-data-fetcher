package org.stanwood.media.cli.manager;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.model.Mode;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.source.xbmc.cli.CLIManageAddons;
import org.stanwood.media.util.FileHelper;

/**
 * This is a test class used to test that NFO files are handled correctly by the media
 * manager application.
 */
public class TestNFOFilms extends XBMCAddonTestBase {

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
		CLIManageAddons.setExitHandler(new IExitHandler() {
			@Override
			public void exit(int exitCode) {

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
	 * Tests that media renaming works as expected when their is a .NFO file.
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testNFOFilmsRenaming() throws Exception {
		File mediaDir = FileHelper.createTmpDir("films");
		try {
			createFiles(mediaDir);

			String pattern = "%t{ (%y)}{ Part %p}.%x";
			mmXBMCCmd(mediaDir, pattern,"--log_config","NOINIT","install","metadata.imdb.com");
			mmXBMCCmd(mediaDir, pattern,"--log_config","DEBUG","update");
			mmManagerCmd(mediaDir, pattern);

			List<String>files = FileHelper.listFilesAsStrings(mediaDir);

			Assert.assertEquals(6,files.size());
			Assert.assertEquals(new File(mediaDir,File.separator+"Iron Man (2008) Part 1.avi").getAbsolutePath(),files.get(0));
			Assert.assertEquals(new File(mediaDir,File.separator+"Iron Man (2008) Part 2.avi").getAbsolutePath(),files.get(1));
			Assert.assertEquals(new File(mediaDir,File.separator+"Iron Man (2008).avi").getAbsolutePath(),files.get(2));
			Assert.assertEquals(new File(mediaDir,File.separator+"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]"+File.separator+"Read This Guide Now.txt").getAbsolutePath(),files.get(3));
			Assert.assertEquals(new File(mediaDir,File.separator+"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]"+File.separator+"kkid.nfo").getAbsolutePath(),files.get(4));
			Assert.assertEquals(new File(mediaDir,File.separator+"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]"+File.separator+"www.Torrentday.com.txt").getAbsolutePath(),files.get(5));

			Assert.assertEquals("Check exit code",0,exitCode);

		}
		finally {
			if (mediaDir!=null ) {
				FileHelper.delete(mediaDir);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void mmManagerCmd(File mediaDir, String pattern) throws Exception {
		Map<String,String>params = new HashMap<String,String>();
		params.put("posters","false");
		params.put("trailer","false");
//		params.put("scrapers", "metadata.imdb.com");
		TestCLIMediaManager.setupTestController(mediaDir,pattern,Mode.FILM,XBMCSource.class,params,null,"",RenameAction.class);
		String args[] = new String[] {"-d",mediaDir.getAbsolutePath(),"--log_config","NOINIT"};
		CLIMediaManager.main(args);
	}

	@SuppressWarnings("unchecked")
	private void mmXBMCCmd(File mediaDir, String pattern,String ... cmd) throws Exception {
		TestCLIMediaManager.setupTestController(mediaDir,pattern,Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null,"",RenameAction.class);
		CLIManageAddons.main(cmd);
	}

	private void createFiles(File mediaDir) throws IOException {
		File nfoDir = new File(mediaDir,"Iron.Man.(2008).DVDRip.XViD-blah [NO-RAR] - [ www.blah.com ]");
		createFile(new File(nfoDir,"CD1"+File.separator+"ironman_part1.avi"));
		createFile(new File(nfoDir,"CD2"+File.separator+"ironman_part2.avi"));
		createFile(new File(nfoDir,"kkid.nfo"));
		createFile(new File(nfoDir,"Read This Guide Now.txt"));
		createFile(new File(nfoDir,"Sample"+File.separator+"ironman.avi"));
		createFile(new File(nfoDir,"www.Torrentday.com.txt"));
	}

	private void createFile(File file) throws IOException {
		// Create dirs
		if (!file.getParentFile().exists()) {
			if (!file.getParentFile().mkdirs() && !file.getParentFile().exists()) {
				throw new IOException("Unable to create dir: "+file.getParentFile());
			}
		}
		if (!file.exists()) {
			if (!file.createNewFile() && !file.exists()) {
				throw new IOException("Unable to create file: " + file);
			}
		}
		if (file.getName().endsWith(".nfo")) {
			PrintStream ps = null;
			try {
				ps = new PrintStream(file);
				ps.println("Iron Man (2008)\n");
				ps.println("http://www.imdb.com/title/tt0371746/\n");
				ps.println("R3 DVD9 2h 11m\n");
				ps.println("2CD 640x288 XViD 1030kb/s\n");
				ps.println("Eng Ac3 5.1 448KB/s\n");
				ps.println("\n");
				ps.println("Blah blah blah!\n");
				ps.println("\n");
			}
			finally {
				if (ps!=null) {
					ps.close();
				}
			}
		}
	}
}
