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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.rename.FileNameParser;
import org.stanwood.media.actions.rename.ParsedFileName;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;


/**
 * This class is used to perform a list of actions upon files in a media directory
 */
public class ActionPerformer implements IActionEventHandler {

	private final static Log log = LogFactory.getLog(ActionPerformer.class);

	private List<String> exts;
	private MediaDirectory dir;
	private List<IAction> actions;

	private ArrayList<File> dirs;



	private boolean testMode;


	/**
	 * Constructor used to create a instance of the class
	 * @param actions List of actions to perform
	 * @param dir The media directory
	 * @param exts The extensions to search for
	 * @param testMode True if test mode is enabled
	 */
	public ActionPerformer(List<IAction> actions,MediaDirectory dir,List<String> exts,boolean testMode) {
		this.dir = dir;
		this.exts = exts;
		this.actions = actions;
		this.testMode =testMode;
	}

	/**
	 * Used to perform the actions
	 * @throws ActionException Thrown if their are any errors with the actions
	 */
	public void performActions() throws ActionException {
		List<File> sortedFiles = findMediaFiles();

		performActions(sortedFiles);
	}

	/**
	 * Used to perform the actions
	 * @param files The files to perform the actions on
	 * @throws ActionException Thrown if their are any errors with the actions
	 */
	public void performActions(List<File> files) throws ActionException {
		for (IAction action : actions) {
			action.init(dir);
		}

		log.info(("Processing "+files.size()+" files"));
		performActionsFiles(files);
		performActionsDirs(files);

		for (IStore store : dir.getStores()) {
			try {
				store.performedActions(dir);
			} catch (StoreException e) {
				log.error("Unable to clean up store: " + store.getClass().getName(),e);
			}
		}

		for (IAction action : actions) {
			action.finished(dir);
		}
		log.info("Finished");
	}

	protected List<File> findMediaFiles() throws ActionException {
		List<File>mediaFiles = new ArrayList<File>();
		dirs = new ArrayList<File>();
		findMediaFiles(dir.getMediaDirConfig().getMediaDir(),mediaFiles,dirs);
		List<File> sortedFiles = new ArrayList<File>();
		for (File file : mediaFiles) {
			if (dir.getMediaDirConfig().getIgnorePatterns()!=null) {
				for (Pattern p : dir.getMediaDirConfig().getIgnorePatterns()) {
					if (p.matcher(file.getAbsolutePath()).matches()) {
						continue;
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
		return sortedFiles;
	}

	private void findMediaFiles(File parentDir,List<File>mediaFiles,List<File>mediaDirs) throws ActionException {
		if (log.isDebugEnabled()) {
			log.debug("Tidying show names in the directory : " + parentDir);
		}
		File files[] = parentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isFile()) {
					String name = file.getName();
					for (String ext : exts) {
						if (name.toLowerCase().endsWith("."+ext.toLowerCase())) {
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
			mediaDirs.add(dir);
			findMediaFiles(dir,mediaFiles,mediaDirs);
		}
	}

	private void performActionsFiles(List<File> files) throws ActionException {
		for (File file : files) {
			if (dir.getMediaDirConfig().getMode().equals(Mode.FILM)) {
				SearchResult result = findFilm(dir, file);
				if (result!=null) {
					Film film = getFilm(result,dir,file);
					if (film!=null) {
						for (IAction action : actions) {
							action.perform(dir,film,file,result.getPart(),this);
						}
					}
				}
			}
			else if (dir.getMediaDirConfig().getMode().equals(Mode.TV_SHOW)) {
				Episode episode = getTVEpisode(dir, file);
				if (episode!=null) {
					for (IAction action : actions) {
						action.perform(dir,episode, file,this);
					}
				}
			}

		}
	}

	private void performActionsDirs(List<File> files) throws ActionException {
		log.info(("Processing "+files.size()+" dirs"));
		Set<File> dirs = new HashSet<File>();
		for (File file : files) {
			if (!dir.getMediaDirConfig().getMediaDir().equals(file.getParentFile())) {
				dirs.add(file.getParentFile());
			}
		}
		for (File d : dirs) {
			for (IAction action : actions) {
				action.performOnDirectory(dir, d,this);
			}
		}
		log.info("Finished");
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
				log.error("Unable to process action file deleted event",e);
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
		} catch (StoreException e) {
			throw new ActionException("Unable to rename file",e);
		}
	}

	private Episode getTVEpisode(MediaDirectory dir,File file) throws ActionException {
		boolean refresh = false;
		try {
			SearchResult result = searchForId(dir,file);
			if (result==null) {
				log.error("Unable to find show id for file '"+file+"'");
				return null;
			}

			Show show =  dir.getShow(dir.getMediaDirConfig().getMediaDir(),file,result,refresh);
			if (show == null) {
				log.fatal("Unable to find show details for file '"+file+"'");
				return null;
			}

			ParsedFileName data =  FileNameParser.parse(dir.getMediaDirConfig(),file);
			if (data==null) {
				log.error("Unable to workout the season and/or episode number for file '" + file.getName()+"'");
			}
			else {
				Season season = dir.getSeason(dir.getMediaDirConfig().getMediaDir(),file, show, data.getSeason(), refresh);
				if (season == null) {
					log.error("Unable to find season for file '"+file+"'");
				} else {
					Episode episode = dir.getEpisode(dir.getMediaDirConfig().getMediaDir(),file, season, data.getEpisode(), refresh);
					return episode;
				}
			}
		}
		catch (Exception e) {
			throw new ActionException("Unable to get TV epsiode details for file '"+file.getAbsolutePath()+"'",e);
		}
		return null;
	}

	private Film getFilm(SearchResult result,MediaDirectory dir,File file) throws ActionException {
		boolean refresh = false;
		try {
			Film film = dir.getFilm(dir.getMediaDirConfig().getMediaDir(), file,result,refresh);
			if (film==null) {
				log.error("Unable to find film with id  '" + result.getId() +"' and source '"+result.getSourceId()+"'");
				return null;
			}
			if (!testMode) {
				boolean found = false;
				for (VideoFile vf : film.getFiles()) {
					if (vf.getLocation().equals(file)) {
						found = true;
						break;
					}
				}
				if (!found) {
					for (IStore store : dir.getStores()) {
						store.cacheFilm(dir.getMediaDirConfig().getMediaDir(), file, film, result.getPart());
					}
				}
			}
			return film;
		}
		catch (Exception e) {
			throw new ActionException("Unable to find film for file '" + file.getAbsolutePath()+"'",e);
		}
	}

	protected SearchResult findFilm(MediaDirectory dir, File file) throws ActionException {
		try {
			SearchResult result = searchForId(dir,file);
			if (result==null) {
				log.error("Unable to find film id for file '"+file.getName()+"'");
				return null;
			}
			return result;
		}
		catch (SourceException e) {
			log.error("Unable to search for film: " + file,e);
			return null;
		}
		catch (Exception e) {
			throw new ActionException("Unable to find film for file '" + file.getAbsolutePath()+"'",e);
		}
	}

	private SearchResult searchForId(MediaDirectory dir,File file) throws MalformedURLException, SourceException, StoreException, IOException
	{
		SearchResult result;
		result = dir.searchForVideoId(dir.getMediaDirConfig(),file);
		return result;

	}
}
