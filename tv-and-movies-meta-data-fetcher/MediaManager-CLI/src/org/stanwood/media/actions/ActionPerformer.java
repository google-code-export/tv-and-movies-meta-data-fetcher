/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.actions;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.FileNameParser;
import org.stanwood.media.actions.rename.ParsedFileName;
import org.stanwood.media.actions.rename.Token;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.IVideoFile;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.progress.SubProgressMonitor;
import org.stanwood.media.search.ReversePatternSearchStrategy;
import org.stanwood.media.search.SearchDetails;
import org.stanwood.media.search.SearchHelper;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.xml.XMLParserException;


/**
 * This class is used to perform a list of actions upon files in a media directory
 */
public class ActionPerformer implements IActionEventHandler {

	private final static Log log = LogFactory.getLog(ActionPerformer.class);

	private List<String> exts;
	private MediaDirectory dir;
	private List<IAction> actions;
	private boolean testMode;
	private File nativeFolder;
	private File configDir;

	private SeenDatabase seenDb;

	/**
	 * Constructor used to create a instance of the class
	 * @param configDir The configuration directory
	 * @param nativeFolder
	 * @param actions List of actions to perform
	 * @param dir The media directory
	 * @param exts The extensions to search for
	 * @param testMode True if test mode is enabled
	 */
	public ActionPerformer(File configDir,File nativeFolder, List<IAction> actions,MediaDirectory dir,List<String> exts,boolean testMode) {
		this.dir = dir;
		this.exts = exts;
		this.actions = actions;
		this.testMode =testMode;
		this.nativeFolder = nativeFolder;
		this.configDir = configDir;
	}

	/**
	 * Used to perform the actions
	 * @param monitor Progress monitor
	 * @throws ActionException Thrown if their are any errors with the actions
	 */
	public void performActions(IProgressMonitor monitor) throws ActionException {
		try {
			monitor.beginTask(Messages.getString("ActionPerformer.Performing_actions"), 4); //$NON-NLS-1$
			if (dir.getMediaDirConfig().getIgnoreSeen()) {
				try {
					seenDb= new SeenDatabase(configDir);
					seenDb.read(new SubProgressMonitor(monitor,1));
				}
				catch (IOException e) {
					throw new ActionException(Messages.getString("ActionPerformer.UNABLE_READ_SEEN_DATABASE"),e); //$NON-NLS-1$
				}
				catch (XMLParserException e) {
					throw new ActionException(Messages.getString("ActionPerformer.UNABLE_READ_SEEN_DATABASE"),e); //$NON-NLS-1$
				}
			}

			List<File> sortedFiles = findMediaFiles(new SubProgressMonitor(monitor,1));
			Set<File> dirs = findDirs(new SubProgressMonitor(monitor,1));

			performActions(sortedFiles,dirs,monitor);
			if (seenDb!=null) {
				try {
					seenDb.write(new SubProgressMonitor(monitor,1));
				} catch (FileNotFoundException e) {
					throw new ActionException(Messages.getString("ActionPerformer.UNABLE_WRITE_SEEN_DATABASE"),e); //$NON-NLS-1$
				}
			}
		}
		finally {
			monitor.done();
		}
	}

	private Set<File> findDirs(IProgressMonitor monitor) {
		monitor.subTask(Messages.getString("ActionPerformer.SEARCHING_FOR_MEDIA_DIRS")); //$NON-NLS-1$
		Set<File>dirs = new HashSet<File>();
		findDirs(dirs, dir.getMediaDirConfig().getMediaDir());
		return dirs;
	}

