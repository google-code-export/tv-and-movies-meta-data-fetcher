package org.stanwood.media.cli.manager;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.ActionPerformer;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.source.xbmc.updater.IConsole;

/**
 * <p>
 * This is a command line launcher that is used to managed a media directory. It reads
 * the configuration file to work out which sources, stores and actions are to be used
 * with media directory. Then the actions are performed on the media directory.
 * </p>
 * <p>
 * It has the following usage:
 * <code>
 *  usage: mm-manager [-c <file>] -d <directory> [-h] [-l <info|debug|file>] [-t] [-u]
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
 */
public class CLIMediaManager extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLIMediaManager.class);

	private final static String ROOT_MEDIA_DIR_OPTION = "d";
	private final static String TEST_OPTION = "t";

	private static final List<Option> OPTIONS;

	private static final String NOUPDATE_OPTION = "u";

	private MediaDirectory rootMediaDir = null;
	private boolean xbmcUpdate = true;

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;


	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(ROOT_MEDIA_DIR_OPTION, "dir",true,"The directory to look for media. If not present use the current directory.");
		o.setRequired(true);
		o.setArgName("directory");
		OPTIONS.add(o);

		o = new Option(TEST_OPTION,"test",false,"If this option is present, then no changes are performed.");
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(NOUPDATE_OPTION,"noupdate",false,"If this option is present, then the XBMC addons won't be updated");
		o.setRequired(false);
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

		CLIMediaManager ca = new CLIMediaManager(exitHandler);
		ca.launch(args);
	}


	private CLIMediaManager(IExitHandler exitHandler) {
		super("mm-manager",OPTIONS,exitHandler,stdout,stderr);
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

			doUpdateCheck();

			ActionPerformer renamer = new ActionPerformer(getController().getNativeFolder(),rootMediaDir.getActions(),rootMediaDir,rootMediaDir.getMediaDirConfig().getExtensions(),getController().isTestRun());

			renamer.performActions();
			return true;
		} catch (ActionException e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}

	private void doUpdateCheck() {
		if ((!getController().isTestRun()) && xbmcUpdate) {
			try {
				log.info("Checking for updated XBMC plugins....");
				int count = getController().getXBMCAddonManager().getUpdater().update(new IConsole() {
					@Override
					public void error(String error) {
						log.info(error);
					}

					@Override
					public void info(String info) {
						log.info(info);
					}
				});
				if (count>0 ) {
					log.info("Downloaded and installed "+count+" updates");
				}
			} catch (XBMCUpdaterException e) {
				log.error("Unable to update XBMC addons",e);
			} catch (XBMCException e) {
				log.error("Unable to update XBMC addons",e);
			}
		}
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
					getController().init(cmd.hasOption(TEST_OPTION));
					rootMediaDir = getController().getMediaDirectory(dir);
				} catch (ConfigException e) {
					fatal(e);
					return false;
				}
			} else {
				fatal("Media directory '"+dir+"' must be a writable directory");
				return false;
			}
			if (rootMediaDir==null || !rootMediaDir.getMediaDirConfig().getMediaDir().exists()) {
				fatal("Media directory '" + dir +"' does not exist.");
				return false;
			}
		}

		if (cmd.hasOption(NOUPDATE_OPTION)) {
			xbmcUpdate = false;
		}
		return true;
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}
}
