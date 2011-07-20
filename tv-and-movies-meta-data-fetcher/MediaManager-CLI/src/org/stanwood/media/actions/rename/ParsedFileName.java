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
package org.stanwood.media.actions.rename;

/**
 * This class is used to hold information about a episode that has been parsed
 * with the @see FileNameParser.
 */
public class ParsedFileName {

	private int season;
	private int episode;
	
	/**
	 * Returns the season of the file
	 * @return the season of the file
	 */
	public int getSeason() {
		return season;
	}
	
	/**
	 * Used to set the season of the file
	 * @param season the season of the file
	 */
	public void setSeason(int season) {
		this.season = season;
	}
	
	/**
	 * Used to get the episode number of the file
	 * @return The episode number of the file
	 */
	public int getEpisode() {
		return episode;
	}
	
	/**
	 * Used to set the episode number of the file
	 * @param episode The episode number of the file 
	 */
	public void setEpisode(int episode) {
		this.episode = episode;
	}
}
