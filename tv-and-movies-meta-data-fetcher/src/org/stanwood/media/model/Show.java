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
package org.stanwood.media.model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is used to store and retrive information about shows
 */
public class Show {
	private final long showId;

	private String longSummary;
	private String shortSummary;
	private List<String> genres = new ArrayList<String>();
	private String name;
	private URL imageURL;
	private URL showURL;
	private File showDirectory;
	private List<Season> seasons = new ArrayList<Season>();
	private String sourceId;

	/**
	 * The constructor used to create a instance of the class
	 * 
	 * @param showDirectory The directory the show is located in
	 * @param showId The id of the show
	 */
	public Show(File showDirectory, long showId) {
		this.showId = showId;
		this.showDirectory = showDirectory;
	}

	/**
	 * Used to set the long summary of the show
	 * 
	 * @param longSummary The long summary of the show
	 */
	public void setLongSummary(String longSummary) {
		this.longSummary = longSummary;
	}

	/**
	 * Used to set the show summary of the show
	 * 
	 * @param shortSummary The short summary iof the show
	 */
	public void setShortSummary(String shortSummary) {
		this.shortSummary = shortSummary;
	}

	/**
	 * Used to set the genres that the show belongs too
	 * 
	 * @param genres The genres that the show belongs too
	 */
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}

	/**
	 * Used to set the name/title of the show
	 * 
	 * @param name The name of the show
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Used too set the URL which points to a summary of the show
	 * @param showURL The URL which points to a summary of the show
	 */
	public void setShowURL(URL showURL) {
		this.showURL = showURL;
	}

	/**
	 * Used to get the directory the shows media files are located in
	 * @return The directory the shows media files are located in
	 */
	public File getShowDirectory() {
		return showDirectory;
	}

	/**
	 * Used to get a long summary of the show
	 * @return The long summary of the show
	 */
	public String getLongSummary() {
		return longSummary;
	}

	/**
	 * Used to get a short summary of the show
	 * @return The short summary of the show
	 */
	public String getShortSummary() {
		return shortSummary;
	}

	/**
	 * Used to get the genres that the show belongs too
	 * @return The genres the show belongs too
	 */
	public List<String> getGenres() {
		return genres;
	}

	/**
	 * Used to get the name/title of the show
	 * @return The name/title of the show
	 */
	public String getName() {
		return name;
	}

	/**
	 * Used to get the id of the show, which was defined by
	 * the source it was fetched from.
	 * @return The show id
	 */
	public long getShowId() {
		return showId;
	}
	
	/** 
	 * Used to get a URL which points too a image of the show
	 * @return A URL which points too a image of the show
	 */
	public URL getImageURL() {
		return imageURL;
	}

	/**
	 * Used to set a URL which points too a image of the show
	 * @param imageURL A URL which points too a image of the show
	 */
	public void setImageURL(URL imageURL) {
		this.imageURL = imageURL;
	}

	/**
	 * Used to get a URL which points to a summary of the show
	 * @return The URL which points to a summary of the show
	 */
	public URL getShowURL() {
		return showURL;
	}

	/**
	 * Used to get a season from the show with the given season number.
	 * If the season can't be found, then it will return null.
	 * @param seasonNum The number of the season too fetch.
	 * @return The season, or null if it can't be found
	 */
	public Season getSeason(int seasonNum) {
		for (Season season : seasons) {
			if (season.getSeasonNumber() == seasonNum) {
				return season;
			}
		}
		return null;
	}

	/**
	 * Used to remove a season with the given season number from the show.
	 * @param seasonNumber The season number of the season to remove
	 */
	public void removeSeason(int seasonNumber) {
		Iterator<Season> it = seasons.iterator();
		while (it.hasNext()) {
			Season foundSeason = it.next();
			if (foundSeason.getSeasonNumber() == seasonNumber) {
				it.remove();
			}
		}
	}

	/**
	 * Used to add a season to the show.
	 * @param season The season to add to the show.
	 */
	public void addSeason(Season season) {
		seasons.add(season);
	}

	/**
	 * Used to get the source id of the source that was used to retrieve the shows information.
	 * @return The source id
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Used to set the source id of the source that was used to retrieve the shows information.
	 * @param sourceId The source id
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
}
