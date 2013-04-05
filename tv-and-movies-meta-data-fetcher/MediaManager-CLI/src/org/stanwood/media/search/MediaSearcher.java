/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.search;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.actions.Messages;
import org.stanwood.media.actions.rename.FileNameParser;
import org.stanwood.media.actions.rename.ParsedFileName;
import org.stanwood.media.actions.rename.Token;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.ConfigReader;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;

/**
 * Used to search for media information
 */
public class MediaSearcher {

	private final static Log log = LogFactory.getLog(MediaSearcher.class);
	private List<MediaDirectory> mediaDirs;

	/**
	 * The constructor
	 * @param controller The media controller
	 * @throws ConfigException Thrown if their is a problem reading the configuration
	 */
	public MediaSearcher(Controller controller) throws ConfigException {
		if (controller!=null) {

			mediaDirs = new ArrayList<MediaDirectory>();
			for (File mediaDirLoc :  controller.getMediaDirectories()) {
				MediaDirectory mediaDir = controller.getMediaDirectory(mediaDirLoc);
				if (mediaDir.getMediaDirConfig().getMode()==Mode.TV_SHOW) {
					mediaDirs.add(0,mediaDir);
				}
				else {
					mediaDirs.add(mediaDir);
				}
			}
		}
	}

	/**
	 * Used to lookup media file information
	 * @param mediaFile The media file
	 * @param useSources If true, then information is fetched remotely as well as locally
	 * @return The media file information, or null if it could not be found.
	 * @throws ActionException Thrown if their are any problems.
	 */
	public MediaSearchResult lookupMedia(File mediaFile,boolean useSources) throws ActionException {
		Mode mode = null;
		FilmNFOSearchStrategy nfoSearcher = new FilmNFOSearchStrategy();
		for (MediaDirectory mediaDir : mediaDirs) {
			if (mediaDir.getMediaDirConfig().getMode()==Mode.FILM) {
				SearchDetails details = nfoSearcher.getSearch(mediaFile, mediaDir);
				if (details!=null) {
					mode = Mode.FILM;
				}
				break;
			}
		}


		if (mode==null) {
			if (possibleFilm(mediaFile)) {
				mode = Mode.FILM;
			}
		}

		if (mode==null && mediaFile.getParentFile().getName().toLowerCase().equals("sample")) {
			return null;
		}

		if (mode==null) {
			ParsedFileName parsed = FileNameParser.parse(mediaFile);
			if (parsed==null) {
				mode = Mode.FILM;
			}
		}

		if (mode==null) {
			mode = Mode.TV_SHOW;
		}

		for (MediaDirectory mediaDir : mediaDirs) {
			if (mediaDir.getMediaDirConfig().getMode()==mode) {
				IVideo result = getMediaDetails(mediaDir,mediaFile,useSources);
				if (result!=null) {
					return new MediaSearchResult(mediaDir,result);
				}
			}
		}
		return null;
	}

	private boolean possibleFilm(File mediaFile) {
		List<Pattern> stripTokens  = ConfigReader.DEFAULT_STRIP_TOKENS;
		StringBuilder term = new StringBuilder(mediaFile.getName());
		SearchHelper.replaceDots(term);
		Matcher m = FilmSearcher.PATTERN_YEAR2.matcher(term);
		if (m.matches()) {
			StringBuilder end= new StringBuilder(m.group(3));
			SearchHelper.replaceWithSpaces(end);
			if (SearchHelper.hasStripTokens(stripTokens,end)) {
				return true;
			}
		}

		return false;
	}

	private IVideo getMediaDetails(MediaDirectory dir,File file,boolean useSources) throws ActionException {
		if (dir.getMediaDirConfig().getMode()==Mode.TV_SHOW) {
			return getTVEpisode(dir, file,useSources);
		}
		else {
			return getFilm(dir, file,useSources);
		}
	}

