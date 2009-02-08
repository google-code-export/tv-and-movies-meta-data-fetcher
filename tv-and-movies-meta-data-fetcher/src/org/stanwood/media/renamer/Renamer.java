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
package org.stanwood.media.renamer;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.StoreException;


/**
 * This class is used to rename show episodes in a directory to the correct name.
 * The pattern given is the format of the name.
 * The following are valid values:
 * <pre>
 *  %h - show Id
 * 	%s - series number  
 *  %e - episode number
 *  %% - add a % char
 *  %n - show name
 *  %t - episode or film title  
 *  %x - extension (avi, mkv....)
 * </pre> 
 */
public class Renamer {

	private final static Log log = LogFactory.getLog(Renamer.class);
	
	private String id;
	private File showDirectory;
	private String pattern;
	private String[] exts;
	private boolean refresh;
	private String sourceId;
	private Mode mode;
	private boolean recursive;

	/**
	 * Constructor used to create a instance of the class
	 * @param id The id of the show or film to rename
	 * @param mode Used to tell the renamer what mode to work in
	 * @param showDirectory The directory it is located in
	 * @param pattern The pattern to use while renaming
	 * @param exts The extensions to search for
	 * @param refresh If true, then don't read from the stores
	 * @param recursive If true, then also include subdirectories
	 */
	public Renamer(String id,Mode mode,File showDirectory,String pattern,String exts[], boolean refresh, boolean recursive) {
		this.id = id;
		this.showDirectory = showDirectory;
		this.pattern = pattern;
		this.exts = exts.clone();
		this.refresh = refresh;
		this.mode = mode;
		this.recursive = recursive;
	}
	
	/**
	 * This will cause the renaming to happen.
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a IO problem. For example, not been
	 *                     about to read from the disc.
	 * @throws SourceException Thrown if their is a problem reading from the source
	 * @throws StoreException Thrown is their is a problem with a store
	 */
	public void tidyShowNames() throws MalformedURLException, IOException, SourceException, StoreException {
		tidyDirectory(showDirectory);		
	}
	
	public void tidyDirectory(File parentDir) throws MalformedURLException, IOException, SourceException, StoreException {
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
				
		if (mode == Mode.TV_SHOW) {
			for (File file : files) {
				renameTVShow(file);
			}
		} else if (mode == Mode.FILM) {
			for (File file : files) {
				renameFilm(file);
			}
		} else {
			fatal("Unknown rename mode");
		}
		
		if (recursive) {
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
				tidyDirectory(dir);
			}
		}
	}

	private void renameFilm(File file) throws MalformedURLException, SourceException, IOException, StoreException {
		SearchResult result = searchForId(file);
		if (result!=null) {
			id = result.getId();
			sourceId = result.getSourceId();				
		}
		else {
			log.error("Unable to find film id for file '"+file.getName()+"'");
			return;
		}

		String oldFileName = file.getName();

		Film film = Controller.getInstance().getFilm(file,sourceId,id,refresh);
		if (film==null) {
			log.error("Unable to find film with id  '" + id +"' and source '"+sourceId+"'");
			return;
		}
		
		String ext = oldFileName.substring(oldFileName.lastIndexOf('.')+1);
		String newName = getNewFilmName(film, ext);
	
		doRename(file, oldFileName, newName);
	}

	private SearchResult searchForId(File file)
	{
		SearchResult result;
		try {
			result = Controller.getInstance().searchForVideoId(mode,file);
			return result;
		} catch (SourceException e) {
			e.printStackTrace();
			return null;
		} catch (StoreException e) {
			e.printStackTrace();
			return null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	private void renameTVShow(File file) throws MalformedURLException, SourceException, IOException, StoreException {
		SearchResult result = searchForId(file);
		if (result!=null) {
			id = result.getId();
			sourceId = result.getSourceId();				
		}
		else {
			log.error("Unable to find show id");
			Main.doExit(1);
			return;
		}
				
		Show show =  Controller.getInstance().getShow(file,sourceId, id,refresh);		
		if (show == null) {
			fatal("Unable to find show details");						
		}
		String oldFileName = file.getName(); 
		ParsedFileName data =  FileNameParser.parse(oldFileName);
		if (data==null) {
			log.error("Unable to workout the season and/or episode number of '" + file.getName()+"'");
		}
		else {
			Season season = Controller.getInstance().getSeason(file, show, data.getSeason(), refresh);
			if (season == null) {
				log.error("Unable to find season for file : " + file.getAbsolutePath());
			} else {
				Episode episode = Controller.getInstance().getEpisode(file, season, data.getEpisode(), refresh);
				if (episode == null) {
					log.error("Unable to find epsiode for file : " + file.getAbsolutePath());
				} else {
					String ext = oldFileName.substring(oldFileName.length() - 3);
					String newName = getNewTVShowName(show, season, episode, ext);

					doRename(file, oldFileName, newName);
				}
			}
		}
	
	}

	private void doRename(File file, String oldFileName, String newName) throws StoreException {
		// Don't put ':' in filenames as windows does not like them
		newName = newName.replaceAll(":","-");
		File newFile = new File(file.getParentFile(),newName);
		if (file.equals(newFile)) {
			log.info("File '" + oldFileName+"' already has the correct name.");
		}
		else {					
			if (newFile.exists()) {
				log.error("Unable rename '"+oldFileName+"' file too '"+newFile.getName()+"' as it already exists.");					
			}
			else {
				log.info("Renaming '" + oldFileName + "' -> '" + newName+"'");
				
				File oldFile = new File(file.getAbsolutePath());
				if (file.renameTo(newFile)) {
					Controller.getInstance().renamedFile(oldFile,newFile);	
				}
				else {
					log.error("Failed to rename '"+oldFileName+"' file too '"+newFile.getName()+"'.");
				}
			}
		}
	}

	private void fatal(String msg) {
		log.fatal(msg);
		Main.doExit(1);
		throw new RuntimeException(msg);
	}

	private String getNewFilmName(Film film, String ext) {
		String newName = pattern;
		newName = newName.replaceAll("%h", String.valueOf(id));
		newName = newName.replaceAll("%%", "%");
		newName = newName.replaceAll("%t", film.getTitle());
		newName = newName.replaceAll("%x", ext);
		return newName;
	}
	
	private String getNewTVShowName(Show show,Season season, Episode episode,String ext) {
		String newName = pattern;
		newName = newName.replaceAll("%h", String.valueOf(id));
		newName = newName.replaceAll("%s", String.valueOf(season.getSeasonNumber()));
		String episodeNum = String.valueOf(episode.getEpisodeNumber());
		if (episodeNum.length()==1) {
			episodeNum = "0" +episodeNum;
		}
		
		newName = newName.replaceAll("%e", episodeNum);
		newName = newName.replaceAll("%%", "%");
		newName = newName.replaceAll("%n", show.getName());
		newName = newName.replaceAll("%t", episode.getTitle());
		newName = newName.replaceAll("%x", ext);
		return newName;
	}
}
