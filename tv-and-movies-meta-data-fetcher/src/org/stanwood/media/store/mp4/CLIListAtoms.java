package org.stanwood.media.store.mp4;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.stanwood.media.cli.BaseLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;

/**
 * <p>This is a CLI application used to print the atoms in a MP4 file</p>
 * <p>
 * It has the following usage:
 * <code>
 *  usage: mm-mp4-list-atoms [-h] <mp4 file>
 *
 * --help, -h                    Show the help
 * </code>
 * </p>
 *
 */
public class CLIListAtoms extends BaseLauncher {

	private static IExitHandler exitHandler = null;

	private File mp4File;

	/**
	 * The constructor
	 * @param exitHandler the exit handler
	 */
	public CLIListAtoms(IExitHandler exitHandler) {
		super("mm-mp4-list-atoms", System.out, System.err,exitHandler);
	}

	@Override
	protected boolean processOptionsInternal(String[] args, CommandLine cmd) {
		if (cmd.getArgs().length==0) {
			fatal("Missing argument, expected mp4 file name");
			return false;
		}
		else if (cmd.getArgs().length>1) {
			fatal("More than one argument, only allowed one mp4 filename as a argument");
			return false;
		}
		else {
			mp4File=new File(cmd.getArgs()[0]);
		}

		return true;
	}

	@Override
	protected boolean run() {
		MP4Manager mp4Manager = new MP4Manager();
		try {
			info("Reading atoms...");
			List<Atom> atoms = mp4Manager.listAtoms(mp4File);
			for (Atom a : atoms) {
				String value = a.getValue();
				if (value==null) {
					value = "<unknown>";
				}
				getStdout().println(a.getName()+" = " + value);
			}
		} catch (MP4Exception e) {
			fatal(e);
			return false;
		}
		return true;
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}

	/**
	 * The entry point to the application. For details see the class documentation.
	 *
	 * @param args The arguments.
	 */
	public static void main(String[] args) {
		if (exitHandler==null) {
			setExitHandler(new DefaultExitHandler());
		}

		CLIListAtoms ca = new CLIListAtoms(exitHandler);
		ca.launch(args);
	}

	/* (non-Javadoc)
	 * @see org.stanwood.media.cli.BaseLauncher#printUsage(org.apache.commons.cli.Options, java.io.PrintStream, java.io.PrintStream)
	 */
	@Override
	protected void printUsage(Options options, PrintStream stdout, PrintStream stderr) {
		stdout.println("usage: mm-mp4-list-atoms [-h] <mp4 file>");
		stdout.println("");
	}


}
