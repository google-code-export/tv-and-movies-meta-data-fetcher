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

import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.List;

/**
 * This is used to hold the results of searching for a show id
 */
public class SearchResult {

	private String id;
	private String sourceId;
	private String url;
	private String title;
	private Integer part;
	private Mode mode;
	private Integer season;
	private List<Integer> episodes;

	/**
	 * Constructor of the class
	 * @param id The id of the show that was found
	 * @param sourceId The id of the source that it was found in
	 * @param url The URL of the show
	 * @param part The part number of the media, or null if not known/supported
	 * @param mode The media type
	 */
	public SearchResult(String id, String sourceId,String url,Integer part,Mode mode) {
		super();
		if (id == null || id.length()==0) {
			throw new InvalidParameterException(MessageFormat.format(Messages.getString("SearchResult.INVALID_ID"),id)); //$NON-NLS-1$
		}
		if (sourceId == null || sourceId.length()==0) {
			throw new InvalidParameterException(MessageFormat.format(Messages.getString("SearchResult.INVALID_SOURCEID"),sourceId)); //$NON-NLS-1$
		}
		if (url != null && url.length()==0) {
			throw new InvalidParameterException(MessageFormat.format(Messages.getString("SearchResult.INVALID_URL"),url)); //$NON-NLS-1$
		}

		this.id = id;
		this.sourceId = sourceId;
		this.url = url;
		this.part = part;
		this.mode = mode;
	}

	/**
	 * The id of the show, or null if it can't be found
	 *
	 * @return The id of the show
	 */
	public String getId() {
		return id;
	}

	/**
	 * The source if of the source that was used to find the show id.
	 *
	 * @return The id of the source associated with the show id
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Used to get the url of the show that was found.
	 * @return the url The show url
	 */
	public String getUrl() {
		return url;
	}


	/**
	 * Returns a string representation of the result
	 * @return string representation of the result
	 */
	@Override
	public String toString() {
		return id+":"+sourceId+" - (" + url+") - ("+title+")" ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * Used to set the title of the URL if one could be found
	 * @param title The title of the URL
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Used to get the title of the URL if one was found
	 * @return The title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Used to get the part number if one was found
	 * @return The part number or null if it was not found
	 */
	public Integer getPart() {
		return part;
	}

	/**
	 * Used to get the media results type
	 * @return The media type
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * Used to get the season number
	 * @return the season number, or null if it could not be found
	 */
	public Integer getSeason() {
		return season;
	}

	/**
	 * Used to set the season number
	 * @param season The season number
	 */
	public void setSeason(Integer season) {
		this.season = season;
	}

	/**
	 * Used to get the episode numbers
	 * @return the episode numbers, or null if it could not be found
	 */
	public List<Integer> getEpisodes() {
		return episodes;
	}

	/**
	 * Used to set the episode numbers
	 * @param episodes The episode numbers
	 */
	public void setEpisodes(List<Integer> episodes) {
		this.episodes = episodes;
	}


}
