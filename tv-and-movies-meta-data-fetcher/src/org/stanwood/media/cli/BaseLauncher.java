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

	private final static String HELP_OPTION = "h";

	private IExitHandler exitHandler;
	private PrintStream stdout;
	private PrintStream stderr;
	private Options options;
	private String name;

	public BaseLauncher(String name,PrintStream stdout, PrintStream stderr, IExitHandler exitHandler) {
		this.stdout = stdout;
		this.stderr = stderr;
		this.exitHandler = exitHandler;
		this.name = name;

		this.options = new Options();
		this.options.addOption(new Option(HELP_OPTION,"help",false,"Show the help"));
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
			if (hasOptionHelp(HELP_OPTION,cmd)) {
				displayHelp(options,stdout,stderr);
				doExit(0);
				return;
			} else if (processOptionsInternal(cmd)) {
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

	protected boolean hasOptionHelp(String helpOption, CommandLine cmd) {
		return cmd.hasOption(helpOption);
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

	protected abstract boolean processOptionsInternal(CommandLine cmd);

	/**
	 * This is executed to make the tool perform its function and should be extended.
	 * @return True if executed without problems, otherwise false
	 */
	protected abstract boolean run();
}