	/**
	 * Used to perform the actions
	 * @param files The files to perform the actions on
	 * @param dirs The directories with the media directory
	 * @param parentMonitor Progress monitor parent
	 * @throws ActionException Thrown if their are any errors with the actions
	 */
	public void performActions(List<File> files,Set<File> dirs,IProgressMonitor parentMonitor) throws ActionException {
		parentMonitor.subTask(Messages.getString("ActionPerformer.SETUP_STORES")); //$NON-NLS-1$
		if (!initStores()) {
			throw new ActionException(Messages.getString("ActionPerformer.UNABLE_SETUP_STORES")); //$NON-NLS-1$
		}

		parentMonitor.subTask(Messages.getString("ActionPerformer.SETUP_ACTIONS")); //$NON-NLS-1$
		for (IAction action : actions) {
			action.init(dir);
		}

		log.info((MessageFormat.format(Messages.getString("ActionPerformer.PROCESSING_FILES"),files.size()))); //$NON-NLS-1$
		performActionsFiles(files);
		performActionsDirs(dirs);

		for (IStore store : dir.getStores()) {
			try {
				store.performedActions(dir);
			} catch (StoreException e) {
				log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_CLEAN_STORES"),store.getClass().getName()),e); //$NON-NLS-1$
			}
		}

		for (IAction action : actions) {
			action.finished(dir);
		}

		log.info(Messages.getString("ActionPerformer.FINISHED")); //$NON-NLS-1$
	}

	private boolean initStores() {
		boolean hasErrors = false;
		for (IStore store : dir.getStores()) {
			try {
				store.init(nativeFolder);
			}
			catch (StoreException e ) {
				log.debug(e.getMessage(),e);
				hasErrors = true;
			}
		}
		return !hasErrors;
	}

