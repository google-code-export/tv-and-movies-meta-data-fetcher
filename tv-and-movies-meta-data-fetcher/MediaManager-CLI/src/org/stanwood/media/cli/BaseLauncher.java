package org.stanwood.media.cli;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.stanwood.media.Controller;
import org.stanwood.media.logging.StanwoodException;
import org.stanwood.media.util.FileHelper;

/**
 * This is the base class of all CLI command launchers
 */
public abstract class BaseLauncher implements ICLICommand {

	private final static String HELP_OPTION_NAME = "h"; //$NON-NLS-1$

	private IExitHandler exitHandler;
	private PrintStream stdout;
	private PrintStream stderr;
	private Options options;
	private String name;

	private Option helpOption;

	/**
	 * The constructor
	 * @param name The name of the command
	 * @param stdout The standard output stream
	 * @param stderr The standard error stream
	 * @param exitHandler The exit handler
	 */
	public BaseLauncher(String name,PrintStream stdout, PrintStream stderr, IExitHandler exitHandler) {
		this.stdout = stdout;
		this.stderr = stderr;
		this.exitHandler = exitHandler;

		this.name = name;

		this.options = new Options();
		this.helpOption = new Option(HELP_OPTION_NAME,"help",false,Messages.getString("BaseLauncher.HELP_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		this.options.addOption(helpOption);
	}

	/**
	 * Called to setup stuff that depends on the controller
	 * @param controller The controller
	 */
	public void init(Controller controller) {

	}

	/**
	 * Used to get the options of the command
	 * @return The options
	 */
	public Options getOptions() {
		return options;
	}

	protected void addOption(Option o) {
		this.options.addOption(o);
	}

	@SuppressWarnings("unchecked")
	private Option getOption(String name) {
		if (name.startsWith("--")) { //$NON-NLS-1$
			name = name.substring(2);
		}
		else if (name.startsWith("-")) { //$NON-NLS-1$
			name = name.substring(1);
		}
		for (Option o : (Collection<Option>)options.getOptions()) {
			if (o.getLongOpt().equals(name) || o.getOpt().equals(name)) {
				return o;
			}
		}
		return null;
	}

	/**
	 * This should be called from the main method to launch the tool.
	 * @param args The args passed from the CLI
	 */
	public void launch(String args[]) {
		int argNum = 0;
		String subCommand = null;
		for (int i=0;i<args.length;i++) {
			String arg = args[i];
			if (arg.startsWith("-")) { //$NON-NLS-1$
				Option o = getOption(arg);
				if (o!=null) {
					if (o.hasArg()) {
						i++;
					}
				}
				else {
					handleBadOption(subCommand,arg);
				}
				// It's a option
			}
			else {
				if (argNum == 0) {
					subCommand = checkSubCommand(arg);
				}
				argNum++;
			}
		}


		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args,false);

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
		} catch (UnrecognizedOptionException e1) {
			fatal(e1.getMessage());
			return;
		} catch (MissingOptionException e1) {
			fatal(e1.getMessage());
			return;
		} catch (ParseException e1) {
			fatal(e1.getMessage());
			return;
		}
	}

	private void handleBadOption(String subCommand, String arg) {
		if (subCommand==null) {
			fatal(MessageFormat.format(Messages.getString("BaseLauncher.UNRECOGNIZED_OPTION"),arg)); //$NON-NLS-1$
		}
		else {
			handleBadSubCommandOption(options,arg);
		}
		return;
	}

	protected void handleBadSubCommandOption(Options options,String arg) {
		fatal(MessageFormat.format(Messages.getString("BaseLauncher.UNRECONGNIZED_SUB_OPTION") ,arg)); //$NON-NLS-1$
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
		if (e instanceof StanwoodException) {
			fatal(((StanwoodException)e).printException(null));
		}
		else if (e instanceof RuntimeException) {
			e.printStackTrace();
		}
		else {
			fatal(e.getMessage());
		}
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

	/**
	 * Called to display the commands help
	 * @param options The commands options
	 * @param stdout The standard output stream
	 * @param stderr The standard error stream
	 */
	public void displayHelp(Options options,PrintStream stdout,PrintStream stderr) {
		printUsage(options,stdout,stderr);
		printOptions(options,stdout,stderr);
	}

	protected void printUsage(Options options,PrintStream stdout,PrintStream stderr) {
		PrintWriter pw = new PrintWriter(stdout);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printUsage(pw, 80, getName(),options);
		pw.flush();
		stdout.println(""); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	protected void printOptions(Options options,PrintStream stdout,PrintStream stderr) {
		for (Option o : (Collection<Option>)options.getOptions()) {
			stdout.print("  "); //$NON-NLS-1$
			StringBuilder buffer = new StringBuilder();
			boolean doneLong = false;
			if (o.getLongOpt()!=null && o.getLongOpt().length()>0) {
				buffer.append("--"+o.getLongOpt()); //$NON-NLS-1$
				doneLong = true;
			}
			if (o.getOpt()!=null && o.getOpt().length()>0) {
				if (doneLong) {
					buffer.append(", "); //$NON-NLS-1$
				}
				buffer.append("-"+o.getOpt()); //$NON-NLS-1$
				if (o.hasArg() && o.getArgName()!=null && o.getArgName().length()>0) {
					buffer.append(" <" + o.getArgName()+">");  //$NON-NLS-1$//$NON-NLS-2$
				}
			}

			while (buffer.length()<30) {
				buffer.append(" "); //$NON-NLS-1$
			}
			stdout.print(buffer);
			if (buffer.length()>30) {
				stdout.print(FileHelper.LS+"                                "); //$NON-NLS-1$
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

	protected PrintStream getStdout() {
		return stdout;
	}

	protected PrintStream getStderr() {
		return stderr;
	}
}
