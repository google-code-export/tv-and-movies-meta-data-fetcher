package org.stanwood.media.cli;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * This is a abstract base class for the sub commands of CLI commands
 */
public abstract class AbstractSubCLICommand extends BaseLauncher implements ICLICommand {

	private String description;
	private ICLICommand rootCommand;

	/**
	 * The constructor
	 * @param rootCommand The parent command
	 * @param name The name of the sub command
	 * @param description The description of the sub command
	 * @param options The sub command options
	 * @param stdout The standard output stream
	 * @param stderr The standard error stream
	 * @param exitHandler The exit handler
	 */
	public AbstractSubCLICommand(ICLICommand rootCommand,String name,String description,List<Option> options,IExitHandler exitHandler,PrintStream stdout,PrintStream stderr) {
		super(name,stdout,stderr,exitHandler);
		this.description = description;
		this.rootCommand = rootCommand;
	}

	/**
	 * Used to get the sub command description
	 * @return the sub command description
	 */
	public String getDescription() {
		return description;
	}

	@Override
	protected void printUsage(Options options, PrintStream stdout,PrintStream stderr) {
		stdout.println("usage: "+rootCommand.getName()+" [--global-options] " + getName() +" [--command-options]" + getPrintArguments());
		stdout.println("");
		stdout.println("Command Options:");
	}

	protected String getPrintArguments() {
		return "";
	}

	@Override
	protected boolean processOptionsInternal(String[] args, CommandLine cmd) {
		return processOptions(args, cmd);
	}

	protected abstract boolean processOptions(String[] args, CommandLine cmd);
}
