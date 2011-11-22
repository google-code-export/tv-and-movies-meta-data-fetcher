package org.stanwood.media.store.mp4;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.store.mp4.atomicparsley.MP4AtomicParsleyManager;

/**
 * <p>This is a CLI application used to print the atoms in a MP4 file</p>
 * <p>
 * It has the following usage:
 * <code>
 *  usage: mm-mp4-list-atoms [-h] [-c <file>] [-l <info|debug|file>] <mp4 file>
 *    --config_file, -c <file>      The location of the config file. If not present, attempts to load it from /etc/mediafetcher-conf.xml
 *    --log_config, -l <info|debug|file>
                                The log config mode [<INFO>|<DEBUG>|<log4j config file>]
 * --help, -h                    Show the help
 * </code>
 * </p>
 *
 */
public class CLIListAtoms extends AbstractLauncher {

	private static IExitHandler exitHandler = null;

	private File mp4File;

	/**
	 * The constructor
	 * @param exitHandler the exit handler
	 */
	public CLIListAtoms(IExitHandler exitHandler) {
		super("mm-mp4-list-atoms",new ArrayList<Option>(),exitHandler, System.out, System.err); //$NON-NLS-1$
	}

	@Override
	protected boolean processOptions(String[] args, CommandLine cmd) {
		if (cmd.getArgs().length==0) {
			fatal(Messages.getString("CLIListAtoms.MISSING_ARG")); //$NON-NLS-1$
			return false;
		}
		else if (cmd.getArgs().length>1) {
			fatal(Messages.getString("CLIListAtoms.MORE_THAN_ONE_ARG")); //$NON-NLS-1$
			return false;
		}
		else {
			mp4File=new File(cmd.getArgs()[0]);
		}

		return true;
	}

	@Override
	protected boolean run() {
		try {
			IMP4Manager mp4Manager = new MP4AtomicParsleyManager();
			mp4Manager.init(getController().getNativeFolder());

			info(Messages.getString("CLIListAtoms.READING_ATOMS")); //$NON-NLS-1$
			List<IAtom> atoms = mp4Manager.listAtoms(mp4File);
			if (atoms==null || atoms.size()==0) {
				info(Messages.getString("CLIListAtoms.NO_METADATA")); //$NON-NLS-1$
			}
			else {
				for (IAtom a : atoms) {
					if (a!=null) {
						getStdout().println(a.toString());
					}
				}
			}
		} catch (Exception e) {
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

	@Override
	protected void printUsage(Options options, PrintStream stdout, PrintStream stderr) {
		stdout.println(Messages.getString("CLIListAtoms.USAGE")+"mm-mp4-list-atoms [-h] [-c <file>] [-l <info|debug|file>] <mp4 file>");  //$NON-NLS-1$//$NON-NLS-2$
		stdout.println(""); //$NON-NLS-1$
	}

}
