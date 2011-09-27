package org.stanwood.media.cli.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.SeenDatabase;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.mp4v2cli.MP4v2CLIManager;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.xml.XMLParserException;


public class CLIFixSeenDB extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLIFixSeenDB.class);

	private final static String ROOT_MEDIA_DIR_OPTION = "d"; //$NON-NLS-1$

	private static final List<Option> OPTIONS;
	private MediaDirectory rootMediaDir = null;

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;

	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(ROOT_MEDIA_DIR_OPTION, "dir",true,Messages.getString("CLIMediaManager.CLI_MEDIA_DIR_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(true);
		o.setArgName("directory"); //$NON-NLS-1$
		OPTIONS.add(o);
	}

	/**
	 * The entry point
	 * <p>
	 * It has the following usage:
	 * <code>
	 *  usage: mm-manager [-c <info|debug|file>] -d <directory> [-h] [-l <file>] [-t] [-u]
	 *
	 *  --noupdate, -u                If this option is present, then the XBMC addons won't be updated
	 *  --dir, -d <directory>         The directory to look for media. If not present use the current directory.
	 *  --test, -t                    If this option is present, then no changes are performed.
	 *  --config_file, -c <info|debug|file>
	 *                                The location of the config file. If not present, attempts to load it from /etc/mediafetcher-conf.xml
	 *  --log_config, -l <file>       The log config mode [<INFO>|<DEBUG>|<log4j config file>]
	 *  --help, -h                    Show the help
	 * </code>
	 * </p>
	 * @param args The arguments
	 */
	public static void main(String[] args) {
		if (exitHandler==null) {
			setExitHandler(new DefaultExitHandler());
		}

		CLIFixSeenDB ca = new CLIFixSeenDB(exitHandler);
		ca.launch(args);
	}


	private CLIFixSeenDB(IExitHandler exitHandler) {
		super("mm-manager",OPTIONS,exitHandler,stdout,stderr); //$NON-NLS-1$
	}


	/**
	 * This does the actual work of the tool.
	 * @return true if successful, otherwise false.
	 */
	@Override
	protected boolean run() {
		try  {
			for (IAction action : rootMediaDir.getActions()) {
				action.setTestMode(getController().isTestRun());
			}
			SeenDatabase seenDb = new SeenDatabase(getController().getConfigDir());
			seenDb.read(new NullProgressMonitor());
			IMP4Manager mp4Manager = new MP4v2CLIManager();
			mp4Manager.init(getController().getNativeFolder());

			File root = rootMediaDir.getMediaDirConfig().getMediaDir();
			List<File> files = FileHelper.listFiles(root);
			for (File f : files) {
				if (f.getAbsolutePath().endsWith(".m4v")) { //$NON-NLS-1$

					List<IAtom> atoms = mp4Manager.listAtoms(f);
					boolean found = false;
					for (IAtom atom : atoms) {
						if (atom.getName().equals("stik")) { //$NON-NLS-1$
							found = true;
							break;
						}
					}
					if (!found) {
						if (seenDb.isSeen(root, f)) {
							log.info(MessageFormat.format("Remove {0} from seen database as it as incomplete metadata.",f));
							seenDb.removeFile(root,f);
						}
					}
				}
			}
			seenDb.write(new NullProgressMonitor());

			return true;
		} catch (MP4Exception e) {
			log.error(e.getMessage(),e);
		} catch (ConfigException e) {
			log.error(e.getMessage(),e);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(),e);
		} catch (XMLParserException e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}


	/**
	 * Used to check the CLI options are valid
	 * @param cmd The CLI options
	 * @return true if valid, otherwise false.
	 */
	@Override
	protected boolean processOptions(String args[],CommandLine cmd) {
		rootMediaDir = null;

		if (cmd.hasOption(ROOT_MEDIA_DIR_OPTION) && cmd.getOptionValue(ROOT_MEDIA_DIR_OPTION) != null) {
			File dir = new File(cmd.getOptionValue(ROOT_MEDIA_DIR_OPTION));
			if (dir.isDirectory()) {
				try {
					getController().init(false);
					rootMediaDir = getController().getMediaDirectory(dir);
				} catch (ConfigException e) {
					fatal(e);
					return false;
				}
			} else {
				fatal(MessageFormat.format(Messages.getString("CLIMediaManager.MEDIA_DIR_NOT_WRITEABLE"),dir)); //$NON-NLS-1$
				return false;
			}
			if (rootMediaDir==null || !rootMediaDir.getMediaDirConfig().getMediaDir().exists()) {
				fatal(MessageFormat.format(Messages.getString("CLIMediaManager.MEDIA_DIR_NOT_EXIST"),dir)); //$NON-NLS-1$
				return false;
			}
		}

		return true;
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}
}