	/**
	 * Used to lookup film information
	 * @param dir The media directory
	 * @param file The media file
	 * @param useSources If true, then information is fetched remotely as well as locally
	 * @return  The media file information, or null if it could not be found.
	 * @throws ActionException Thrown if their are any problems.
	 */
	public static IFilm getFilm(MediaDirectory dir,File file,boolean useSources) throws ActionException {
		try {
			for (IStore store : dir.getStores()) {
				if (file.getAbsolutePath().startsWith(dir.getMediaDirConfig().getMediaDir().getAbsolutePath())) {
					IFilm film = store.getFilm(dir,file);
					if (film!=null) {
						return film;
					}
				}
			}
			SearchResult result = findFilm(dir, file,useSources);
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

	protected static SearchResult findFilm(MediaDirectory dir, File file,boolean useSources) throws ActionException {
		try {
			SearchResult result = searchForId(dir,file,useSources);
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

	private static SearchResult searchForId(MediaDirectory dir,File file,boolean useSources) throws MalformedURLException, SourceException, StoreException, IOException
	{
		SearchResult result;
		result = dir.searchForVideoId(file,useSources);
		return result;

	}

	private static IFilm getFilm(SearchResult result,MediaDirectory dir,File file) throws ActionException {
		if (!file.exists()) {
			return null;
		}
		boolean refresh = false;
		try {
			IFilm film = dir.getFilm(dir.getMediaDirConfig().getMediaDir(), file,result,refresh,false);
			if (film==null) {
				log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_FILM"),result.getId(),result.getSourceId(),file.getAbsolutePath())); //$NON-NLS-1$
				return null;
			}
			if (!dir.getController().isTestRun()) {
//				boolean found = false;
				Integer maxPart = 0;
				for (VideoFile vf : film.getFiles()) {
//					if (vf.getLocation().equals(file)) {
//						found = true;
//					}
					if (result.getPart()!=null) {
						if (vf.getPart()!=null && vf.getPart()>maxPart) {
							maxPart = vf.getPart();
						}
					}
				}

				// Update existing stores with new part
				if (result.getPart()!=null && result.getPart()>maxPart) {
					for (VideoFile vf : film.getFiles()) {
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

	/**
	 * Used to lookup TV episode information
	 * @param dir The media directory
	 * @param file The media file
	 * @param useSources If true, then information is fetched remotely as well as locally
	 * @return  The media file information, or null if it could not be found.
	 * @throws ActionException Thrown if their are any problems.
	 */
	public static IEpisode getTVEpisode(MediaDirectory dir,File file,boolean useSources) throws ActionException {
		boolean refresh = false;
		try {
			for (IStore store : dir.getStores()) {
					if (file.getAbsolutePath().startsWith(dir.getMediaDirConfig().getMediaDir().getAbsolutePath())) {
					IEpisode ep = store.getEpisode(dir,file);
					if (ep!=null) {
						return ep;
					}
				}
			}
			SearchResult result = searchForId(dir,file,useSources);
			if (result==null) {
				log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_SHOW_ID_FOR_FILE"),file)); //$NON-NLS-1$
				return null;
			}

			IShow show =  dir.getShow(dir.getMediaDirConfig().getMediaDir(),file,result,refresh);
			if (show == null) {
				log.fatal(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_TO_FIND_SHOW_DETAILS_FOR_FILE"),file)); //$NON-NLS-1$
				return null;
			}

			ParsedFileName data =  FileNameParser.parse(dir.getMediaDirConfig(),file,result);
			if (data==null) {
				log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_TO_WORKOUT_SEASON_AND_EPISODE_NUMBER_FOR_FILE"),file.getName())); //$NON-NLS-1$
			}
			else {
				ISeason season = dir.getSeason(dir.getMediaDirConfig().getMediaDir(),file, show, data.getSeason(), refresh);
				if (season == null) {
					log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABVLE_TO_FIMD_SEASON"),file)); //$NON-NLS-1$
				} else {
					IEpisode episode = dir.getEpisode(dir.getMediaDirConfig().getMediaDir(),file, season, data.getEpisodes(), refresh,false);
					if (episode==null) {
						log.error(MessageFormat.format(Messages.getString("ActionPerformer.UNABLE_FIND_EPISODE_NUMBER"),file)); //$NON-NLS-1$
						return null;
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

	/**
	 * Used to get the part number of a film
	 * @param dir The media directory of the film
	 * @param file The media file
	 * @param film The film information
	 * @return The part number or null if it does not have one
	 */
	public static Integer getFilmPart(MediaDirectory dir,File file, IFilm film) {
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
			SearchDetails result = rp.getSearch(file, dir.getMediaDirConfig().getMediaDir(), dir.getMediaDirConfig().getPattern(), dir);
			if (result!=null) {
				part = result.getPart();
			}
		}
		return part;
	}
}
