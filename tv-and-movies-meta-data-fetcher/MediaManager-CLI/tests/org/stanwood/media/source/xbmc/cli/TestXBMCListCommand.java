package org.stanwood.media.source.xbmc.cli;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.cli.manager.TestCLIMediaManager;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the XBMC addon list command
 */
@SuppressWarnings({ "unchecked", "nls" })
public class TestXBMCListCommand extends BaseCLITest {

	/**
	 * USed to test the help output of the mm-xbmc list command
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testListCommandHelp() throws Exception {
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null,"");
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"--log_config","INFO","list","--help"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			Assert.assertEquals(0,e.getExitCode());
		}

		StringBuilder expected = new StringBuilder();
		Assert.assertEquals(expected.toString(), stderr.toString());

		expected = new StringBuilder();
		expected.append("usage: mm-xbmc [--global-options] list [--command-options]\n");
		expected.append("\n");
		expected.append("Command Options:\n");
		expected.append("  --help, -h                    Show the help\n");
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void reset() throws Exception {
		super.reset();
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null,"");
	}

	/**
	 * Used to test a list of addons
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testList() throws Exception {
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null,"");
		LogSetupHelper.initLogging(stdout,stderr);

		// Check inital list of plugins
		assertPluginList("expectedAddonList.txt");

		// Update a plugin
		reset();
		String[] args = new String[] {"--log_config","INFO","update","metadata.common.hdtrailers.net"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			if (e.getExitCode()!=0) {
				System.out.println(stdout);
				System.err.println(stderr);
			}
			Assert.assertEquals(0,e.getExitCode());
		}

		StringBuilder expected = new StringBuilder();
		expected.append("Downloaded plugin 'metadata.common.hdtrailers.net' version=1.0.6\n");
		expected.append("Installed plugin 'metadata.common.hdtrailers.net'\n");
		Assert.assertEquals(expected.toString(), stdout.toString());
		Assert.assertEquals("",stderr.toString());

		// Assert that the plugins were updated
		reset();
		assertPluginList("expectedAddonList1.txt");

		// Remove a plugin
		reset();
		args = new String[] {"--log_config","INFO","remove","metadata.common.hdtrailers.net"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			if (e.getExitCode()!=0) {
				System.out.println(stdout);
				System.err.println(stderr);
			}
			Assert.assertEquals(0,e.getExitCode());
		}

		// Assert that the plugins were removed
		reset();
		assertPluginList("expectedAddonList2.txt");

		// Add a plugin
		reset();
		args = new String[] {"--log_config","INFO","install","metadata.themoviedb.org"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			if (e.getExitCode()!=0) {
				System.out.println(stdout);
				System.err.println(stderr);
			}
			Assert.assertEquals(0,e.getExitCode());
		}

		// Assert that the plugins was installed
		reset();
		assertPluginList("expectedAddonList3.txt");

	}

	protected void assertPluginList(String filename) throws IOException {
		// Get a initial list of plugins
		String args[] = new String[] {"--log_config","INFO","list"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			Assert.assertEquals(0,e.getExitCode());
		}

		String expected = FileHelper.readFileContents(TestXBMCListCommand.class.getResourceAsStream(filename));
		Assert.assertEquals(expected, stdout.toString());

		Assert.assertEquals("",stderr.toString());
	}

	/**
	 * Used to test known options
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testUnkownOption() throws Exception {
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null,"");
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"list","--blah"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			Assert.assertEquals(1,e.getExitCode());
		}

		StringBuilder expected = new StringBuilder();
		expected.append("Unrecognized subcommand option: --blah\n");
		Assert.assertEquals(expected.toString(),stderr.toString());

		expected = new StringBuilder();
		expected.append("usage: mm-xbmc [--global-options] list [--command-options]\n");
		expected.append("\n");
		expected.append("Command Options:\n");
		expected.append("  --help, -h                    Show the help\n");
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

	/**
	 * Test unexpected arguments
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testUnexpectedArgument() throws Exception {
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null,"");
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"list","blah"};
		try {
			CLIManageAddons.main(args);
			Assert.fail("No exit code");
		}
		catch (ExitException e) {
			Assert.assertEquals(1,e.getExitCode());
		}

		StringBuilder expected = new StringBuilder();
		expected.append("Unkown sub-command argument 'blah'\n");
		Assert.assertEquals(expected.toString(),stderr.toString());

		expected = new StringBuilder();
		expected.append("usage: mm-xbmc [--global-options] list [--command-options]\n");
		expected.append("\n");
		expected.append("Command Options:\n");
		expected.append("  --help, -h                    Show the help\n");
		Assert.assertEquals(expected.toString(),stdout.toString());
	}
}