package org.stanwood.media.source.xbmc.cli;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.AbstractSubCLICommand;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.util.FileHelper;

/**
 * The main XBMC addon manager command line command
 */
public class CLIManageAddons extends AbstractLauncher {

	private static IExitHandler exitHandler = null;
	private static final List<Option> OPTIONS;
	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;
	private List<AbstractSubCLICommand> subCommands;

	static {
		 OPTIONS = new ArrayList<Option>();
	}

	private AbstractSubCLICommand subCommand;
	protected int subExitCode;
	private List<String> subCommandArgs;

	private CLIManageAddons(IExitHandler exitHandler) {
		super("mm-xbmc", OPTIONS, exitHandler,stdout,stderr); //$NON-NLS-1$

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
		subCommands.add(new InstallCommand(this,subExitHandler,stdout,stderr));
		subCommands.add(new RemoveCommand(this,subExitHandler,stdout,stderr));
	}

	@Override
	protected boolean run() {
		if (subCommand!=null) {
			subCommand.init(getController());
			subCommand.launch(subCommandArgs.toArray(new String[subCommandArgs.size()]));
		}

		return true;
	}

	@Override
	protected boolean processOptions(String args[], CommandLine cmd) {
		if (!checkArgs(cmd)) {
			return false;
		}
		try {
			getController().init(false);
		} catch (ConfigException e) {
			fatal(e);
			return false;
		}
		boolean found = false;
		if (subCommand==null) {
			fatal(Messages.getString("CLIManageAddons.NO_SUB_COMMAND")); //$NON-NLS-1$
			return false;
		}
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
				fatal(MessageFormat.format(Messages.getString("CLIManageAddons.UNKNOWN_SUB_COMMAND_ARG"),args[0])); //$NON-NLS-1$
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
	protected void handleBadSubCommandOption(Options options,String arg) {
		stderr.println(MessageFormat.format(Messages.getString("CLIManageAddons.UNKNOWN_SUB_CMD_OPT"),arg)); //$NON-NLS-1$
		subCommand.displayHelp(subCommand.getOptions(),stdout,stderr);
		doExit(1);
	}

	/**@{inheritDoc} */
	@Override
	public void displayHelp(Options options,PrintStream stdout,PrintStream stderr) {
		printUsage(options,stdout,stderr);
		stdout.println(Messages.getString("CLIManageAddons.GLOBAL_OPTS")); //$NON-NLS-1$
		printOptions(options,stdout,stderr);

		stdout.println(""); //$NON-NLS-1$
		stdout.println(Messages.getString("CLIManageAddons.COMMANDS")); //$NON-NLS-1$
		for (AbstractSubCLICommand command : subCommands) {
			stdout.print("  "); //$NON-NLS-1$
			StringBuilder buffer = new StringBuilder();
			buffer.append(command.getName());
			while (buffer.length()<30) {
				buffer.append(" "); //$NON-NLS-1$
			}
			stdout.print(buffer);
			if (buffer.length()>30) {
				stdout.print(FileHelper.LS+"                                "); //$NON-NLS-1$
			}
			stdout.println(command.getDescription());
		}

	}

	@Override
	protected void printUsage(Options options, PrintStream stdout,PrintStream stderr) {
		stdout.println(Messages.getString("CLIManageAddons.USAGE")+" "+getName()+ " [--global-options] <command> [--command-options] [arguments]");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		stdout.println(""); //$NON-NLS-1$
	}

	/**
	 * <p>
	 * This CLI launcher command is used to manage XBMC addons. It has sub commands and args
	 * listed below.
	 * </p>
	 * <code>
	 * usage: mm-xbmc [--global-options] <command> [--command-options] [arguments]
	 *
	 * Global options:
	 *   --version, -v                 Display the version
	 *   --config_file, -c <file>
	 *                                 The location of the config file. If not present, attempts to load it from /etc/mediamanager-conf.xml
	 *   --log_config, -l <info|debug|file>
	 *                                 The log config mode [<INFO>|<DEBUG>|<log4j config file>]
	 *   --help, -h                    Show the help
	 *
	 * Commands:
	 *   list                          lists the installed XBMC addons
	 *   update                        Update the installed XBMC addons to the latest versions
	 *   install                       Install a new XBMC addon
	 *   remove                        Remove a installed XBMC addons
	 * </code>
	 * @param args The arguments passed to the program from the command line
	 */
	public static void main(String[] args) {
		if (exitHandler==null) {
			setExitHandler(new DefaultExitHandler());
		}

		CLIManageAddons ca = new CLIManageAddons(exitHandler);
		ca.launch(args);
	}

	/**
	 * Used to se a exit handler
	 * @param handler The exit handler
	 */
	public static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}

	/**
	 * Set the stdout that this command should use. Mainly used by tests
	 * @param stream The stdout stream
	 */
	public static void setStdout(PrintStream stream) {
		stdout = stream;
	}

	/**
	 * Set the stderr that this command should use. Mainly used by tests
	 * @param stream The stderr stream
	 */
	public static void setStderr(PrintStream stream) {
		stderr = stream;
	}


}
