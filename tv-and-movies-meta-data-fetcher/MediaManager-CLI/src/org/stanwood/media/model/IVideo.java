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

import java.util.List;

/**
 * This interface is used to define the methods that are common for any type of video
 * that can be shown as a tv show or a film.
 */
public interface IVideo {

	/**
	 * Used to get the video title.
	 * @return The video title.
	 */
	public String getTitle();

	/**
	 * Used to set the title of the video
	 * @param title The title of the video
	 */
	public void setTitle(String title);

	/**
	 * Used to get a list of directors for the video
	 * @return A list of directors for the video
	 */
	public List<String> getDirectors();

	/**
	 * Used to set a list of directors for the episode
	 * @param directors The list of directors for the episode
	 */
	public void setDirectors(List<String> directors);
	/**
	 * Used to get a list of writers for the video
	 * @return Get a list of writers for the video
	 */
	public List<String> getWriters();

	/**
	 * Used to set a list of writers for the video
	 * @param writers The list of writers
	 */
	public void setWriters(List<String> writers);

	/**
	 * Used to get a summary of the video
	 * @return The summary of the video
	 */
	public String getSummary();

	/**
	 * Used to set the films summary
	 * @param summary The films summary
	 */
	public void setSummary(String summary);

	/**
	 * Used to get a list of files that belong to the video entry
	 * @return The list of files
	 */
	public List<VideoFile> getFiles();

	/**
	 * Used to set the list of files that belong to the video entry
	 * @param videoFiles The list of files
	 */
	public void setFiles(List<VideoFile> videoFiles);

}
