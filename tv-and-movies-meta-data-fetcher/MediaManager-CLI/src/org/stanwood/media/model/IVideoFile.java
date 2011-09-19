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

import java.io.File;

/**
 * Interface for video files
 */
public interface IVideoFile {

	/**
	 * Used to get the current location of the video file
	 * @return The current location of the video file
	 */
	public File getLocation();

	/**
	 * Used to get the original location of the video file. This is the location
	 * it was first seen in.
	 * @return The original location of the file
	 */
	public File getOrginalLocation();

	/**
	 * Used to set the original location of the video file. This is the location
	 * it was first seen in.
	 * @param orginalLocation The original location of the file
	 */
	public void setOrginalLocation(File orginalLocation);

	/**
	 * Used to set the current location of the video file
	 * @param location The current location of the video file
	 */
	public void setLocation(File location);

	/**
	 * Used to get the part number of the file
	 * @return The part number or null if not known/supported
	 */
	public Integer getPart();

	/**
	 * Used to set the part number of the file
	 * @param part The part number or null if not known/supported
	 */
	public void setPart(Integer part);

	/**
	 * Returns the media directory the file is in
	 * @return the media directory the file is in
	 */
	public File getMediaDirectory();
}
