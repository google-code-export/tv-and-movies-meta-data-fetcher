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
import java.util.Date;

/**
 * A base interface for all episodes
 */
public interface IEpisode extends IVideo, IVideoActors, IVideoRating {

	/**
	 * Get the season the episode belongs too
	 * @return The season the episode belongs too
	 */
	public ISeason getSeason();

	/**
	 * Gets the number of the episode.
	 * @return The number of the episode
	 */
	public int getEpisodeNumber();

	/**
	 * Sets the number of the episode.
	 * @param episodeNumner
	 */
	public void setEpisodeNumber(int episodeNumner);

	/**
	 * Sets the air date of the show
	 * @param airDate The air date of the show
	 */
	public void setDate(Date airDate);

	/**
	 * Used to find out if this is a special
	 * @return True if special, otherwise false
	 */
	public boolean isSpecial();

	/**
	 * Gets the first air date of the episode
	 * @return The first air date of the episode
	 */
	public Date getDate();


	/**
	 * Used to set the URL used to get a summary of the show
	 * @param url The summary URL
	 */
	public void setUrl(URL url);

	/**
	 * Used to get the URL used to get a summary of the show
	 * @return The summary URL
	 */
	public URL getUrl();

	/**
	 * Used to get the numeric unique episode id used by the source
	 * @return the numeric unique episode id used by the source
	 */
	public String getEpisodeId();

	/**
	 * Used to set the numeric unique episode id used by the source
	 * @param episodeId The numeric unique episode id used by the source
	 */
	public void setEpisodeId(String episodeId);

	/**
	 * Used to get a URL which points to a image of the episode
	 * @return A URL which points too a image of the episode
	 */
	public URL getImageURL();

	/**
	 * Used to set a URL which points too a image of the episode
	 * @param imageURL A URL which points too a image of the episode
	 */
	public void setImageURL(URL imageURL);

	/**
	 * Used to flag this as a special episode
	 * @param special The special value to set
	 */
	public void setSpecial(boolean special);

}
