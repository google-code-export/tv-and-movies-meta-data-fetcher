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

/**
 * This is used to hold the results of searching for a show id
 */
public class SearchResult {

	private long id;
	private String sourceId;

	/**
	 * Constructor of the class
	 * @param id The id of the show that was found
	 * @param sourceId The id of the source that it was found in
	 */
	public SearchResult(long id, String sourceId) {
		super();
		this.id = id;
		this.sourceId = sourceId;
	}

	/**
	 * The id of the show, or null if it can't be found
	 * 
	 * @return The id of the show
	 */
	public long getId() {
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

	
}
