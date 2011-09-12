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
package org.stanwood.media.model;

import java.net.URL;

/** Base interface for all season classes */
public interface ISeason {

	/**
	 * Used to get the episode listing URL for the season
	 * @return The episode listing URL for the season
	 */
	public URL getURL();

	/**
	 * Sets the episode listing URL for the season
	 * @param url The episode listing URL for the season
	 */
	public void setURL(URL url);

	/**
	 * Get the number of the season
	 * @return The season number
	 */
	public int getSeasonNumber();

	/**
	 * Get the show the season belongs too
	 * @return The show the season belongs too
	 */
	public IShow getShow();

}
