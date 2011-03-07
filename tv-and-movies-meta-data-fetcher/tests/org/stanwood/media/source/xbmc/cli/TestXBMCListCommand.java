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

		String args[] = new String[] {"list","--help"};
		CLIManageAddons.main(args);

		StringBuilder expected = new StringBuilder();
		Assert.assertEquals(expected.toString(),stdout.toString());
	}

}
