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
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.cli.manager.Messages;
import org.stanwood.media.logging.StanwoodException;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.setup.ConfigException;
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
	private static final List<Option> OPTIONS;
	private static final String NOUPDATE_OPTION = "u"; //$NON-NLS-1$

	private boolean xbmcUpdate = true;
	private List<File> files;
	private HashSet<String> extensions;

	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;
	private boolean useDefaults = true;


	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(TEST_OPTION,"test",false,Messages.getString("CLICopyToMediaDir.CLI_MEDIA_TEST_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(NOUPDATE_OPTION,"noupdate",false,Messages.getString("CLICopyToMediaDir.CLI_MEDIA_NOUPDATE_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(USE_DEFAULT_OPTION,"dontUseDefaults",false,"Don't use default media directiores"); //$NON-NLS-1$ //$NON-NLS-2$
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
		super("mm-move-into-media-directory",OPTIONS,exitHandler,stdout,stderr); //$NON-NLS-1$
	}


	/**
	 * This does the actual work of the tool.
	 * @return true if successful, otherwise false.
	 */
	@Override
	protected boolean run() {
		try  {
			Map<File,List<File>>newFiles = new HashMap<File,List<File>>();
			for (File mediaDirLoc :  getController().getMediaDirectories()) {
				for (IAction action : getController().getMediaDirectory(mediaDirLoc).getActions()) {
					action.setTestMode(getController().isTestRun());
				}
				newFiles.put(mediaDirLoc, new ArrayList<File>());
			}

			doUpdateCheck();

			MediaSearcher searcher = new MediaSearcher(getController());
			for (File file : files) {
				SearchResult result = searcher.lookupMedia(file);
				if (result==null) {
					log.error(MessageFormat.format("Unable to find media details for file {0}",file));
					continue;
				}

				MediaDirectory dir = findMediaDir(file,result);
				if (dir==null) {
					log.error(MessageFormat.format("Unable to find media directory for file {0}",file));
					continue;
				}

				moveFileToMediaDir(file, newFiles, dir.getMediaDirConfig().getMediaDir());
			}

			for (Entry<File,List<File>> e : newFiles.entrySet()) {
				performActions(e.getValue(),getController().getMediaDirectory(e.getKey()));
			}

			return true;
		} catch (StanwoodException e) {
			log.error(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}

	private MediaDirectory findMediaDir(File file, SearchResult result) throws ConfigException, StoreException, MalformedURLException, IOException {
		if (result.getMode()==Mode.FILM) {
			List<MediaDirectory> mediaDirs = getController().getMediaDirectories(result.getMode());
			if (useDefaults) {
				for (MediaDirectory mediaDir :  mediaDirs) {
					if (mediaDir.getMediaDirConfig().getMode()==result.getMode() && mediaDir.getMediaDirConfig().isDefaultForMode()) {
						return mediaDir;
					}
				}
			}
		}
		else {
			List<MediaDirectory> mediaDirs = getController().getMediaDirectories(result.getMode());

			// Check to see if their is already a media directory that contains the show
			for (MediaDirectory mediaDir :  mediaDirs) {
				for (IStore store : mediaDir.getStores()) {
					if (store.getShow(mediaDir.getMediaDirConfig().getMediaDir(), file, result.getId())!=null) {
						return mediaDir;
					}
				}
			}

			if (useDefaults) {
				// Used a default media directory
				for (MediaDirectory mediaDir :  mediaDirs) {
					if (mediaDir.getMediaDirConfig().getMode()==result.getMode() && mediaDir.getMediaDirConfig().isDefaultForMode()) {
						return mediaDir;
					}
				}
			}
		}
		return null;
	}


	private void moveFileToMediaDir(File file,Map<File,List<File>>newFiles,File mediaDirLoc) throws IOException {
		File toFile =new File(mediaDirLoc,file.getName());
		if (getController().isTestRun()) {
			log.info(MessageFormat.format("Test run so aborting move of ''{0}'' to media directory ''{1}'' and performing actions...",file,toFile.getParent()));
		}
		else {
			log.info(MessageFormat.format("Moving ''{0}'' to media directory ''{1}'' and performing actions...",file,toFile.getParent()));
			FileHelper.move(file, toFile);
			newFiles.get(mediaDirLoc).add(toFile);
		}
	}

	private void performActions(List<File> newFiles, MediaDirectory dir) throws ActionException, ConfigException {
		List<IAction> actions = new ArrayList<IAction>(dir.getActions());
		boolean found = false;
		for (IAction action : actions) {
			if (action instanceof RenameAction) {
				found = true;
			}
		}

		if (!found) {
			actions.add(0,new RenameAction());
		}

		ActionPerformer renamer = new ActionPerformer(getController().getConfigDir(),getController().getNativeFolder(),actions,dir,dir.getMediaDirConfig().getExtensions(),getController().isTestRun());
		renamer.performActions(newFiles,new HashSet<File>(),new NullProgressMonitor());
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

		if (cmd.getArgs().length==0) {
			fatal(Messages.getString("CLICopyToMediaDir.MISSING_ARG")); //$NON-NLS-1$
			return false;
		}
		else {
			extensions = new HashSet<String>();
			try {
				for (File mediaDirLoc :  getController().getMediaDirectories()) {
					MediaDirectory mediaDir = getController().getMediaDirectory(mediaDirLoc);
					extensions.addAll(mediaDir.getMediaDirConfig().getExtensions());
				}
			} catch (ConfigException e) {
				log.error("Unable to read configuration",e);
				return false;
			}
			files = new ArrayList<File>();
			for (String s : cmd.getArgs()) {
				File f = new File(s);
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
				log.info (MessageFormat.format("Found {0} media files...",files.size()));
			}
			else {
				log.info("Unable to find any media files");
				return false;
			}
		}

		if (cmd.hasOption(NOUPDATE_OPTION)) {
			xbmcUpdate = false;
		}
		if (cmd.hasOption(USE_DEFAULT_OPTION)) {
			useDefaults = false;
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