	protected List<File> findMediaFiles(IProgressMonitor monitor) throws ActionException {
		monitor.subTask(Messages.getString("ActionPerformer.SEARCHING_FOR_MEDIA_FILES")); //$NON-NLS-1$
		List<File>mediaFiles = new ArrayList<File>();
		findMediaFiles(dir.getMediaDirConfig().getMediaDir(),mediaFiles);
		List<File> sortedFiles = new ArrayList<File>();
		files:
		for (File file : mediaFiles) {
			if (dir.getMediaDirConfig().getIgnorePatterns()!=null) {
				for (Pattern p : dir.getMediaDirConfig().getIgnorePatterns()) {
					Matcher m = p.matcher(file.getAbsolutePath());
					if (m.matches()) {
						continue files;
					}
				}
			}
			sortedFiles.add(file);
		}
		Collections.sort(sortedFiles,new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				return arg0.getAbsolutePath().compareTo(arg1.getAbsolutePath());
			}
		});

		if (seenDb!=null) {
			Iterator<File> it = sortedFiles.iterator();
			while (it.hasNext()) {
				File f = it.next();
				if (seenDb.isSeen(dir.getMediaDirConfig().getMediaDir(), f)) {
					log.info(MessageFormat.format(Messages.getString("ActionPerformer.INGORED_SEEN_FILE"),f.getAbsolutePath())); //$NON-NLS-1$
					it.remove();
				}
			}
		}

		return sortedFiles;
	}

	private void findMediaFiles(File parentDir,List<File>mediaFiles) throws ActionException {
		if (log.isDebugEnabled()) {
			log.debug(MessageFormat.format(Messages.getString("ActionPerformer.TIDYING_SHOW_NAMES_IN_DIR"),parentDir)); //$NON-NLS-1$
		}
		File files[] = parentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isFile()) {
					String name = file.getName();
					for (String ext : exts) {
						if (name.toLowerCase().endsWith("."+ext.toLowerCase())) { //$NON-NLS-1$
							return true;
						}
					}
				}
				return false;
			}
		});
		for (File f : files) {
			mediaFiles.add(f);
		}

		File dirs[] = parentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		for (File dir : dirs) {
			findMediaFiles(dir,mediaFiles);
		}
	}

	private IFilm getFilm(File file) throws ActionException {
		try {
			for (IStore store : dir.getStores()) {
				IFilm film = store.getFilm(dir,file);
				if (film!=null) {
					return film;
				}
			}
			SearchResult result = findFilm(dir, file);
			if (result!=null) {
				IFilm film = getFilm(result,dir,file);
				if (film!=null) {
					return film;
				}
			}
			return null;
		}
		catch (StoreException e) {
			throw new ActionException(Messages.getString("ActionPerformer.UNABLE_TO_READ_FILE_DETAILS"),e); //$NON-NLS-1$
		}
	}

	private void performActionsFiles(List<File> files) throws ActionException {
		for (File file : files) {
			if (dir.getMediaDirConfig().getMode().equals(Mode.FILM)) {
				IFilm film = getFilm(file);
				if (film!=null) {
					for (IAction action : actions) {
						Integer part = getFilmPart(file, film);
						action.perform(dir,film,file,part,this);
					}
					if (seenDb!=null && file.exists()) {
						seenDb.markAsSeen(dir.getMediaDirConfig().getMediaDir(), file);
					}
				}
			}
			else if (dir.getMediaDirConfig().getMode().equals(Mode.TV_SHOW)) {
				IEpisode episode = getTVEpisode(dir, file);
				if (episode!=null) {
					for (IAction action : actions) {
						action.perform(dir,episode, file,this);
					}
					if (seenDb!=null && file.exists()) {
						seenDb.markAsSeen(dir.getMediaDirConfig().getMediaDir(), file);
					}
				}
			}

		}
	}

	protected Integer getFilmPart(File file, IFilm film) {
		Integer part = null;
		if (film.getFiles()!=null) {
			for (IVideoFile vf : film.getFiles()) {
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
			SearchDetails result = rp.getSearch(file, dir.getMediaDirConfig().getMediaDir(), dir.getMediaDirConfig().getPattern(), dir);
			if (result!=null) {
				part = result.getPart();
			}
		}
		return part;
	}

	private void findDirs(Set<File>dirs,File parent) {
		for (File file : parent.listFiles()) {
			if (file.isDirectory()) {
				findDirs(dirs,file);
				dirs.add(file);
			}
		}
	}

	private void performActionsDirs(Set<File>dirs) throws ActionException {
		log.info((MessageFormat.format(Messages.getString("ActionPerformer.PROCESSING_DIRS"),dirs.size()))); //$NON-NLS-1$


		for (File d : dirs) {
			for (IAction action : actions) {
				action.performOnDirectory(dir, d,this);
			}
		}
		log.info(Messages.getString("ActionPerformer.FINISHED")); //$NON-NLS-1$
	}

	/**
	 * Listen for the new file event and perform the actions on it
	 * @param file the File
	 */
	@Override
	public void sendEventNewFile(File file) throws ActionException {
		List<File> files = new ArrayList<File>();
		files.add(file);
		performActionsFiles(files);
	}

	/**
	 * Listen for the delete file event and remove it from the stores
	 * @param file the File
	 */
	@Override
	public void sendEventDeletedFile(File file) {
		for (IStore store : dir.getStores()) {
			try {
				store.fileDeleted(dir,file);
			} catch (StoreException e) {
				log.error(Messages.getString("ActionPerformer.UNABLE_TO_PROCESS_ACTION_FILE_DELETED_EVENT"),e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Listen for the rename file event and and update stores
	 * @param oldFile The old filename
	 * @param newFile The new filename
	 */
	@Override
	public void sendEventRenamedFile(File oldFile, File newFile) throws ActionException {
		try {
			dir.renamedFile(dir.getMediaDirConfig().getMediaDir(),oldFile,newFile);
			if (seenDb!=null) {
				seenDb.renamedFile(dir.getMediaDirConfig().getMediaDir(),oldFile,newFile);
			}
		} catch (StoreException e) {
			throw new ActionException(Messages.getString("ActionPerformer.UNABLE_RENAME_FILE"),e); //$NON-NLS-1$
		}
	}

	private IEpisode getTVEpisode(MediaDirectory dir,File file) throws ActionException {
		boolean refresh = false;
		try {
			for (IStore store : dir.getStores()) {
				IEpisode ep = store.getEpisode(dir,file);
				if (ep!=null) {
					return ep;
				}
			}
			SearchResult result = searchForId(dir,file);
			if (result==null) {
				log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_SHOW_ID_FOR_FILE"),file)); //$NON-NLS-1$
				return null;
			}

			IShow show =  dir.getShow(dir.getMediaDirConfig().getMediaDir(),file,result,refresh);
			if (show == null) {
				log.fatal(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_TO_FIND_SHOW_DETAILS_FOR_FILE"),file)); //$NON-NLS-1$
				return null;
			}

			ParsedFileName data =  FileNameParser.parse(dir.getMediaDirConfig(),file);
			if (data==null) {
				log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_TO_WORKOUT_SEASON_AND_EPISODE_NUMBER_FOR_FILE"),file.getName())); //$NON-NLS-1$
			}
			else {
				ISeason season = dir.getSeason(dir.getMediaDirConfig().getMediaDir(),file, show, data.getSeason(), refresh);
				if (season == null) {
					log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABVLE_TO_FIMD_SEASON"),file)); //$NON-NLS-1$
				} else {
					IEpisode episode = dir.getEpisode(dir.getMediaDirConfig().getMediaDir(),file, season, data.getEpisode(), refresh);
					if (episode==null) {
						log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_EPISODE_NUMBER"),file)); //$NON-NLS-1$
						return null;
					}

					boolean found = false;
					if (episode.getFiles()!=null) {
						for (IVideoFile vf : episode.getFiles()) {
							if (vf.getLocation().equals(file)) {
								found = true;
							}
						}
					}
					if (!found) {
						for (IStore store : dir.getStores()) {
							store.cacheEpisode(dir.getMediaDirConfig().getMediaDir(), file, episode);
						}
					}

					return episode;
				}
			}
		}
		catch (SourceException e) {
			log.error(Messages.getString("ActionPerformer.UNABLE_FIND_MEDIA_DETAILS"),e); //$NON-NLS-1$
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ActionException(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_GET_TV_EPISODE_DETAILS"),file.getAbsolutePath()),e); //$NON-NLS-1$
		}
		return null;
	}

	private IFilm getFilm(SearchResult result,MediaDirectory dir,File file) throws ActionException {
		if (!file.exists()) {
			return null;
		}
		boolean refresh = false;
		try {
			IFilm film = dir.getFilm(dir.getMediaDirConfig().getMediaDir(), file,result,refresh);
			if (film==null) {
				log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_FILM"),result.getId(),result.getSourceId(),file.getAbsolutePath())); //$NON-NLS-1$
				return null;
			}
			if (!testMode) {
				boolean found = false;
				Integer maxPart = 0;
				for (IVideoFile vf : film.getFiles()) {
					if (vf.getLocation().equals(file)) {
						found = true;
					}
					if (result.getPart()!=null) {
						if (vf.getPart()!=null && vf.getPart()>maxPart) {
							maxPart = vf.getPart();
						}
					}
				}
				if (!found) {
					for (IStore store : dir.getStores()) {
						store.cacheFilm(dir.getMediaDirConfig().getMediaDir(), file, film, result.getPart());
					}
				}

				// Update existing stores with new part
				if (result.getPart()!=null && result.getPart()>maxPart) {
					for (IVideoFile vf : film.getFiles()) {
						if (!vf.getLocation().equals(file)) {
							for (IStore store : dir.getStores()) {
								if (vf.getLocation().exists()) {
									store.cacheFilm(dir.getMediaDirConfig().getMediaDir(), vf.getLocation(), film, vf.getPart());
								}
							}
						}
					}
				}
			}
			return film;
		}
		catch (Exception e) {
			throw new ActionException(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_FILM_WITH_FILE"),file.getAbsolutePath()),e); //$NON-NLS-1$
		}
	}

	protected SearchResult findFilm(MediaDirectory dir, File file) throws ActionException {
		try {
			SearchResult result = searchForId(dir,file);
			if (result==null) {
				log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_FILM_WITH_FILE"),file.getAbsolutePath())); //$NON-NLS-1$
				return null;
			}
			return result;
		}
		catch (SourceException e) {
			log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNALBE_SEARCH_FOR_FILM"),file.getAbsolutePath()),e); //$NON-NLS-1$
			return null;
		}
		catch (Exception e) {
			throw new ActionException(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_FILM_WITH_FILE"),file.getAbsolutePath()),e); //$NON-NLS-1$
		}
	}

	private SearchResult searchForId(MediaDirectory dir,File file) throws MalformedURLException, SourceException, StoreException, IOException
	{
		SearchResult result;
		result = dir.searchForVideoId(file);
		return result;

	}
}
