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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;

/**
 * <p>
 * This is a write only store that is used to store information in a format that can be used by the sapphire frontrow
 * plug-in. {@link "http://appletv.nanopi.net/"}. The details of the XML format can be found here: {@link
 * "http://appletv.nanopi.net/manual/overriding-metadata/"}.
 * </p>
 * <p>
 * Every time the {@link SapphireStore#cacheEpisode(File,Episode)} or the {@link SapphireStore#cacheFilm(File,Film)} 
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
	
	private final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
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
	public void cacheEpisode(File episodeFile, Episode episode) throws StoreException {
		try {
			writeEpisode(episodeFile, episode);
		} catch (IOException e) {
			throw new StoreException("Error creating spahire store", e);
		}
	}

	private File getCacheFile(File mediaFile) {
		int pos = mediaFile.getName().lastIndexOf('.');
		if (pos != -1) {
			String name = mediaFile.getName().substring(0, pos );
			File xmlFile = new File(mediaFile.getParent(), name + ".xml");
			return xmlFile;
		}
		return null;
	}

	private void writeEpisode(File file, Episode episode) throws FileNotFoundException {
		File xmlFile = getCacheFile(file);
		if (xmlFile != null) {
			if (xmlFile.exists()) {
				xmlFile.delete();
			}
			PrintStream ps = null;
			try {
				ps = new PrintStream(new FileOutputStream(xmlFile));

				ps.println("<media>");
				ps.println("  <title>" + episode.getTitle() + "</title>");
				ps.println("     <summary>" + episode.getSummary() + "</summary>");
//				ps.println("     <description></description>");
				// ps.println("     <publisher>Publisher</publisher>");
				// ps.println("     <composer>Composer</composer>");
				// ps.println("     <copyright>Copyright</copyright>");
				ps.println("     <userStarRating>" + Math.round((episode.getRating() / 10) * 5) + "</userStarRating>");
				// ps.println("     <rating>TV-PG</rating>");
				ps.println("     <seriesName>" + episode.getSeason().getShow().getName() + "</seriesName>");
				// ps.println("     <broadcaster>The CW</broadcaster>");
				ps.println("     <episodeNumber>" + episode.getEpisodeSiteId() + "</episodeNumber>");
				ps.println("     <season>" + episode.getSeason().getSeasonNumber() + "</season>");
				ps.println("     <episode>" + episode.getEpisodeNumber() + "</episode>");
				ps.println("     <published>" + df.format(episode.getDate()) + "</published>");
				ps.println("     <genres>");
				for (String genre : episode.getSeason().getShow().getGenres()) {
					// ps.println("        <genre primary="true">Mystery</genre>");
					ps.println("        <genre>" + genre + "</genre>");
				}
				ps.println("     </genres>");
				if (episode.getGuestStars() != null) {
					ps.println("     <cast>");
					for (Link cast : episode.getGuestStars()) {
						ps.println("        <name>" + cast + "</name>");
					}
					ps.println("     </cast>");
				}
				
				// ps.println("     <producers>");
				// ps.println("        <name>Rob Thomas</name>");
				// ps.println("     </producers>");
				if (episode.getDirectors() != null) {
					ps.println("     <directors>");
					for (Link director : episode.getDirectors()) {
						ps.println("       <name>" + director + "</name>");
					}
					ps.println("     </directors>");
				}

				ps.println("</media>");
			} finally {
				ps.close();
				ps = null;
			}
		} else {
			log.error("Unable to find extension of media file: " + file.getName());			
		}
	}

	private void writeFilm(File filmFile, Film film) throws FileNotFoundException {
		File xmlFile = getCacheFile(filmFile);
		if (xmlFile != null) {
			if (xmlFile.exists()) {
				xmlFile.delete();
			}
			PrintStream ps = null;
			try {
				ps = new PrintStream(new FileOutputStream(xmlFile));

				ps.println("<media>");
				ps.println("  <title>" + film.getTitle() + "</title>");
				ps.println("     <summary>" + film.getSummary() + "</summary>");
				 ps.println("     <description>"+film.getDescription()+"</description>");
				// ps.println("     <publisher>Publisher</publisher>");
				// ps.println("     <composer>Composer</composer>");
				// ps.println("     <copyright>Copyright</copyright>");
				ps.println("     <userStarRating>" + Math.round((film.getRating() / 10) * 5) + "</userStarRating>");
				ps.println("     <rating>" + findCert(film.getCertifications())+"</rating>");
				// ps.println("     <broadcaster>The CW</broadcaster>");
				ps.println("     <published>" + df.format(film.getDate()) + "</published>");
				ps.println("     <genres>");
				for (String genre : film.getGenres()) {
					if (genre.equals(film.getPreferredGenre())) {
						ps.println("        <genre primary=\"true\">" + genre + "</genre>");
					}
					else {
						ps.println("        <genre>" + genre + "</genre>");
					}
				}
				ps.println("     </genres>");
				if (film.getGuestStars() != null) {
					ps.println("     <cast>");
					for (Link cast : film.getGuestStars()) {
						ps.println("        <name>" + cast + "</name>");
					}
					ps.println("     </cast>");
				}
				// ps.println("     <producers>");
				// ps.println("        <name>Rob Thomas</name>");
				// ps.println("     </producers>");
				if (film.getDirectors() != null) {
					ps.println("     <directors>");
					for (Link director : film.getDirectors()) {
						ps.println("       <name>" + director + "</name>");
					}
				}
				ps.println("     </directors>");
				ps.println("</media>");
			} finally {
				ps.close();
				ps = null;
			}
		} else {
			log.error("Unable to find extension of media file: " + filmFile.getName());
		}
	}

	private String findCert(List<Certification> certifications) {
		if (preferedRating!=null) {
			for (Certification cert : certifications) {
				if (cert.getCountry().toLowerCase().trim().equals(preferedRating.toLowerCase().trim())) {
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
	public void cacheSeason(File episodeFile, Season season) {
	}

	/**
	 * Does nothing as it is not implemented for this store
	 * 
	 * @param show The show too store
	 * @param episodeFile the file witch the episode is stored in
	 */
	@Override
	public void cacheShow(File episodeFile, Show show) {
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * 
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode
	 * @param episodeFile the file which the episode is stored in
	 */
	@Override
	public Episode getEpisode(File episodeFile, Season season, int episodeNum) {
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
	public Season getSeason(File episodeFile, Show show, int seasonNum) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * 
	 * @param showId The id of the show
	 * @param episodeFile the file which the episode is stored in
	 */
	@Override
	public Show getShow(File episodeFile, String showId) {
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
	public Episode getSpecial(File episodeFile, Season season, int specialNumber) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * @param mode The mode that the search operation should be performed in
	 * @param episodeFile The file the episode is stored in
	 */
	@Override
	public SearchResult searchForVideoId(Mode mode,File episodeFile) {
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
	public void cacheFilm(File filmFile, Film film) throws StoreException {
		try {
			writeFilm(filmFile, film);
		} catch (IOException e) {
			throw new StoreException("Error creating spahire store", e);
		}
	}

	/**
	 * This will update all references of the old file to the new file
	 * 
	 * @param oldFile The old file
	 * @param newFile The new file
	 */
	@Override
	public void renamedFile(File oldFile, File newFile) {
		File oldXmlFile = getCacheFile(oldFile);
		File newXmlFile = getCacheFile(newFile);
		if (oldXmlFile.exists()) {

		}
		if (newXmlFile.exists()) {
			log.error("Unable rename '" + oldXmlFile.getName() + "' file too '" + newXmlFile.getName()
					+ "' as it already exists.");
		} else {
			log.error("Renaming '" + oldXmlFile.getName() + "' -> '" + newXmlFile.getName() + "'");

			if (!oldXmlFile.renameTo(newXmlFile)) {
				log.error("Failed to rename '" + oldXmlFile.getName() + "' file too '" + newXmlFile.getName()
						+ "'.");
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
	public Film getFilm(File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		return null;
	}

	/**
	 * Used to set the store parameter used to work out which rating should be used.
	 * If the parameter is not set, then this will return null.
	 * @return The preferred country certification, or null if not set.
	 */
	public String getPreferredCertificationCounrty() {
		return preferedRating;
	}

	/**
	 * Used to set the store parameter used to find which country's certification should be 
	 * used. If this is not set, then it will used the first if finds.
	 * @param country The country that should be used when getting the certification
	 */
	public void setPreferredCertificationCounrty(String country) {
		preferedRating = country;
	}
	
	
}
