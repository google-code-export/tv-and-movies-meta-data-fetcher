/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.server.commands;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.ActionPerformer;
import org.stanwood.media.actions.IAction;
import org.stanwood.media.actions.IActionEventHandler;
import org.stanwood.media.actions.rename.RenameAction;
import org.stanwood.media.cli.importer.RenamedEntry;
import org.stanwood.media.cli.manager.Messages;
import org.stanwood.media.logging.StanwoodException;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.Mode;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.progress.NullProgressMonitor;
import org.stanwood.media.progress.SubProgressMonitor;
import org.stanwood.media.search.MediaSearchResult;
import org.stanwood.media.search.MediaSearcher;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.WatchDirConfig;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.util.FileHelper;

public class ImportMediaCommand extends AbstractServerCommand {

	private boolean useDefaults = true;
	private boolean deleteNonMedia = false;
	private boolean executeActions;

	public ImportMediaCommand(Controller controller) {
		super(controller);
	}

	@Override
	public boolean execute(final ICommandLogger logger,IProgressMonitor monitor) {
		try {
			monitor.beginTask("Importing media...", 105);
			Set<String> extensions = getAcceptableExtensions(monitor);
			List<File> files = getNewMediaFiles(extensions,monitor);
			if (files.size()>0) {
				logger.info (MessageFormat.format(Messages.getString("CLIImportMedia.FOUND_MEDIA_FILES"),files.size())); //$NON-NLS-1$
			}
			else {
				logger.info(Messages.getString("CLIImportMedia.UNABLE_FIND_MEDIA")); //$NON-NLS-1$
				return false;
			}


			Map<File, List<File>> newFiles = setupStoresAndActions();
			List<RenamedEntry> renamedFiles = new ArrayList<RenamedEntry>();

			SubProgressMonitor importMediaMonitor = new SubProgressMonitor(monitor,100);
			try {
				importMediaMonitor.beginTask("Importing media...", files.size());
				MediaSearcher searcher = new MediaSearcher(getController());
				for (File file : files) {
					importMediaMonitor.setTaskName(MessageFormat.format("Importing {0}...",file.getAbsolutePath()));
					MediaSearchResult result;
					try {
						result = searcher.lookupMedia(file,useDefaults);
						if (result==null) {
							logger.error(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_FIND_MEDIA_DETIALS"),file)); //$NON-NLS-1$
							continue;
						}
					}
					catch (StanwoodException e) {
						logger.error(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_FIND_MEDIA_DETIALS"),file),e); //$NON-NLS-1$
						continue;
					}

					moveFileToMediaDir(logger,file, renamedFiles,newFiles, result,searcher);
					importMediaMonitor.worked(1);
				}
			}
			finally {
				importMediaMonitor.done();
			}

			SubProgressMonitor renamedFilesMonitor = new SubProgressMonitor(monitor,100);
			try {
				renamedFilesMonitor.beginTask("Informing stores of new filenames", renamedFiles.size());
				for (RenamedEntry e : renamedFiles) {
					e.getMediaDirectory().renamedFile(e.getMediaDirectory().getMediaDirConfig().getMediaDir()
							                         ,e.getOldName(), e.getNewName());
					renamedFilesMonitor.worked(1);
				}
			}
			finally {
				renamedFilesMonitor.done();
			}

			if (executeActions) {
				for (Entry<File,List<File>> e : newFiles.entrySet()) {
					MediaDirectory mediaDir = getController().getMediaDirectory(e.getKey());
					monitor.setTaskName(MessageFormat.format("Execute store actions on media directory {0}",mediaDir.getMediaDirConfig().getMediaDir()));
					performActions(logger,e.getValue(),mediaDir);
				}
			}
			monitor.worked(1);

			if (deleteNonMedia) {
				monitor.setTaskName("Removing non media files in watched directories");
				cleanUpNonMediaFiles(logger,extensions);
			}
			monitor.worked(1);

			return true;
		} catch (ConfigException e) {
			logger.error(Messages.getString("CLIImportMedia.UNABLE_READ_CONFIG"),e); //$NON-NLS-1$
			return false;
		} catch (StanwoodException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		finally {
			monitor.done();
		}
		return false;
	}

	private void cleanUpNonMediaFiles(ICommandLogger logger,Set<String>extensions) {
		for (WatchDirConfig wd : getController().getWatchDirectories()) {
			List<File> dirs = FileHelper.listDirectories(wd.getWatchDir());
			for (File d : dirs) {
				if (d !=null && !d.equals(wd.getWatchDir()) && !dirContainsMedia(extensions,d)) {
					if (getController().isTestRun()) {
						logger.info(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_DELETE_FOLDER_TEST_MODE"), d)); //$NON-NLS-1$
					}
					else {
						logger.info(MessageFormat.format(Messages.getString("CLIImportMedia.DELETEING_FOLDER"), d)); //$NON-NLS-1$
						try {
							FileHelper.delete(d);
						} catch (IOException e) {
							logger.error(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_DELETE_FOLDER"),d),e); //$NON-NLS-1$
						}
					}

				}
			}
		}
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

	private boolean dirContainsMedia(Set<String>extensions,File d) {
		File files[] = d.listFiles();
		if (files!=null) {
			for (File f : files) {
				if (f.isDirectory()) {
					if (dirContainsMedia(extensions,f)) {
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

	private void moveFileToMediaDir(ICommandLogger logger,File file,final List<RenamedEntry>renamed,final Map<File,List<File>>newFiles,MediaSearchResult result, MediaSearcher searcher) throws IOException, StoreException, ConfigException {
		final MediaDirectory dir = findMediaDir(file, result.getVideo());
		if (dir==null) {
			throw new ConfigException(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_FIND_MEDIA_DIR"),file)); //$NON-NLS-1$
		}
		final File mediaDirLoc = dir.getMediaDirConfig().getMediaDir();
		logger.info(MessageFormat.format(Messages.getString("CLIImportMedia.MOVING_MEDIA_MSG"),file,mediaDirLoc)); //$NON-NLS-1$
		RenameAction ra = new RenameAction();
		try {
			ra.init(dir);
			ra.setTestMode(getController().isTestRun());

			if (result.getVideo() instanceof IFilm) {
				Integer part = MediaSearcher.getFilmPart(result.getMediaDirectory(), file, (IFilm)result.getVideo());
				ra.perform(dir, (IFilm)result.getVideo(), file,part, new IActionEventHandler() {
					@Override
					public void sendEventRenamedFile(File oldName, File newName)
							throws ActionException {
						renamed.add(new RenamedEntry(oldName, newName,dir));
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
						renamed.add(new RenamedEntry(oldName, newName,dir));
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
			logger.error(MessageFormat.format(Messages.getString("CLIImportMedia.UNABLE_TO_MOVE_FILE"),file),e); //$NON-NLS-1$
		}
	}

	private void performActions(ICommandLogger logger,List<File> newFiles, MediaDirectory dir) throws ActionException, ConfigException {
		logger.info(MessageFormat.format(Messages.getString("CLIImportMedia.PERFORMING_ACTIONS"), dir.getMediaDirConfig().getMediaDir())); //$NON-NLS-1$
		List<IAction> actions = new ArrayList<IAction>(dir.getActions());
		ActionPerformer actionPerformer = new ActionPerformer(getController(),actions,dir,dir.getMediaDirConfig().getExtensions());
		actionPerformer.performActions(newFiles,new HashSet<File>(),new NullProgressMonitor());
	}

	private List<File> getNewMediaFiles(Set<String>extensions,IProgressMonitor monitor) {
		monitor.setTaskName("Finding new media files");
		List<File>newMediaFiles = new ArrayList<File>();
		for (WatchDirConfig c : getController().getWatchDirectories()) {
			File f = c.getWatchDir();
			if (f.isDirectory()) {
				for (File f2 : FileHelper.listFiles(f)) {
					if (isAllowedMediaFileType(extensions,f2)) {
						newMediaFiles .add(f2);
					}
				}
			}
			else {
				newMediaFiles .add(f);
			}
		}
		monitor.worked(1);
		return newMediaFiles;
	}


	private Set<String> getAcceptableExtensions(IProgressMonitor monitor) throws ConfigException {
		monitor.setTaskName("Getting accetable media file extensions");
		Set<String>extensions = new HashSet<String>();
		for (File mediaDirLoc :  getController().getMediaDirectories()) {
			MediaDirectory mediaDir = getController().getMediaDirectory(mediaDirLoc);
			extensions.addAll(mediaDir.getMediaDirConfig().getExtensions());
		}
		monitor.worked(1);
		return extensions;
	}

	private boolean isAllowedMediaFileType(Set<String>extensions,File f2) {
		if (extensions.contains(FileHelper.getExtension(f2))) {
			return true;
		}
		return false;
	}

	@param(name="deleteNonMedia",description="If set to True, then delete files that are not media files.")
	public void setDeleteNonMedia(boolean value) {
		this.deleteNonMedia = value;
	}

	@param(name="useDefaults",description="If set to true, attempts are made to import the media files into a default media directory if the actual media directory can't be found")
	public void setUseDefaults(boolean value) {
		this.useDefaults = value;
	}

	@param(name="executeActions",description="If set to true, then actions of the media directory are executed after importing the media")
	public void setExecuteActions(boolean executeActions) {
		this.executeActions = executeActions;
	}
}
