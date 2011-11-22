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

/** Base interface for all show classes */
public interface IShow extends IVideoGenre, IVideoExtra, IVideoCertification {

	/**
	 * Used to set the long summary of the show
	 *
	 * @param longSummary The long summary of the show
	 */
	public void setLongSummary(String longSummary);

	/**
	 * Used to set the show summary of the show
	 *
	 * @param shortSummary The short summary iof the show
	 */
	public void setShortSummary(String shortSummary);

	/**
	 * Used to set the name/title of the show
	 *
	 * @param name The name of the show
	 */
	public void setName(String name);

	/**
	 * Used too set the URL which points to a summary of the show
	 * @param showURL The URL which points to a summary of the show
	 */
	public void setShowURL(URL showURL);

	/**
	 * Used to get a long summary of the show
	 * @return The long summary of the show
	 */
	public String getLongSummary();

	/**
	 * Used to get a short summary of the show
	 * @return The short summary of the show
	 */
	public String getShortSummary();

	/**
	 * Used to get the name/title of the show
	 * @return The name/title of the show
	 */
	public String getName();

	/**
	 * Used to get the id of the show, which was defined by
	 * the source it was fetched from.
	 * @return The show id
	 */
	public String getShowId();

	/**
	 * Used to get a URL which points too a image of the show
	 * @return A URL which points too a image of the show
	 */
	public URL getImageURL();

	/**
	 * Used to set a URL which points too a image of the show
	 * @param imageURL A URL which points too a image of the show
	 */
	public void setImageURL(URL imageURL);

	/**
	 * Used to get a URL which points to a summary of the show
	 * @return The URL which points to a summary of the show
	 */
	public URL getShowURL();

	/**
	 * Used to get the source id of the source that was used to retrieve the shows information.
	 * @return The source id
	 */
	public String getSourceId();

	/**
	 * Used to set the source id of the source that was used to retrieve the shows information.
	 * @param sourceId The source id
	 */
	public void setSourceId(String sourceId);

	public String getStudio();

	public void setStudio(String studio);


}
