package org.stanwood.media.cli;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


public abstract class AbstractSubCLICommand extends BaseLauncher implements ICLICommand {

	private String name;
	private String description;
	private IExitHandler exitHandler = null;
	private ICLICommand rootCommand;

	public AbstractSubCLICommand(ICLICommand rootCommand,String name,String description,List<Option> options,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(name,stdout,stderr,exitHandler);
		this.description = description;
		this.rootCommand = rootCommand;
	}

	public String getDescription() {
		return description;
	}

	@Override
	protected void printUsage(Options options, PrintStream stdout,PrintStream stderr) {
		stdout.println("usage: "+rootCommand.getName()+" [--global-options] " + getName() +" [--command-options]");
		stdout.println("");
		stdout.println("Command Options:");
	}

	@Override
	protected boolean processOptionsInternal(String[] args, CommandLine cmd) {
		return processOptions(args, cmd);
	}

	protected abstract boolean processOptions(String[] args, CommandLine cmd);
}
