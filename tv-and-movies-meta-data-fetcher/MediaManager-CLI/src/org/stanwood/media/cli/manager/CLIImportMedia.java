package org.stanwood.media.cli.manager;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.cli.AbstractLauncher;
import org.stanwood.media.cli.DefaultExitHandler;
import org.stanwood.media.cli.IExitHandler;
import org.stanwood.media.logging.StanwoodException;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.source.xbmc.XBMCException;
import org.stanwood.media.source.xbmc.XBMCUpdaterException;
import org.stanwood.media.source.xbmc.updater.IConsole;
import org.stanwood.media.util.FileHelper;

public class CLIImportMedia extends AbstractLauncher {

	private final static Log log = LogFactory.getLog(CLIImportMedia.class);

	private final static String TEST_OPTION = "t"; //$NON-NLS-1$
	private static final List<Option> OPTIONS;
	private static final String NOUPDATE_OPTION = "u"; //$NON-NLS-1$

	private boolean xbmcUpdate = true;
	private List<File> files;
	private HashSet<String> extensions;
	private static PrintStream stdout = System.out;
	private static PrintStream stderr = System.err;


	private static IExitHandler exitHandler = null;

	static {
		OPTIONS = new ArrayList<Option>();

		Option o = new Option(TEST_OPTION,"test",false,Messages.getString("CLICopyToMediaDir.CLI_MEDIA_TEST_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
		o.setRequired(false);
		OPTIONS.add(o);

		o = new Option(NOUPDATE_OPTION,"noupdate",false,Messages.getString("CLICopyToMediaDir.CLI_MEDIA_NOUPDATE_DESC")); //$NON-NLS-1$ //$NON-NLS-2$
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
			for (File mediaDirLoc :  getController().getMediaDirectiores()) {
				for (IAction action : getController().getMediaDirectory(mediaDirLoc).getActions()) {
					action.setTestMode(getController().isTestRun());
				}
			}

			doUpdateCheck();

//			List<File>newFiles = new ArrayList<File>();
			for (File file : files) {
				for (File mediaDirLoc :  getController().getMediaDirectiores()) {
					MediaDirectory dir = getController().getMediaDirectory(mediaDirLoc);
					SearchResult result = dir.searchForVideoId(dir.getMediaDirConfig(), file);
					if (result==null) {
						log.error(MessageFormat.format("Unable to find media details for file {0}",file));
					}
					else {
						File toFile =new File(dir.getMediaDirConfig().getMediaDir(),file.getName());
						if (getController().isTestRun()) {
							log.info(MessageFormat.format("Test run so aborting move of ''{0}'' to media directory ''{1}'' and performing actions...",file,toFile.getParent()));
						}
						else {
							log.info(MessageFormat.format("Moving ''{0}'' to media directory ''{1}'' and performing actions...",file,toFile.getParent()));
							FileHelper.move(file, toFile);
							performActions(toFile,dir);
							break;
						}
					}
				}
			}

			return true;
		} catch (StanwoodException e) {
			log.error(e.getMessage(),e);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return false;
	}

	private void performActions(File toFile, MediaDirectory dir) {
//		List<IAction> actions = new ArrayList<IAction>(dir.getActions());
//		boolean found = false;
//		for (IAction action : actions) {
//			if (action instanceof RenameAction) {
//				found = true;
//			}
//		}
//
//		if (!found) {
//			actions.add(0,new RenameAction());
//		}
//
//		ActionPerformer renamer = new ActionPerformer(getController().getConfigDir(),getController().getNativeFolder(),actions,rootMediaDir,rootMediaDir.getMediaDirConfig().getExtensions(),getController().isTestRun());
//		renamer.performActions(newFiles,new HashSet<File>(),new NullProgressMonitor());
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
				for (File mediaDirLoc :  getController().getMediaDirectiores()) {
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
							files.add(f);
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
