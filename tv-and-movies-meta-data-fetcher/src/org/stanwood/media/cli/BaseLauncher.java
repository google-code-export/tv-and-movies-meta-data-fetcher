package org.stanwood.media.cli;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public abstract class BaseLauncher implements ICLICommand {

	private final static String HELP_OPTION_NAME = "h";

	private IExitHandler exitHandler;
	private PrintStream stdout;
	private PrintStream stderr;
	private Options options;
	private String name;

	private Option helpOption;

	public BaseLauncher(String name,PrintStream stdout, PrintStream stderr, IExitHandler exitHandler) {
		init(stdout,stderr,exitHandler);

		this.name = name;

		this.options = new Options();
		this.helpOption = new Option(HELP_OPTION_NAME,"help",false,"Show the help");
		this.options.addOption(helpOption);
	}

	public void init(PrintStream stdout, PrintStream stderr,IExitHandler exitHandler) {
		this.stdout = stdout;
		this.stderr = stderr;
		this.exitHandler = exitHandler;
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
				fatal("Invalid command line parameters");
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
		displayHelp(options,stderr,stdout);
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

	@Override
	public String getName() {
		return name;
	}

	protected void displayHelp(Options options,PrintStream stdout,PrintStream stderr) {
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(stdout);
			formatter.printHelp(pw,80, getName(),"",options ,0,0,"",true);
			pw.flush();
		}
		finally {
			pw.close();
		}
	}

	protected abstract boolean processOptionsInternal(String args[],CommandLine cmd);

	/**
	 * This is executed to make the tool perform its function and should be extended.
	 * @return True if executed without problems, otherwise false
	 */
	protected abstract boolean run();
}
