package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.stanwood.media.cli.AbstractSubCLICommand;
import org.stanwood.media.cli.ICLICommand;
import org.stanwood.media.cli.IExitHandler;

public class UpdateCommand extends AbstractSubCLICommand {

	private final static String NAME = "update";
	private final static String DESCRIPTION = "Update the installed XBMC addons to the latest versions";
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	public UpdateCommand(ICLICommand rootCommand,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(rootCommand,NAME, DESCRIPTION,OPTIONS,exitHandler,stdout,stderr);
	}

	@Override
	protected boolean run() {
		return false;
	}

	@Override
	protected boolean processOptions(String args[],CommandLine cmd) {
		return false;
	}

}
