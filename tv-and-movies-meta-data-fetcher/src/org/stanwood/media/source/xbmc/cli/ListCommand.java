package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.cli.AbstractSubCLICommand;
import org.stanwood.media.cli.ICLICommand;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.renamer.Controller;
import org.stanwood.media.source.xbmc.XBMCAddonManager;

public class ListCommand extends AbstractSubCLICommand {

	private final static Log log = LogFactory.getLog(ListCommand.class);

	private final static String NAME = "list";
	private final static String DESCRIPTION = "lists the installed XBMC addons";
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	private XBMCAddonManager xbmcMgr;

	public ListCommand(ICLICommand rootCommand,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(rootCommand,NAME, DESCRIPTION,OPTIONS,exitHandler,stdout,stderr);
	}

	@Override
	public void init(Controller controller) {
//		try {
//		controller.init();
//	}
//	catch (ConfigException e) {
//		log.error("Unable to setup the XBMC addon manager",e);
//		return false;
//	}

		xbmcMgr = controller.getXBMCAddonManager();
	}

	@Override
	protected boolean run() {
		xbmcMgr.listAddons();
		return false;
	}

	@Override
	protected boolean processOptions(String args[],CommandLine cmd) {

		return true;
	}



}
