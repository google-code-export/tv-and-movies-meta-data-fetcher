package org.stanwood.media.cli.manager;

import java.io.File;
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
import org.stanwood.media.actions.rename.Token;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.search.ReversePatternSearchStrategy;
import org.stanwood.media.search.SearchDetails;
import org.stanwood.media.search.SearchHelper;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.source.xbmc.updater.IConsole;
import org.stanwood.media.store.IStore;
import org.stanwood.media.util.FileHelper;

/**
 * <p>
 * The mm-copy-store-to-store command is used to copy media file information from one store to another.
 * </p>
 * <p>
 * It has the following usage:
 * <code>
 *  usage: mm-manager [-c <file>] -d <directory> [-h] [-l <info|debug|file>] [-t] [-v] [-u] <media files...>
 *
 *  --version, -v                 Display the version
 *  --fromStore, f				  A required option specifying the store ID to copy from.
 *  --toStore, o				  A required option specifying the store ID to copy to.
 *  --noupdate, -u                If this option is present, then the XBMC addons won't be updated
 *  --dir, -d <directory>         The directory to look for media. If not present use the current directory.
 *  --test, -t                    If this option is present, then no changes are performed.
 *  --config_file, -c <info|debug|file>
 *                                The location of the config file. If not present, attempts to load it from /etc/mediamanager-conf.xml
 *  --log_config, -l <file>       The log config mode [<INFO>|<DEBUG>|<log4j config file>]
 *  --help, -h                    Show the help
 * </code>
 * </p>
 */
