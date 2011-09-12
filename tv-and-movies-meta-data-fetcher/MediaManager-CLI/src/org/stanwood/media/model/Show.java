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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is used to store and retrive information about shows
 */
public class Show implements IShow {
	private final String showId;

	private String longSummary;
	private String shortSummary;
	private List<String> genres = new ArrayList<String>();
	private String name;
	private URL imageURL;
	private URL showURL;
	private String sourceId;
	private String preferredGenre;
	private Map<String,String>extraInfo = new HashMap<String,String>();

	/**
	 * The constructor used to create a instance of the class
	 *
	 * @param showId The id of the show
	 */
	public Show(String showId) {
		this.showId = showId;
	}

	/**
	 * Used to set the long summary of the show
	 *
	 * @param longSummary The long summary of the show
	 */
	@Override
	public void setLongSummary(String longSummary) {
		this.longSummary = longSummary;
	}

	/**
	 * Used to set the show summary of the show
	 *
	 * @param shortSummary The short summary iof the show
	 */
	@Override
	public void setShortSummary(String shortSummary) {
		this.shortSummary = shortSummary;
	}

	/**
	 * Used to set the genres that the show belongs too
	 *
	 * @param genres The genres that the show belongs too
	 */
	@Override
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}

	/**
	 * Used to set the name/title of the show
	 *
	 * @param name The name of the show
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Used too set the URL which points to a summary of the show
	 * @param showURL The URL which points to a summary of the show
	 */
	@Override
	public void setShowURL(URL showURL) {
		this.showURL = showURL;
	}

	/**
	 * Used to get a long summary of the show
	 * @return The long summary of the show
	 */
	@Override
	public String getLongSummary() {
		return longSummary;
	}

	/**
	 * Used to get a short summary of the show
	 * @return The short summary of the show
	 */
	@Override
	public String getShortSummary() {
		return shortSummary;
	}

	/**
	 * Used to get the genres that the show belongs too
	 * @return The genres the show belongs too
	 */
	@Override
	public List<String> getGenres() {
		return genres;
	}

	/**
	 * Used to get the name/title of the show
	 * @return The name/title of the show
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Used to get the id of the show, which was defined by
	 * the source it was fetched from.
	 * @return The show id
	 */
	@Override
	public String getShowId() {
		return showId;
	}

	/**
	 * Used to get a URL which points too a image of the show
	 * @return A URL which points too a image of the show
	 */
	@Override
	public URL getImageURL() {
		return imageURL;
	}

	/**
	 * Used to set a URL which points too a image of the show
	 * @param imageURL A URL which points too a image of the show
	 */
	@Override
	public void setImageURL(URL imageURL) {
		this.imageURL = imageURL;
	}

	/**
	 * Used to get a URL which points to a summary of the show
	 * @return The URL which points to a summary of the show
	 */
	@Override
	public URL getShowURL() {
		return showURL;
	}

	/**
	 * Used to get the source id of the source that was used to retrieve the shows information.
	 * @return The source id
	 */
	@Override
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Used to set the source id of the source that was used to retrieve the shows information.
	 * @param sourceId The source id
	 */
	@Override
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Used to add a genre to the show
	 * @param genre The genre
	 */
	@Override
	public void addGenre(String genre) {
		genres.add(genre);
		Collections.sort(genres);
	}

	/**
	 * This is useful if the film belongs to more than one genres. It will returned the
	 * genre that is preferred.
	 * @return The preferred genre or null if not or flagged as preferred.
	 */
	@Override
	public String getPreferredGenre() {
		if (preferredGenre==null && genres.size()>0) {
			return genres.get(0);
		}
		return preferredGenre;
	}

	/**
	 * Used to set the genre that is preferred in the list of genres.
	 * @param preferredGenre The preferred genre
	 */
	@Override
	public void setPreferredGenre(String preferredGenre) {
		this.preferredGenre = preferredGenre;
	}

	/**
	 * Used to get extra information to a show that their are no getters/setters for in the regular fields
	 * @return The extra information in a map of key value pairs
	 */
	@Override
	public Map<String, String> getExtraInfo() {
		return extraInfo;
	}

	/**
	 * Used to add extra information to a show that their are no getters/setters for in the regular fields
	 * @param params The extra information in a map of key value pairs
	 */
	@Override
	public void setExtraInfo(Map<String, String> params) {
		this.extraInfo = params;
	}

}
