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
import java.util.List;

/**
 * Interface for all the film classes
 */
public interface IFilm extends IVideo,IVideoActors,IVideoGenre,IVideoRating, IVideoCertification {

	/**
	 * Used to get the id of the film used by the source that it was read from.
	 * @return The id of the film
	 */
	public String getId();

	/**
	 * Used to set the id of the film used by the source that it was read from.
	 * @param id The id of the film
	 */
	public void setId(String id);

	/**
	 * Used to get the source id of the source that was used to retrieve the film information.
	 * @return The source id
	 */
	public String getSourceId();

	/**
	 * Used to set the source id of the source that was used to retrieve the film information.
	 * @param sourceId The source id
	 */
	public void setSourceId(String sourceId);

	/**
	 * Used to set the URL used to get a summary of the film
	 * @param url The summary URL
	 */
	public void setFilmUrl(URL url);

	/**
	 * Used to get the URL used to get a summary of the film
	 * @return The summary URL
	 */
	public URL getFilmUrl();


	/**
	 * Used to get the release date of the film
	 * @return The release date of the film
	 */
	public Date getDate();

	/**
	 * Used to set the release date of the film
	 * @param date The release date of the film
	 */
	public void setDate(Date date);

	/**
	 * Used to set the URL of the film poster
	 * @param imageURL The URL of the film poster.
	 */
	public void setImageURL(URL imageURL);

	/**
	 * Used to get the URL of the film poster. This will return null if
	 * a poster could not be found.
	 * @return The film poster, or null if it does not have one
	 */
	public URL getImageURL();

	/**
	 * Used to add a chapter to the film
	 * @param chapter The chapter to add
	 */
	public void addChapter(Chapter chapter);

	/**
	 * Used to get the chapters of the film
	 * @return The chapters of the film
	 */
	public List<Chapter> getChapters();

	/**
	 * Used to set the chapter information for the film
	 * @param chapters The chapters of the film
	 */
	public void setChapters(List<Chapter> chapters);

	/**
	 * Used to set the films long description
	 * @param description The films long description
	 */
	public void setDescription(String description);

	/**
	 * Used to get the films long description
	 * @return the films long description
	 */
	public String getDescription();

	/**
	 * This will return the country the film was made in, or
	 * null if it's not known.
	 * @return the country the film was made in.
	 */
	public String getCountry();

	/**
	 * Used to set the country the film was made in.
	 * @param country the country to set
	 */
	public void setCountry(String country);

}