public class CLICopyStoreToStore extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLICopyStoreToStore.class);

	private final static String ROOT_MEDIA_DIR_OPTION = "d"; //$NON-NLS-1$
	private final static String TEST_OPTION = "t"; //$NON-NLS-1$
	private static final String NOUPDATE_OPTION = "u"; //$NON-NLS-1$
	private final static String TO_STORE_OPTION = "o"; //$NON-NLS-1$
	private final static String FROM_STORE_OPTION = "f"; //$NON-NLS-1$

	private static final List<Option> OPTIONS;

	private MediaDirectory rootMediaDir = null;
	private boolean xbmcUpdate = true;

	private List<File> files;

	private IStore fromStore;

	private IStore toStore;

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;

	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(ROOT_MEDIA_DIR_OPTION, "dir",true,Messages.getString("CLICopyStoreToStore.CLI_MEDIA_DIR_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(true);
		o.setArgName("directory"); //$NON-NLS-1$
		OPTIONS.add(o);

		o = new Option(TEST_OPTION,"test",false,Messages.getString("CLICopyStoreToStore.CLI_TEST_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(NOUPDATE_OPTION,"noupdate",false,Messages.getString("CLICopyStoreToStore.CLI_OPTION_NO_UPDATE_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(TO_STORE_OPTION,"toStore",true,Messages.getString("CLICopyStoreToStore.CLI_STORE_TO_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setArgName("store ID"); //$NON-NLS-1$
		o.setRequired(true);
		OPTIONS.add(o);

		o = new Option(FROM_STORE_OPTION,"fromStore",true,Messages.getString("CLICopyStoreToStore.CLI_STORE_FROM_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setArgName("store ID"); //$NON-NLS-1$
		o.setRequired(true);
		OPTIONS.add(o);
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

		CLICopyStoreToStore ca = new CLICopyStoreToStore(exitHandler);
		ca.launch(args);
	}


	private CLICopyStoreToStore(IExitHandler exitHandler) {
		super("mm-copy-store-to-store",OPTIONS,exitHandler,stdout,stderr); //$NON-NLS-1$
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

			MediaDirConfig dirConfig = rootMediaDir.getMediaDirConfig();
			for (File mediaFile : files) {
				if (dirConfig.getMode()==Mode.FILM) {
					IFilm f = fromStore.getFilm(rootMediaDir, mediaFile);
					if (f==null) {
						fatal(MessageFormat.format(Messages.getString("CLICopyStoreToStore.UNABLE_FIND_FILM"),mediaFile)); //$NON-NLS-1$
						return false;
					}
					Integer part =getFilmPart(mediaFile,f);
					toStore.cacheFilm(dirConfig.getMediaDir(), mediaFile, f, part);
				}
				else {
					IEpisode episode = fromStore.getEpisode(rootMediaDir, mediaFile);
					if (episode==null) {
						fatal(MessageFormat.format("Unable to find episode {0} in media directory {1}",mediaFile,rootMediaDir));
						return false;
					}
					toStore.cacheEpisode(rootMediaDir.getMediaDirConfig().getMediaDir(), mediaFile, episode);
				}
				log.info(MessageFormat.format(Messages.getString("CLICopyStoreToStore.STORE_UPDATED"),toStore.getClass().getName(),mediaFile)); //$NON-NLS-1$
			}


			return true;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}

	protected Integer getFilmPart(File file, IFilm film) {
		Integer part = null;
		if (film.getFiles()!=null) {
			for (VideoFile vf : film.getFiles()) {
				if (vf.getLocation().equals(file)) {
					part = vf.getPart();
				}
			}
		}
		if (part == null) {
			part = SearchHelper.extractPart(new StringBuilder(file.getName()));
		}
		if (part == null) {
			ReversePatternSearchStrategy rp = new ReversePatternSearchStrategy(Token.TITLE, false,true);
			SearchDetails result = rp.getSearch(file, rootMediaDir.getMediaDirConfig().getMediaDir(), rootMediaDir.getMediaDirConfig().getPattern(),rootMediaDir);
			if (result!=null) {
				part = result.getPart();
			}
		}
		return part;
	}

	private void doUpdateCheck() {
		if ((!getController().isTestRun()) && xbmcUpdate) {
			try {
				log.info(Messages.getString("CLICopyStoreToStore.CHECKING_FOR_UPDATES")); //$NON-NLS-1$
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
					log.info(MessageFormat.format(Messages.getString("CLICopyStoreToStore.DOWNLOAD_INSTALL_UPDATE"),count)); //$NON-NLS-1$
				}
			} catch (XBMCUpdaterException e) {
				log.error(Messages.getString("CLICopyStoreToStore.UNABLE_TO_UPDATE_XBMC_ADDONS"),e); //$NON-NLS-1$
			} catch (XBMCException e) {
				log.error(Messages.getString("CLICopyStoreToStore.UNABLE_TO_UPDATE_XBMC_ADDONS"),e); //$NON-NLS-1$
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
				fatal(MessageFormat.format(Messages.getString("CLICopyStoreToStore.MEDIA_DIR_NOT_WRITABLE"),dir)); //$NON-NLS-1$
				return false;
			}
			if (rootMediaDir==null || !rootMediaDir.getMediaDirConfig().getMediaDir().exists()) {
				fatal(MessageFormat.format(Messages.getString("CLICopyStoreToStore.MEDIA_DIR_DOES_NOT_EXIST"),dir)); //$NON-NLS-1$
				return false;
			}
		}

		fromStore = findStoreById(cmd.getOptionValue(FROM_STORE_OPTION));
		if (fromStore==null) {
			return false;
		}
		toStore = findStoreById(cmd.getOptionValue(TO_STORE_OPTION));
		if (fromStore==null) {
			return false;
		}


		if (cmd.getArgs().length==0) {
			fatal(Messages.getString("CLICopyStoreToStore.MISSING_ARG")); //$NON-NLS-1$
			return false;
		}
		else {
			files = new ArrayList<File>();
			for (String s : cmd.getArgs()) {
				File file = null;
				if (s.startsWith(File.separator)) {
					file = new File(s);
				}
				else {
					file = new File(FileHelper.getWorkingDirectory(),s);
				}
				if (!file.exists()) {
					fatal(MessageFormat.format(Messages.getString("CLICopyStoreToStore.UNABLE_FIND_FILE"),file)); //$NON-NLS-1$
					return false;
				}
				files.add(file);
			}
		}

		if (cmd.hasOption(NOUPDATE_OPTION)) {
			xbmcUpdate = false;
		}
		return true;
	}

	private IStore findStoreById(String id) {
		for (IStore store : rootMediaDir.getStores()) {
			if (store.getClass().getName().equals(id)) {
				return store;
			}
		}
		fatal(MessageFormat.format(Messages.getString("CLICopyStoreToStore.UNABLE_FIND_STORE"),id)); //$NON-NLS-1$
		return null;
	}


	protected String getPrintArguments() {
		return Messages.getString("CLICopyStoreToStore.MEDIA_FILES"); //$NON-NLS-1$
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}
}
