package org.stanwood.media.cli.importer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.ActionPerformer;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.cli.manager.Messages;
import org.stanwood.media.logging.StanwoodException;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.Mode;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.search.MediaSearchResult;
import org.stanwood.media.search.MediaSearcher;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.WatchDirConfig;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.source.xbmc.updater.IConsole;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.util.FileHelper;

public class CLIImportMedia extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLIImportMedia.class);

	private final static String TEST_OPTION = "t"; //$NON-NLS-1$
	private final static String USE_DEFAULT_OPTION = "d"; //$NON-NLS-1$
	private final static String DELETE_NON_MEDIA_OPTION = "e"; //$NON-NLS-1$
	private final static String ACTIONS_OPTION = "a"; //$NON-NLS-1$
	private static final List<Option> OPTIONS;
	private static final String NOUPDATE_OPTION = "u"; //$NON-NLS-1$

	private boolean xbmcUpdate = true;
	private List<File> files;
	private HashSet<String> extensions;

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;
	private boolean useDefaults = true;
	private boolean deleteNonMediaFiles = false;
	private boolean doActions = false;


	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(TEST_OPTION,"test",false,Messages.getString("CLICopyToMediaDir.CLI_MEDIA_TEST_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(NOUPDATE_OPTION,"noupdate",false,Messages.getString("CLICopyToMediaDir.CLI_MEDIA_NOUPDATE_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(USE_DEFAULT_OPTION,"dontUseDefaults",false,Messages.getString("CLIImportMedia.DONT_USE_DEFAULT_MEDIA_DIRS")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(DELETE_NON_MEDIA_OPTION,"deleteNonMedia",false,Messages.getString("CLIImportMedia.DELETE_NON_MEDIA")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(ACTIONS_OPTION,"actions",false,Messages.getString("CLIImportMedia.EXECUTE_ACTIONS")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
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

		CLIImportMedia ca = new CLIImportMedia(exitHandler);
		ca.launch(args);
	}


	private CLIImportMedia(IExitHandler exitHandler) {
		super("mm-import-media",OPTIONS,exitHandler,stdout,stderr); //$NON-NLS-1$
	}


	/**
	 * This does the actual work of the tool.
	 * @return true if successful, otherwise false.
	 */
	@Override
	protected boolean run() {
		try  {
			Map<File, List<File>> newFiles = setupStoresAndActions();

			doUpdateCheck();

			MediaSearcher searcher = new MediaSearcher(getController());
			for (File file : files) {
				MediaSearchResult result;
				try {
					result = searcher.lookupMedia(file,useDefaults);
					if (result==null) {
						log.error(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_FIND_MEDIA_DETIALS"),file)); //$NON-NLS-1$
						continue;
					}
				}
				catch (StanwoodException e) {
					log.error(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_FIND_MEDIA_DETIALS"),file),e); //$NON-NLS-1$
					continue;
				}
				moveFileToMediaDir(file, newFiles, result,searcher);
			}

			if (doActions) {
				for (Entry<File,List<File>> e : newFiles.entrySet()) {
					performActions(e.getValue(),getController().getMediaDirectory(e.getKey()));
				}
			}

			if (deleteNonMediaFiles) {
				cleanUpNonMediaFiles();
			}

			return true;
		} catch (StanwoodException e) {
			log.error(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}


	protected Map<File, List<File>> setupStoresAndActions()
			throws ConfigException, StoreException {
		Map<File,List<File>>newFiles = new HashMap<File,List<File>>();
		for (File mediaDirLoc :  getController().getMediaDirectories()) {
			for (IAction action : getController().getMediaDirectory(mediaDirLoc).getActions()) {
				action.setTestMode(getController().isTestRun());
			}
			newFiles.put(mediaDirLoc, new ArrayList<File>());
		}

		for (File mediaDirLoc :  getController().getMediaDirectories()) {
			for (IStore store : getController().getMediaDirectory(mediaDirLoc).getStores()) {
				store.init(getController(),getController().getNativeFolder());
			}
		}
		return newFiles;
	}

	private void cleanUpNonMediaFiles() {
		for (WatchDirConfig wd : getController().getWatchDirectories()) {
			List<File> dirs = FileHelper.listDirectories(wd.getWatchDir());
			for (File d : dirs) {
				if (d !=null && !d.equals(wd.getWatchDir()) && !dirContainsMedia(d)) {
					if (getController().isTestRun()) {
						log.info(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_DELETE_FOLDER_TEST_MODE"), d)); //$NON-NLS-1$
					}
					else {
						log.info(MessageFormat.format(Messages.getString("CLIImportMedia.DELETEING_FOLDER"), d)); //$NON-NLS-1$
						try {
							FileHelper.delete(d);
						} catch (IOException e) {
							log.error(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_DELETE_FOLDER"),d),e); //$NON-NLS-1$
						}
					}

				}
			}
		}
	}

	private boolean dirContainsMedia(File d) {
		File files[] = d.listFiles();
		if (files!=null) {
			for (File f : files) {
				if (f.isDirectory()) {
					if (dirContainsMedia(f)) {
						return true;
					}
				}
				else {
					if (extensions.contains(FileHelper.getExtension(f))) {
						return true;
					}
				}
			}
		}
		return false;
	}


	private MediaDirectory findMediaDir(File file, IVideo video) throws ConfigException, StoreException, MalformedURLException, IOException {
		if (video instanceof IFilm) {
			List<MediaDirectory> mediaDirs = getController().getMediaDirectories(Mode.FILM);
			if (mediaDirs.size()==1) {
				return mediaDirs.get(0);
			}
			if (useDefaults) {
				for (MediaDirectory mediaDir :  mediaDirs) {
					if (mediaDir.getMediaDirConfig().getMode()==Mode.FILM && mediaDir.getMediaDirConfig().isDefaultForMode()) {
						return mediaDir;
					}
				}
			}
		}
		else {
			IEpisode episode = (IEpisode)video;
			List<MediaDirectory> mediaDirs = getController().getMediaDirectories(Mode.TV_SHOW);

			// Check to see if their is already a media directory that contains the show
			for (MediaDirectory mediaDir :  mediaDirs) {
				for (IStore store : mediaDir.getStores()) {
					if (store.getShow(mediaDir.getMediaDirConfig().getMediaDir(), file,episode.getSeason().getShow().getShowId())!=null) {
						return mediaDir;
					}
				}
			}

			if (useDefaults) {
				// Used a default media directory
				for (MediaDirectory mediaDir :  mediaDirs) {
					if (mediaDir.getMediaDirConfig().getMode()==Mode.TV_SHOW && mediaDir.getMediaDirConfig().isDefaultForMode()) {
						return mediaDir;
					}
				}
			}
		}
		return null;
	}

	private void moveFileToMediaDir(File file,final Map<File,List<File>>newFiles,MediaSearchResult result, MediaSearcher searcher) throws IOException, StoreException, ConfigException {
		MediaDirectory dir = findMediaDir(file, result.getVideo());
		if (dir==null) {
			throw new ConfigException(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_FIND_MEDIA_DIR"),file)); //$NON-NLS-1$
		}
		final File mediaDirLoc = dir.getMediaDirConfig().getMediaDir();
		log.info(MessageFormat.format(Messages.getString("CLIImportMedia.MOVING_MEDIA_MSG"),file,mediaDirLoc)); //$NON-NLS-1$
		RenameAction ra = new RenameAction();
		try {
			ra.init(dir);
			ra.setTestMode(getController().isTestRun());

			if (result.getVideo() instanceof IFilm) {
				Integer part = searcher.getFilmPart(result.getMediaDirectory(), file, (IFilm)result.getVideo());
				ra.perform(dir, (IFilm)result.getVideo(), file,part, new IActionEventHandler() {
					@Override
					public void sendEventRenamedFile(File oldName, File newName)
							throws ActionException {
						newFiles.get(mediaDirLoc).add(newName);
					}

					@Override
					public void sendEventNewFile(File file) throws ActionException {
						newFiles.get(mediaDirLoc).add(file);
					}

					@Override
					public void sendEventDeletedFile(File file) throws ActionException {
					}
				});
			}
			else {
				ra.perform(dir, (IEpisode)result.getVideo(), file, new IActionEventHandler() {
					@Override
					public void sendEventRenamedFile(File oldName, File newName)
							throws ActionException {
						newFiles.get(mediaDirLoc).add(newName);
					}

					@Override
					public void sendEventNewFile(File file) throws ActionException {
						newFiles.get(mediaDirLoc).add(file);
					}

					@Override
					public void sendEventDeletedFile(File file) throws ActionException {
					}
				});
			}
			ra.finished(dir);
		}
		catch (ActionException e) {
			log.error(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_TO_MOVE_FILE"),file),e); //$NON-NLS-1$
		}
	}

	private void performActions(List<File> newFiles, MediaDirectory dir) throws ActionException, ConfigException {
		log.info(MessageFormat.format(Messages.getString("CLIImportMedia.PERFORMING_ACTIONS"), dir.getMediaDirConfig().getMediaDir())); //$NON-NLS-1$
		List<IAction> actions = new ArrayList<IAction>(dir.getActions());
		ActionPerformer actionPerformer = new ActionPerformer(getController(),actions,dir,dir.getMediaDirConfig().getExtensions());
		actionPerformer.performActions(newFiles,new HashSet<File>(),new NullProgressMonitor());
	}


	private void doUpdateCheck() {
		if ((!getController().isTestRun()) && xbmcUpdate) {
			try {
				log.info(Messages.getString("CLICopyToMediaDir.CHECKING_UPTODATE")); //$NON-NLS-1$
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
					log.info(MessageFormat.format(Messages.getString("CLICopyToMediaDir.DOWNLOAD_INSTALL_UPDATE"),count)); //$NON-NLS-1$
				}
			} catch (XBMCUpdaterException e) {
				log.error(Messages.getString("CLICopyToMediaDir.UNABLE_TO_UPDATE"),e); //$NON-NLS-1$
			} catch (XBMCException e) {
				log.error(Messages.getString("CLICopyToMediaDir.UNABLE_TO_UPDATE"),e); //$NON-NLS-1$
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
		try {
			getController().init(cmd.hasOption(TEST_OPTION));
		} catch (ConfigException e) {
			fatal(e);
			return false;
		}


		extensions = new HashSet<String>();
		try {
			for (File mediaDirLoc :  getController().getMediaDirectories()) {
				MediaDirectory mediaDir = getController().getMediaDirectory(mediaDirLoc);
				extensions.addAll(mediaDir.getMediaDirConfig().getExtensions());
			}
		} catch (ConfigException e) {
			log.error(Messages.getString("CLIImportMedia.UNABLE_READ_CONFIG"),e); //$NON-NLS-1$
			return false;
		}
		files = new ArrayList<File>();
		for (WatchDirConfig c : getController().getWatchDirectories()) {
			File f = c.getWatchDir();
			if (f.isDirectory()) {
				for (File f2 : FileHelper.listFiles(f)) {
					if (isAllowedMediaFileType(f2)) {
						files.add(f2);
					}
				}
			}
			else {
				files.add(f);
			}
		}
		if (files.size()>0) {
			log.info (MessageFormat.format(Messages.getString("CLIImportMedia.FOUND_MEDIA_FILES"),files.size())); //$NON-NLS-1$
		}
		else {
			log.info(Messages.getString("CLIImportMedia.UNABLE_FIND_MEDIA")); //$NON-NLS-1$
			return false;
		}


		if (cmd.hasOption(NOUPDATE_OPTION)) {
			xbmcUpdate = false;
		}
		if (cmd.hasOption(USE_DEFAULT_OPTION)) {
			useDefaults = false;
		}

		if (cmd.hasOption(DELETE_NON_MEDIA_OPTION)) {
			deleteNonMediaFiles = true;
		}

		if (cmd.hasOption(ACTIONS_OPTION)) {
			doActions = true;
		}
		return true;
	}

	private boolean isAllowedMediaFileType(File f2) {
		if (extensions.contains(FileHelper.getExtension(f2))) {
			return true;
		}
		return false;
	}


	protected String getPrintArguments() {
		return Messages.getString("CLICopyToMediaDir.MEDIA_FILES"); //$NON-NLS-1$
	}

	static synchronized void setExitHandler(IExitHandler handler) {
		exitHandler = handler;
	}
}
