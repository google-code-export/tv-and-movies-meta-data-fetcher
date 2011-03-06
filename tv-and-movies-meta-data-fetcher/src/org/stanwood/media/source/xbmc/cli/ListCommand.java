package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.stanwood.media.cli.AbstractSubCLICommand;
import org.stanwood.media.cli.IExitHandler;

public class ListCommand extends AbstractSubCLICommand {

	private final static String NAME = "list";
	private final static String DESCRIPTION = "lists the installed XBMC addons";
	private final static List<Option>OPTIONS;

	static {
		OPTIONS = new ArrayList<Option>();
	}

	public ListCommand(IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(NAME, DESCRIPTION,OPTIONS,exitHandler,stdout,stderr);
	}

	@Override
	protected boolean run() {
		return false;
	}

	@Override
	protected boolean processOptions(CommandLine cmd) {
		return false;
	}

}
