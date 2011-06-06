package org.stanwood.media.store.mp4;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.mp4.mp4v2cli.MP4v2CLIManager;

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
		super("mm-mp4-list-atoms",new ArrayList<Option>(),exitHandler, System.out, System.err);
	}

	@Override
	protected boolean processOptions(String[] args, CommandLine cmd) {
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
		try {
			IMP4Manager mp4Manager;
			MP4ITunesStore store = findStore();
			if (store!=null) {
				mp4Manager = store.getMP4Manager();
			}
			else {
				mp4Manager = new MP4v2CLIManager();
				mp4Manager.init();
			}

			info("Reading atoms...");
			List<IAtom> atoms = mp4Manager.listAtoms(mp4File);
			if (atoms==null || atoms.size()==0) {
				info("No metadata atoms found.");
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

	private MP4ITunesStore findStore() throws ConfigException {
		for (File f : getController().getMediaDirectiores()) {
			MediaDirectory dir = getController().getMediaDirectory(f);
			for (IStore store : dir.getStores()) {
				if (store instanceof MP4ITunesStore) {
					return (MP4ITunesStore) store;
				}
			}
		}
		return null;
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
		stdout.println("usage: mm-mp4-list-atoms [-h] [-c <file>] [-l <info|debug|file>] <mp4 file>");
		stdout.println("");
	}

}
