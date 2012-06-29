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

import java.util.List;

/**
 * This class is used to hold information about a episode that has been parsed
 * with the @see FileNameParser.
 */
public class ParsedFileName {

	private int season;
	private List<Integer> episodes;
	private String term;

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
	 * Used to get the episode numbers of the file
	 * @return The episode numbers of the file
	 */
	public List<Integer> getEpisodes() {
		return episodes;
	}

	/**
	 * Used to set the episode numbers of the file
	 * @param episodes The episode numbers of the file
	 */
	public void setEpisodes(List<Integer> episodes) {
		this.episodes = episodes;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return term+" - "+season+"x"+episodes.toString();  //$NON-NLS-1$//$NON-NLS-2$
	}


}
