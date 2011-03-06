package org.stanwood.media.source.xbmc.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.renamer.TestRenameRecursive;
import org.stanwood.media.source.xbmc.XBMCAddonTestBase;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.util.FileHelper;

public class TestCLIManagerAddons extends XBMCAddonTestBase {

	protected static int exitCode;
	private File mediaDir;
	private ByteArrayOutputStream stdout;
	private ByteArrayOutputStream stderr;

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

	/**
	 * Test that the correct global help is generated
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testGlobalHelp() throws Exception {

		TestRenameRecursive.setupTestController(mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null);
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"--help"};
		CLIManageAddons.main(args);

		StringBuilder expected = new StringBuilder();
		expected.append("usage: xbmc-addons [--global-options] <command> [--command-options] [arguments]"+FileHelper.LS);
		expected.append(""+FileHelper.LS);
		expected.append("Global options:"+FileHelper.LS);
		expected.append("  --config_file, -c <info|debug|file>"+FileHelper.LS);
		expected.append("                                The location of the config file. If not present, attempts to load it from /etc/mediafetcher-conf.xml"+FileHelper.LS);
		expected.append("  --log_config, -l <file>       The log config mode [<INFO>|<DEBUG>|<log4j config file>]"+FileHelper.LS);
		expected.append("  --help, -h <arg>              Show the help"+FileHelper.LS);
		expected.append(""+FileHelper.LS);
		expected.append("Commands:"+FileHelper.LS);
		expected.append("  list                          lists the installed XBMC addons"+FileHelper.LS);

		Assert.assertEquals(expected.toString(),stdout.toString());
	}

	@Test
	public void testListCommandHelp() throws Exception {
		TestRenameRecursive.setupTestController(mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null);
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"list","--help"};
		CLIManageAddons.main(args);

		StringBuilder expected = new StringBuilder();
		Assert.assertEquals(expected.toString(),stdout.toString());
	}
}

