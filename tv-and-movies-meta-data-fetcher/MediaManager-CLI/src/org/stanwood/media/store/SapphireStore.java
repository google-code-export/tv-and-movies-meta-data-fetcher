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
package org.stanwood.media.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.ParameterType;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.util.FileHelper;

/**
 * <p>
 * This is a write only store that is used to store information in a format that can be used by the sapphire frontrow
 * plug-in. {@link "http://appletv.nanopi.net/"}. The details of the XML format can be found here: {@link
 * "http://appletv.nanopi.net/manual/overriding-metadata/"}.
 * </p>
 * <p>
 * Every time the {@link SapphireStore#cacheEpisode(File, File, Episode)} or the {@link SapphireStore#cacheFilm(File, File, Film, Integer)}
 * method is called, a XML file is written next to the episodes/films file with a
 * .xml extension.
 * </p>
 * <p>
 * This store has the optional parameter "PreferredCertificationCounrty". If this is set, then
 * when fetching the rating, this country's rating is used. If this is not set or the country can't
 * be found, then the first rating is used.
 * </p>
 */
public class SapphireStore implements IStore {

	private final static Log log = LogFactory.getLog(SapphireStore.class);

	private final static DecimalFormat EPISODE_FORMAT = new DecimalFormat("00"); //$NON-NLS-1$
	private final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private String preferedRating = null;



	/**
	 * This will store the episode and show details in a XML file next too the media file. The XML file will be in the
	 * format found here {@link "http://appletv.nanopi.net/manual/overriding-metadata/"}.
	 *
	 * @param episode The episode to the stored
	 * @param episodeFile the file which the episode is stored in
	 * @throws StoreException Thrown if their is a problem writing to the store
	 */
	@Override
	public void cacheEpisode(File rootMediaDir,File episodeFile, Episode episode) throws StoreException {
		try {
			writeEpisode(episodeFile, episode);
		} catch (IOException e) {
			throw new StoreException(Messages.getString("SapphireStore.ERROR_CREATING_STORE"), e); //$NON-NLS-1$
		}
	}

	private File getCacheFile(File mediaFile) {
		int pos = mediaFile.getName().lastIndexOf('.');
		if (pos != -1) {
			String name = mediaFile.getName().substring(0, pos );
			File xmlFile = new File(mediaFile.getParent(), name + ".xml"); //$NON-NLS-1$
			return xmlFile;
		}
		return null;
	}

