package org.stanwood.media.source.xbmc.cli;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.stanwood.media.logging.LogSetupHelper;
import org.stanwood.media.model.Mode;
import org.stanwood.media.renamer.TestRenameRecursive;
import org.stanwood.media.source.xbmc.XBMCSource;

public class TestXBMCListCommand extends BaseCLITest {

	@Test
	public void testListCommandHelp() throws Exception {
		TestRenameRecursive.setupTestController(mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null);
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
		expected.append("usage: xbmc-addons [--global-options] list [--command-options]\n");
		expected.append("\n");
		expected.append("Command Options:\n");
		expected.append("  --help, -h                    Show the help\n");
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

	@Test
	public void testList() throws Exception {
		TestRenameRecursive.setupTestController(mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null);
		LogSetupHelper.initLogging(stdout,stderr);

		String args[] = new String[] {"--log_config","INFO","list"};
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
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

	@Test
	public void testUnkownOption() throws Exception {
		TestRenameRecursive.setupTestController(mediaDir,"%t.%x",Mode.FILM,XBMCSource.class,new HashMap<String,String>(),null);
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
		expected.append("Unrecognized option: --blah\n");
		Assert.assertEquals(expected.toString(),stderr.toString());

		expected = new StringBuilder();
		Assert.assertEquals(expected.toString(),stdout.toString());
	}
}
