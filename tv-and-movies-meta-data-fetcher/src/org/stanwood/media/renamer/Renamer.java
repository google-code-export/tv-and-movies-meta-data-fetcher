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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

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
 * 	%s - season number
 *  %e - episode number
 *  %% - add a % char
 *  %n - show name
 *  %t - episode or film title
 *  %x - extension (avi, mkv....)
 * </pre>
 */
public class Renamer {

	/** the token for "show name" */
	public static final String TOKEN_SHOW_NAME = "%n";
	/** the token for "episode number" */
	public static final String TOKEN_EPISODE = "%e";
	/** the token for "season number" */
	public static final String TOKEN_SEASON = "%s";
	/** the token for "extension" */
	public static final String TOKEN_EXT = "%x";
	/** the token for "episode or film title" */
	public static final String TOKEN_TITLE = "%t";
	/** add a % char */
	public static final String TOKEN_PERCENT = "%%";
	/** the token for "show Id" */
	public static final String TOKEN_ID = "%h";

	private final static String TOKENS[] = {TOKEN_SHOW_NAME,TOKEN_EPISODE,TOKEN_SEASON,TOKEN_EXT,TOKEN_TITLE,TOKEN_PERCENT,TOKEN_ID};

	private final static Log log = LogFactory.getLog(Renamer.class);

	private String[] exts;
	private boolean refresh;
	private boolean recursive;

	private MediaDirectory dir;
	private Controller controller;

	/**
	 * Constructor used to create a instance of the class
	 * @param controller The controller
	 * @param dirConfig The root directory configuration the media is located in
	 * @param exts The extensions to search for
	 * @param refresh If true, then don't read from the stores
	 * @param recursive If true, then also include sub-directories
	 */
	public Renamer(Controller controller,MediaDirectory dir,String exts[], boolean refresh,boolean recursive) {
		this.dir = dir;
		this.exts = exts.clone();
		this.refresh = refresh;
		this.recursive = recursive;
		this.controller = controller;
	}

	/**
	 * This will cause the renaming to happen.
	 * @return false if their was a fatal error, otherwise true
	 * @throws MalformedURLException Thrown if their is a problem creating URL's
	 * @throws IOException Thrown if their is a IO problem. For example, not been
	 *                     about to read from the disc.
	 * @throws SourceException Thrown if their is a problem reading from the source
	 * @throws StoreException Thrown is their is a problem with a store
	 */
	public boolean tidyShowNames() throws MalformedURLException, IOException, SourceException, StoreException {
		return tidyDirectory(dir.getMediaDirConfig().getMediaDir());
	}