	private void writeEpisode(File file, Episode episode) throws IOException {
		File xmlFile = getCacheFile(file);
		if (xmlFile != null) {
			if (xmlFile.exists() && !xmlFile.delete()) {
				throw new IOException(MessageFormat.format(Messages.getString("SapphireStore.UNABLE_DELETE_FILE"),xmlFile)); //$NON-NLS-1$
	        }
			PrintStream ps = null;
			try {
				ps = new PrintStream(new FileOutputStream(xmlFile));
				Season season  = episode.getSeason();
				Show show = episode.getSeason().getShow();

				ps.println("<media>"); //$NON-NLS-1$
				ps.println("  <title>" + episode.getTitle() + "</title>"); //$NON-NLS-1$ //$NON-NLS-2$
				ps.println("     <summary>" + episode.getSummary() + "</summary>"); //$NON-NLS-1$ //$NON-NLS-2$
//				ps.println("     <description></description>");
				// ps.println("     <publisher>Publisher</publisher>");
				// ps.println("     <composer>Composer</composer>");
				// ps.println("     <copyright>Copyright</copyright>");
				ps.println("     <userStarRating>" + Math.round((episode.getRating().getRating() / 10) * 5) + "</userStarRating>"); //$NON-NLS-1$ //$NON-NLS-2$
//				ps.println("     <rating>"+episode+"</rating>");
				ps.println("     <seriesName>" + show.getName() + "</seriesName>"); //$NON-NLS-1$ //$NON-NLS-2$
				// ps.println("     <broadcaster>The CW</broadcaster>");
				ps.println("     <episodeNumber>" + season.getSeasonNumber()+EPISODE_FORMAT.format(episode.getEpisodeNumber()) + "</episodeNumber>"); //$NON-NLS-1$ //$NON-NLS-2$
				ps.println("     <season>" + season.getSeasonNumber() + "</season>");  //$NON-NLS-1$//$NON-NLS-2$
				ps.println("     <episode>" + episode.getEpisodeNumber() + "</episode>"); //$NON-NLS-1$ //$NON-NLS-2$
				ps.println("     <published>" + DF.format(episode.getDate()) + "</published>"); //$NON-NLS-1$ //$NON-NLS-2$
				ps.println("     <genres>"); //$NON-NLS-1$
				for (String genre : show.getGenres()) {
					if (show.getPreferredGenre().equals(genre)) {
						ps.println("        <genre primary=\"true\">" + genre + "</genre>"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else {
						ps.println("        <genre>" + genre + "</genre>"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				ps.println("     </genres>"); //$NON-NLS-1$
				if (episode.getActors() != null) {
					ps.println("     <cast>"); //$NON-NLS-1$
					for (Actor cast : episode.getActors()) {
						ps.println("        <name>" + cast.getName() + "</name>"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					ps.println("     </cast>"); //$NON-NLS-1$
				}

				// ps.println("     <producers>");
				// ps.println("        <name>Rob Thomas</name>");
				// ps.println("     </producers>");
				if (episode.getDirectors() != null) {
					ps.println("     <directors>"); //$NON-NLS-1$
					for (String director : episode.getDirectors()) {
						ps.println("       <name>" + director + "</name>"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					ps.println("     </directors>"); //$NON-NLS-1$
				}

				ps.println("</media>"); //$NON-NLS-1$
			} finally {
				ps.close();
				ps = null;
			}
		} else {
			log.error(MessageFormat.format(Messages.getString("SapphireStore.UNABLE_FIND_EXT"),file.getName())); //$NON-NLS-1$
		}
	}

	private void writeFilm(File filmFile, Film film) throws IOException {
		File xmlFile = getCacheFile(filmFile);
		if (xmlFile != null) {
			if (xmlFile.exists() && !xmlFile.delete()) {
				throw new IOException(MessageFormat.format(Messages.getString("SapphireStore.UNABLE_DELETE_FILE"), xmlFile)); //$NON-NLS-1$
	        }
			PrintStream ps = null;
			try {
				ps = new PrintStream(new FileOutputStream(xmlFile));

				ps.println("<media>"); //$NON-NLS-1$
				ps.println("  <title>" + film.getTitle() + "</title>"); //$NON-NLS-1$ //$NON-NLS-2$
				ps.println("     <summary>" + film.getSummary() + "</summary>"); //$NON-NLS-1$ //$NON-NLS-2$
				if (film.getDescription()!=null) {
					ps.println("     <description>"+film.getDescription()+"</description>"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				// ps.println("     <publisher>Publisher</publisher>");
				// ps.println("     <composer>Composer</composer>");
				// ps.println("     <copyright>Copyright</copyright>");
				ps.println("     <userStarRating>" + Math.round((film.getRating().getRating() / 10) * 5) + "</userStarRating>"); //$NON-NLS-1$ //$NON-NLS-2$
				ps.println("     <rating>" + findCert(film.getCertifications())+"</rating>");  //$NON-NLS-1$//$NON-NLS-2$
				// ps.println("     <broadcaster>The CW</broadcaster>");
				ps.println("     <published>" + DF.format(film.getDate()) + "</published>"); //$NON-NLS-1$ //$NON-NLS-2$
				ps.println("     <genres>"); //$NON-NLS-1$
				for (String genre : film.getGenres()) {
					if (genre.equals(film.getPreferredGenre())) {
						ps.println("        <genre primary=\"true\">" + genre + "</genre>"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else {
						ps.println("        <genre>" + genre + "</genre>");  //$NON-NLS-1$//$NON-NLS-2$
					}
				}
				ps.println("     </genres>"); //$NON-NLS-1$
				if (film.getActors() != null) {
					ps.println("     <cast>"); //$NON-NLS-1$
					for (Actor cast : film.getActors()) {
						ps.println("        <name>" + cast.getName() + "</name>"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					ps.println("     </cast>"); //$NON-NLS-1$
				}
				// ps.println("     <producers>");
				// ps.println("        <name>Rob Thomas</name>");
				// ps.println("     </producers>");
				if (film.getDirectors() != null) {
					ps.println("     <directors>"); //$NON-NLS-1$
					for (String director : film.getDirectors()) {
						ps.println("       <name>" + director + "</name>"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				ps.println("     </directors>"); //$NON-NLS-1$
				ps.println("</media>"); //$NON-NLS-1$
			} finally {
				ps.close();
				ps = null;
			}
		} else {
			log.error(MessageFormat.format(Messages.getString("SapphireStore.UNABLE_FIND_EXT"),filmFile.getName())); //$NON-NLS-1$
		}
	}

	private String findCert(List<Certification> certifications) {
		if (preferedRating!=null) {
			for (Certification cert : certifications) {
				if (cert.getType().toLowerCase().trim().equals(preferedRating.toLowerCase().trim())) {
					return cert.getCertification();
				}
			}
		}

		return certifications.get(0).getCertification();
	}

	/**
	 * Does nothing as it is not implemented for this store
	 *
	 * @param season The season too store
	 * @param episodeFile the file witch the episode is stored in
	 */
	@Override
	public void cacheSeason(File rootMediaDir,File episodeFile, Season season) {
	}

	/**
	 * Does nothing as it is not implemented for this store
	 *
	 * @param show The show too store
	 * @param episodeFile the file witch the episode is stored in
	 */
	@Override
	public void cacheShow(File rootMediaDir,File episodeFile, Show show) {
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 *
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode
	 * @param episodeFile the file which the episode is stored in
	 */
	@Override
	public Episode getEpisode(File rootMediaDir,File episodeFile, Season season, int episodeNum) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 *
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season
	 * @param episodeFile the file which the episode is stored in
	 */
	@Override
	public Season getSeason(File rootMediaDir,File episodeFile, Show show, int seasonNum) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 *
	 * @param showId The id of the show
	 * @param episodeFile the file which the episode is stored in
	 */
	@Override
	public Show getShow(File rootMediaDir,File episodeFile, String showId) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 *
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode
	 * @param episodeFile the file which the episode is stored in
	 */
	@Override
	public Episode getSpecial(File rootMediaDir,File episodeFile, Season season, int specialNumber) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		return null;
	}

	/**
	 * This is used to write a film to the store.
	 *
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheFilm(File rootMediaDir,File filmFile, Film film,Integer part) throws StoreException {
		try {
			writeFilm(filmFile, film);
		} catch (IOException e) {
			throw new StoreException(Messages.getString("SapphireStore.ERROR_CREATING_STORE"), e); //$NON-NLS-1$
		}
	}

	/**
	 * This will update all references of the old file to the new file
	 *
	 * @param oldFile The old file
	 * @param newFile The new file
	 */
	@Override
	public void renamedFile(File rootMediaDir,File oldFile, File newFile) {
		File oldXmlFile = getCacheFile(oldFile);
		File newXmlFile = getCacheFile(newFile);
		if (oldXmlFile.exists()) {

		}
		if (newXmlFile.exists()) {
			log.error(MessageFormat.format(Messages.getString("SapphireStore.UNABLE_RENAME"),oldXmlFile.getName(),newXmlFile.getName())); //$NON-NLS-1$
		} else {
			log.error(MessageFormat.format(Messages.getString("SapphireStore.RENAMING"),oldXmlFile.getName(),newXmlFile.getName())); //$NON-NLS-1$

			if (!oldXmlFile.renameTo(newXmlFile)) {
				log.error(MessageFormat.format(Messages.getString("SapphireStore.9"),oldXmlFile.getName(),newXmlFile.getName())); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 *
	 * @param filmFile The file the film is stored in
	 * @param filmId The id of the film
	 */
	@Override
	public Film getFilm(File rootMediaDir,File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) {
		if (key.equalsIgnoreCase("PreferredCertificationCounrty")) { //$NON-NLS-1$
			preferedRating = value;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) {
		if (key.equalsIgnoreCase("PreferredCertificationCounrty")) { //$NON-NLS-1$
			return preferedRating;
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void performedActions(MediaDirectory dir) {

	}

	/** {@inheritDoc} */
	@Override
	public void fileDeleted(MediaDirectory dir, File file) throws StoreException {
		File xmlFile = getCacheFile(file);
		if (xmlFile.exists()) {
			try {
				FileHelper.delete(xmlFile);
			} catch (IOException e) {
				throw new StoreException(MessageFormat.format(Messages.getString("SapphireStore.10"),xmlFile.getAbsolutePath()),e); //$NON-NLS-1$
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Episode getEpisode(MediaDirectory dir, File file)
			throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Film getFilm(MediaDirectory dir, File file) throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void init(File nativeDir) throws StoreException {
	}

	/** {@inheritDoc} */
	@Override
	public ParameterType[] getParameters() {
		return new ParameterType[0];
	}
}
