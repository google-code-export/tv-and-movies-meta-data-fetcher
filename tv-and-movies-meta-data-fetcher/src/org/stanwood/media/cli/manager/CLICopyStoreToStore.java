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
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.rename.Token;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
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
 *  usage: mm-manager [-c <file>] -d <directory> [-h] [-l <info|debug|file>] [-t] [-u] <media files...>
 *
 *  --fromStore, f				  A required option specifying the store ID to copy from.
 *  --toStore, o				  A required option specifying the store ID to copy to.
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
public class CLICopyStoreToStore extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLICopyStoreToStore.class);

	private final static String ROOT_MEDIA_DIR_OPTION = "d";
	private final static String TEST_OPTION = "t";
	private static final String NOUPDATE_OPTION = "u";
	private final static String TO_STORE_OPTION = "o";
	private final static String FROM_STORE_OPTION = "f";

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

		o = new Option(TO_STORE_OPTION,"toStore",true,"The store to write to");
		o.setArgName("store ID");
		o.setRequired(true);
		OPTIONS.add(o);

		o = new Option(FROM_STORE_OPTION,"fromStore",true,"The store to read from");
		o.setArgName("store ID");
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
		super("mm-copy-store-to-store",OPTIONS,exitHandler,stdout,stderr);
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
					Film f = fromStore.getFilm(rootMediaDir, mediaFile);
					if (f==null) {
						fatal("Unable to find file for file: " + mediaFile);
						return false;
					}
					Integer part =getFilmPart(mediaFile,f);
					toStore.cacheFilm(dirConfig.getMediaDir(), mediaFile, f, part);
				}
				else {
					Episode episode = fromStore.getEpisode(rootMediaDir, mediaFile);
					toStore.cacheEpisode(rootMediaDir.getMediaDirConfig().getMediaDir(), mediaFile, episode);
				}
				log.info("Store "+toStore.getClass().getName()+" updated for file '"+mediaFile+"'");
			}


			return true;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}

	protected Integer getFilmPart(File file, Film film) {
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
			ReversePatternSearchStrategy rp = new ReversePatternSearchStrategy(Token.TITLE, false);
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

		fromStore = findStoreById(cmd.getOptionValue(FROM_STORE_OPTION));
		if (fromStore==null) {
			return false;
		}
		toStore = findStoreById(cmd.getOptionValue(TO_STORE_OPTION));
		if (fromStore==null) {
			return false;
		}


		if (cmd.getArgs().length==0) {
			fatal("Missing argument giving media files to move");
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
					fatal("Unable to find file: "+file);
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
		fatal("Unable to find store with ID: " + id);
		return null;
	}


	protected String getPrintArguments() {
		return " <media files...>";
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}
}