	private boolean tidyDirectory(File parentDir) throws MalformedURLException, IOException, SourceException, StoreException {
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

		List<File> sortedFiles = new ArrayList<File>();
		for (File file : files) {
			sortedFiles.add(file);
		}
		Collections.sort(sortedFiles,new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				return arg0.getAbsolutePath().compareTo(arg1.getAbsolutePath());
			}
		});

		if (dir.getMediaDirConfig().getMode() == Mode.TV_SHOW) {
			for (File file : sortedFiles) {
				if (!renameTVShow(file)) {
					return false;
				}
			}
		} else if (dir.getMediaDirConfig().getMode() == Mode.FILM) {
			for (File file : sortedFiles) {
				if (!renameFilm(file)) {
					return false;
				}
			}
		} else {
			log.fatal("Unknown rename mode");
			return false;
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
		return true;
	}

	private boolean renameFilm(File file) throws MalformedURLException, SourceException, IOException, StoreException {
		SearchResult result = searchForId(file);
		if (result==null) {
			log.error("Unable to find film id for file '"+file.getName()+"'");
			return false;
		}

		String oldFileName = file.getName();

		Film film = dir.getFilm(dir.getMediaDirConfig().getMediaDir(), file,result,refresh);
		if (film==null) {
			log.error("Unable to find film with id  '" + result.getId() +"' and source '"+result.getSourceId()+"'");
			return false;
		}

		String ext = oldFileName.substring(oldFileName.lastIndexOf('.')+1);
		File newName = getNewFilmName(film, ext);

		doRename(file, newName);
		return true;
	}

	private SearchResult searchForId(File file) throws MalformedURLException, SourceException, StoreException, IOException
	{
		SearchResult result;
		result = dir.searchForVideoId(dir.getMediaDirConfig(),file);
		return result;

	}

	private boolean renameTVShow(File file) throws MalformedURLException, SourceException, IOException, StoreException {
		SearchResult result = searchForId(file);
		if (result==null) {
			log.error("Unable to find show id");
			return false;
		}

		Show show =  dir.getShow(dir.getMediaDirConfig().getMediaDir(),file,result,refresh);
		if (show == null) {
			log.fatal("Unable to find show details");
			return false;
		}
		String oldFileName = file.getName();
		ParsedFileName data =  FileNameParser.parse(dir.getMediaDirConfig(),file);
		if (data==null) {
			log.error("Unable to workout the season and/or episode number of '" + file.getName()+"'");
		}
		else {
			Season season = dir.getSeason(dir.getMediaDirConfig().getMediaDir(),file, show, data.getSeason(), refresh);
			if (season == null) {
				log.error("Unable to find season for file : " + file.getAbsolutePath());
			} else {
				Episode episode = dir.getEpisode(dir.getMediaDirConfig().getMediaDir(),file, season, data.getEpisode(), refresh);
				if (episode == null) {
					log.error("Unable to find episode for file : " + file.getAbsolutePath());
				} else {
					String ext = oldFileName.substring(oldFileName.length() - 3);
					File newName = getNewTVShowName(show, season, episode, ext);

					doRename(file, newName);
				}
			}
		}
		return true;
	}

	private void doRename(File file, File newFile) throws StoreException {
		// Remove characters from filenames that windows and linux don't like
		if (file.equals(newFile)) {
			log.info("File '" + file.getAbsolutePath()+"' already has the correct name.");
		}
		else {
			if (newFile.exists()) {
				log.error("Unable rename '"+file.getAbsolutePath()+"' file too '"+newFile.getAbsolutePath()+"' as it already exists.");
			}
			else {
				if (!newFile.getParentFile().exists()) {
					if (!newFile.getParentFile().mkdirs() || !newFile.getParentFile().exists()) {
						log.error("Unable to create directories: " + newFile.getParentFile().getAbsolutePath());
					}
				}
				log.info("Renaming '" + file.getAbsolutePath() + "' -> '" + newFile.getAbsolutePath()+"'");

				File oldFile = new File(file.getAbsolutePath());
				if (file.renameTo(newFile)) {
					dir.renamedFile(dir.getMediaDirConfig().getMediaDir(),oldFile,newFile);
				}
				else {
					log.error("Failed to rename '"+file.getAbsolutePath()+"' file too '"+newFile.getName()+"'.");
				}
			}
		}
	}

	private String normalizeText(String text) {
		text = text.replaceAll(":|/","-");
		text = text.replaceAll("!",".");
		return text;
	}

	private File getNewFilmName(Film film,String ext) {
		String newName = dir.getMediaDirConfig().getPattern();
		newName = newName.replaceAll(TOKEN_ID, normalizeText(film.getId()));
		newName = newName.replaceAll(TOKEN_PERCENT, "%");
		newName = newName.replaceAll(TOKEN_TITLE, normalizeText(film.getTitle()));
		newName = newName.replaceAll(TOKEN_EXT, normalizeText(ext));
		File path = getPath(newName);
		return path;
	}

	private File getNewTVShowName(Show show,Season season, Episode episode,String ext) {
		String newName = dir.getMediaDirConfig().getPattern();
		newName = newName.replaceAll(TOKEN_ID, normalizeText(show.getShowId()));
		newName = newName.replaceAll(TOKEN_SEASON, String.valueOf(season.getSeasonNumber()));
		String episodeNum = String.valueOf(episode.getEpisodeNumber());
		if (episodeNum.length()==1) {
			episodeNum = "0" +episodeNum;
		}

		newName = newName.replaceAll(TOKEN_EPISODE, episodeNum);
		newName = newName.replaceAll(TOKEN_PERCENT, "%");
		newName = newName.replaceAll(TOKEN_SHOW_NAME, normalizeText(show.getName()));
		newName = newName.replaceAll(TOKEN_TITLE, normalizeText(episode.getTitle()));
		newName = newName.replaceAll(TOKEN_EXT, normalizeText(ext));

		File path = getPath(newName);
		return path;
	}

	/**
	 * Used to check a pattern is valid
	 * @param pattern The pattern
	 * @return true if valid, otherwise false
	 */
	public static boolean validPattern(String pattern) {

		for (String token : TOKENS) {
			pattern = pattern.replaceAll(token, "");
		}
		return !pattern.contains("%");
	}

	private File getPath(String newName) {
		File dir = this.dir.getMediaDirConfig().getMediaDir();
		StringTokenizer tok = new StringTokenizer(newName,""+File.separatorChar);
		while (tok.hasMoreTokens()) {
			dir = new File(dir,tok.nextToken());
		}
		return dir;
	}
}
