package org.stanwood.media.source.xbmc.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.util.FileHelper;

public class BaseCLITest extends XBMCAddonTestBase {

	protected static int exitCode;
	protected File mediaDir;
	protected ByteArrayOutputStream stdout;
	protected ByteArrayOutputStream stderr;

	/**
	 * Used to setup the exit handler
	 * @throws Exception Thrown if their are any problems
	 */
	@Before
	public void setUp() throws Exception {
		CLIManageAddons.setExitHandler(new IExitHandler() {
			@Override
			public void exit(int exitCode) {
				setExitCode(exitCode);
			}
		});

		setExitCode(0);
		mediaDir = FileHelper.createTmpDir("media");

		stdout = new ByteArrayOutputStream();
		stderr = new ByteArrayOutputStream();
		CLIManageAddons.stdout = new PrintStream(stdout);
		CLIManageAddons.stderr = new PrintStream(stderr);
	}

	/**
	 * Used to tidy up the controller before closing the test
	 * @throws Exception Thrown if their are any problems
	 */
	@After
	public void tearDown() throws Exception {
		stdout.close();
		stderr.close();
		FileHelper.delete(mediaDir);
	}

	private static void setExitCode(int code) {
		exitCode = code;
	}

}
