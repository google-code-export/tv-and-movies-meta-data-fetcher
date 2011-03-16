package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.AbstractSubCLICommand;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;

public class CLIManageAddons extends AbstractLauncher {

	private static IExitHandler exitHandler = null;
	private static final List<Option> OPTIONS;
	public static PrintStream stdout = System.out;
	public static PrintStream stderr = System.err;
	private List<AbstractSubCLICommand> subCommands;

	static {
		 OPTIONS = new ArrayList<Option>();


	}

	private AbstractSubCLICommand subCommand;
	protected int subExitCode;
	private List<String> subCommandArgs;

	private CLIManageAddons(IExitHandler exitHandler) {
		super("xbmc-addons", OPTIONS, exitHandler,stdout,stderr);

		subCommands = new ArrayList<AbstractSubCLICommand>();
		IExitHandler subExitHandler = new IExitHandler() {
			@Override
			public void exit(int exitCode) {
				subExitCode = exitCode;
				doExit(subExitCode);
			}
		};
		subCommands.add(new ListCommand(this,subExitHandler,stdout,stderr));
		subCommands.add(new UpdateCommand(this,subExitHandler,stdout,stderr));
	}

	@Override
	protected boolean run() {
		subCommand.init(getController());
		subCommand.launch(subCommandArgs.toArray(new String[subCommandArgs.size()]));

		return true;
	}

	@Override
	protected boolean processOptions(String args[], CommandLine cmd) {
		if (!checkArgs(cmd)) {
			return false;
		}

		boolean found = false;
		subCommandArgs = new ArrayList<String>();
		for (String a: args) {
			if (found) {
				subCommandArgs.add(a);
			}
			if (a.equals(subCommand.getName())) {
				found = true;
			}
		}
		return true;
	}

	protected boolean checkArgs(CommandLine cmd) {
		String[] args = cmd.getArgs();
		if (subCommand == null) {
			if (args.length>0) {
				fatal("Unkown sub-command or argument '" + args[0]+"'");
				return false;
			}
		}
		return true;
	}

	private AbstractSubCLICommand findCommand(String commandName) {
		for (AbstractSubCLICommand c : subCommands) {
			if (c.getName().equals(commandName)) {
				return c;
			}
		}
		return null;
	}

	@Override
	protected String checkSubCommand(String arg) {
		if (subCommand == null) {
			subCommand = findCommand(arg);
			if (subCommand!=null) {
				return arg;
			}
		}
		return null;
	}

	@Override
	protected void displayHelp(Options options,PrintStream stdout,PrintStream stderr) {
		printUsage(options,stdout,stderr);
		stdout.println("Global options:");
		printOptions(options,stdout,stderr);

		stdout.println("");
		stdout.println("Commands:");
		for (AbstractSubCLICommand command : subCommands) {
			stdout.print("  ");
			StringBuilder buffer = new StringBuilder();
			buffer.append(command.getName());
			while (buffer.length()<30) {
				buffer.append(" ");
			}
			stdout.print(buffer);
			if (buffer.length()>30) {
				stdout.print("\n                                ");
			}
			stdout.println(command.getDescription());
		}

	}

	@Override
	protected void printUsage(Options options, PrintStream stdout,PrintStream stderr) {
		stdout.println("usage: "+getName()+ " [--global-options] <command> [--command-options] [arguments]");
		stdout.println("");
	}

	public static void main(String[] args) {
		if (exitHandler==null) {
			setExitHandler(new DefaultExitHandler());
		}

		CLIManageAddons ca = new CLIManageAddons(exitHandler);
		ca.launch(args);
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}

}
