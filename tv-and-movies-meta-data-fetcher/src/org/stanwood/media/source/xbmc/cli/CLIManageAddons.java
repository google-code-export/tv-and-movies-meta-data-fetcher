package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
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
	private static List<AbstractSubCLICommand> SUBCOMMANDS;

	static {
		 OPTIONS = new ArrayList<Option>();

		 SUBCOMMANDS = new ArrayList<AbstractSubCLICommand>();
		 SUBCOMMANDS.add(new ListCommand(exitHandler,stdout,stderr));
	}

	private AbstractSubCLICommand subCommand;
	protected int subExitCode;
	private List<String> subCommandArgs;

	private CLIManageAddons(IExitHandler exitHandler) {
		super("xbmc-addons", OPTIONS, exitHandler,stdout,stderr);
	}

	@Override
	protected boolean run() {
		subCommand.init(stdout,stderr,new IExitHandler() {
			@Override
			public void exit(int exitCode) {
				subExitCode = exitCode;
			}
		});
		subCommand.launch(subCommandArgs.toArray(new String[subCommandArgs.size()]));

		return subExitCode == 0;
	}

	@Override
	protected boolean processOptions(String args[], CommandLine cmd) {
//			if (subCommand == null) {
//				fatal("Unkown sub-command or option '" + commandName+"'");
//				return false;
//			}

		List<String>commandArgs = new ArrayList<String>();
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

	private AbstractSubCLICommand findCommand(String commandName) {
		for (AbstractSubCLICommand c : SUBCOMMANDS) {
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
		stdout.println("usage: "+getName()+ " [--global-options] <command> [--command-options] [arguments]");
		stdout.println("");
		stdout.println("Global options:");
		for (Option o : (Collection<Option>)options.getOptions()) {
			stdout.print("  ");
			StringBuilder buffer = new StringBuilder();
			boolean doneLong = false;
			if (o.getLongOpt()!=null && o.getLongOpt().length()>0) {
				buffer.append("--"+o.getLongOpt());
				doneLong = true;
			}
			if (o.getOpt()!=null && o.getOpt().length()>0) {
				if (doneLong) {
					buffer.append(", ");
				}
				buffer.append("-"+o.getOpt());
				if (o.getArgName()!=null && o.getArgName().length()>0) {
					buffer.append(" <" + o.getArgName()+">");
				}
			}

			while (buffer.length()<30) {
				buffer.append(" ");
			}
			stdout.print(buffer);
			if (buffer.length()>30) {
				stdout.print("\n                                ");
			}
			stdout.print(o.getDescription());
			stdout.println();
		}

		stdout.println("");
		stdout.println("Commands:");
		for (AbstractSubCLICommand command : SUBCOMMANDS) {
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
