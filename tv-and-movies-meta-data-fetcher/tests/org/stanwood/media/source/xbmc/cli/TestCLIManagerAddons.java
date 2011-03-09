package org.stanwood.media.source.xbmc.cli;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.renamer.TestRenameRecursive;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the XBMC manager cli command
 */
public class TestCLIManagerAddons extends  BaseCLITest {

	/**
	 * Test that the correct global help is generated
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testGlobalHelp() throws Exception {

		TestRenameRecursive.setupTestController(mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null);
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"--help"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			Assert.assertEquals(0,e.getExitCode());
		}

		StringBuilder expected = new StringBuilder();
		appendHelp(expected);

		Assert.assertEquals(expected.toString(),stdout.toString());


	}

	protected void appendHelp(StringBuilder expected) {
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
		expected.append("  update                        Update the installed XBMC addons to the latest versions"+FileHelper.LS);
	}

	/**
	 * Used to check what happens when their is a invalid option
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testUnkownOption() throws Exception {
		TestRenameRecursive.setupTestController(mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null);
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"--blah"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			Assert.assertEquals(1,e.getExitCode());
		}

		StringBuilder expected = new StringBuilder();
		expected.append("Unrecognized option: --blah\n");
		Assert.assertEquals(expected.toString(), stderr.toString());

		expected = new StringBuilder();
		appendHelp(expected);
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

	/**
	 * Used to check what happens when their is a invalid argument
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testUnkownSubCommand() throws Exception {
		TestRenameRecursive.setupTestController(mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null);
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"blah"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			Assert.assertEquals(1,e.getExitCode());
		}

		StringBuilder expected = new StringBuilder();
		expected.append("Unkown sub-command or argument 'blah'\n");
		Assert.assertEquals(expected.toString(), stderr.toString());

		expected = new StringBuilder();
		appendHelp(expected);
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

}

