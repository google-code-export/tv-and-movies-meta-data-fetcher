package org.stanwood.media.source.xbmc.cli;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.Controller;
import org.stanwood.media.cli.manager.TestCLIMediaManager;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.server.commands.StringBuilderCommandLogger;
import org.stanwood.media.server.commands.XBMCListAddonsCommand;
import org.stanwood.media.server.commands.XBMCListAddonsResult;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.xbmc.XBMCSource;
import org.stanwood.media.util.FileHelper;

/**
 * Used to test the XBMC addon list command
 */
@SuppressWarnings({ "nls" })
public class TestXBMCListCommand extends BaseCLITest {

	/**
	 * USed to test the help output of the mm-xbmc list command
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testListCommandHelp() throws Exception {
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class.getName()+"#metadata.themoviedb.org",new HashMap<String,String>(),null,"");
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
		expected.append("usage: mm-xbmc [--global-options] list [--command-options]"+FileHelper.LS);
		expected.append(""+FileHelper.LS);
		expected.append("Command Options:"+FileHelper.LS);
		expected.append("  --help, -h                    Show the help"+FileHelper.LS);
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

	/** {@inheritDoc} */
	@Override
	public void reset() throws Exception {
		super.reset();
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class.getName()+"#metadata.themoviedb.org",new HashMap<String,String>(),null,"");
	}

	/**
	 * Used to test a list of addons
	 * @throws Exception Thrown if their are any problems
	 */
	@Test
	public void testList() throws Exception {
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class.getName()+"#metadata.themoviedb.org",new HashMap<String,String>(),null,"");
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
		expected.append("Downloaded plugin 'metadata.common.hdtrailers.net' version=1.0.8"+FileHelper.LS);
		expected.append("Installed plugin 'metadata.common.hdtrailers.net'"+FileHelper.LS);
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

	@Test
	public void testListAsJson() throws Exception {
		ConfigReader config = TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class.getName()+"#metadata.themoviedb.org",new HashMap<String,String>(),null,"");
		LogSetupHelper.initLogging(stdout,stderr);

		Controller controller = new Controller(config);
		controller.init(false);

		XBMCListAddonsCommand cmd = new XBMCListAddonsCommand(controller);
		StringBuilderCommandLogger logger = new StringBuilderCommandLogger();
		XBMCListAddonsResult result = cmd.execute(logger, new NullProgressMonitor());

		Assert.assertEquals(331,result.getAddons().size());

		String expected = FileHelper.readFileContents(TestXBMCListCommand.class.getResourceAsStream("expectedJsonList.txt"));
		Assert.assertEquals(expected.trim(), result.toJson(true).trim());
	}

	protected void assertPluginList(String filename) throws IOException {
		// Get a initial list of plugins
		String args[] = new String[] {"--log_config","INFO","list"};
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
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class.getName()+"#metadata.themoviedb.org",new HashMap<String,String>(),null,"");
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
		expected.append("Unrecognized subcommand option: --blah"+FileHelper.LS);
		Assert.assertEquals(expected.toString(),stderr.toString());

		expected = new StringBuilder();
		expected.append("usage: mm-xbmc [--global-options] list [--command-options]"+FileHelper.LS);
		expected.append(""+FileHelper.LS);
		expected.append("Command Options:"+FileHelper.LS);
		expected.append("  --help, -h                    Show the help"+FileHelper.LS);
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

	/**
	 * Test unexpected arguments
	 * @throws Exception Thrown if their is a problem
	 */
	@Test
	public void testUnexpectedArgument() throws Exception {
		TestCLIMediaManager.setupTestController(false,mediaDir,"%t.%x",Mode.FILM,XBMCSource.class.getName()+"#metadata.themoviedb.org",new HashMap<String,String>(),null,"");
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
		expected.append("Unkown sub-command argument 'blah'"+FileHelper.LS);
		Assert.assertEquals(expected.toString(),stderr.toString());

		expected = new StringBuilder();
		expected.append("usage: mm-xbmc [--global-options] list [--command-options]"+FileHelper.LS);
		expected.append(""+FileHelper.LS);
		expected.append("Command Options:"+FileHelper.LS);
		expected.append("  --help, -h                    Show the help"+FileHelper.LS);
		Assert.assertEquals(expected.toString(),stdout.toString());
	}
}
