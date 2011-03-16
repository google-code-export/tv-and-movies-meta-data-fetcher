package org.stanwood.media.cli;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.stanwood.media.renamer.Controller;

public abstract class BaseLauncher implements ICLICommand {

	private final static String HELP_OPTION_NAME = "h";

	private IExitHandler exitHandler;
	private PrintStream stdout;
	private PrintStream stderr;
	private Options options;
	private String name;

	private Option helpOption;

	public BaseLauncher(String name,PrintStream stdout, PrintStream stderr, IExitHandler exitHandler) {
		this.stdout = stdout;
		this.stderr = stderr;
		this.exitHandler = exitHandler;

		this.name = name;

		this.options = new Options();
		this.helpOption = new Option(HELP_OPTION_NAME,"help",false,"Show the help");
		this.options.addOption(helpOption);
	}

	public void init(Controller controller) {

	}

	protected void addOption(Option o) {
		this.options.addOption(o);
	}

	/**
	 * This should be called from the main method to launch the tool.
	 * @param args The args passed from the CLI
	 */
	public void launch(String args[]) {

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
			String subCommand = null;
			if (cmd.getArgs().length>0) {
				subCommand = checkSubCommand(cmd.getArgs()[0]);
			}

			boolean displayHelp = shouldDisplayHelp(args, cmd, subCommand);
			if (displayHelp) {
				displayHelp(options,stdout,stderr);
				doExit(0);
				return;
			}
			else if (processOptionsInternal(args,cmd)) {
				if (run()) {
					doExit(0);
					return;
				}
				else {
					doExit(0);
					return;
				}
			} else {
//				fatal("Invalid command line parameters");
				displayHelp(options,stdout,stderr);
				doExit(1);
				return;
			}
		} catch (ParseException e1) {
			fatal(e1.getMessage());
			return;
		}
	}

	protected boolean shouldDisplayHelp(String[] args, CommandLine cmd,
			String subCommand) {
		boolean displayHelp = false;
		if (cmd.hasOption(HELP_OPTION_NAME)) {
			displayHelp = true;
			if (subCommand !=null) {
				boolean foundHelp = false;
				for (String a : args) {
					if (a.equals(helpOption.getLongOpt()) || a.equals(helpOption.getOpt())) {
						foundHelp = true;
					}
					if (a.equals(subCommand)) {
						if (!foundHelp) {
							displayHelp = false;
							break;
						}
					}
				}
			}

		}
		return displayHelp;
	}

	protected String checkSubCommand(String string) {
		return null;
	}

	/**
	 * This will exit the application
	 * @param code The exit code
	 */
	public void doExit(int code) {
		exitHandler.exit(code);
	}

	/**
	 * Called to issue a warning message
	 * @param msg The message
	 */
	protected void warn(String msg) {
		stdout.println(msg);
	}

	/**
	 * Called to issue a fatal message and exit
	 * @param msg The message
	 */
	protected void fatal(String msg) {
		stderr.println(msg);
		displayHelp(options,stdout,stderr);
		doExit(1);
	}

	protected void fatal(Exception e) {
		fatal(e.getMessage());
	}

	/**
	 * Called to issue a info message
	 * @param msg The message
	 */
	protected void info(String msg) {
		stdout.println(msg);
	}

	/**
	 * Used to get the command name
	 * @return the command name
	 */
	@Override
	public String getName() {
		return name;
	}

	protected void displayHelp(Options options,PrintStream stdout,PrintStream stderr) {
		printUsage(options,stdout,stderr);
		printOptions(options,stdout,stderr);
	}

	protected void printUsage(Options options,PrintStream stdout,PrintStream stderr) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(stdout);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printUsage(pw, 80, getName(),options);
			pw.flush();
		}
		finally {
			pw.close();
		}
		stdout.println("");
	}

	@SuppressWarnings("unchecked")
	protected void printOptions(Options options,PrintStream stdout,PrintStream stderr) {
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
				if (o.hasArg() && o.getArgName()!=null && o.getArgName().length()>0) {
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
	}


	protected abstract boolean processOptionsInternal(String args[],CommandLine cmd);

	/**
	 * This is executed to make the tool perform its function and should be extended.
	 * @return True if executed without problems, otherwise false
	 */
	protected abstract boolean run();
}
