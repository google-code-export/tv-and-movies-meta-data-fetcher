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

/**
 * This is used to hold information on a season within a show
 */
public class Season {

	private int seasonNumber;

	private URL url;
	private Show show;

	/**
	 * The constructor used to create a instance of the season.
	 * @param show The show the season belongs too
	 * @param seasonNumber The season number
	 */
	public Season(Show show,int seasonNumber) {
		this.seasonNumber = seasonNumber;
		this.show = show;
	}

	/**
	 * Used to get the episode listing URL for the season
	 * @return The episode listing URL for the season
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Sets the episode listing URL for the season
	 * @param seasonListingUrl The episode listing URL for the season
	 */
	public void setURL(URL url) {
		this.url = url;
	}

	/**
	 * Get the number of the season
	 * @return The season number
	 */
	public int getSeasonNumber() {
		return seasonNumber;
	}

	/**
	 * Get the show the season belongs too
	 * @return The show the season belongs too
	 */
	public Show getShow() {
		return show;
	}

}
