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
package org.stanwood.media.cli.importer;

import java.io.File;

import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.IVideo;

/**
 * Used to store details of files imported from a watch directory
 */
public class ImportedEntry {

	private File oldName;
	private File newName;
	private MediaDirectory dir;
	private IVideo video;
	private Integer part;

	/**
	 * The constructor
	 * @param oldName The old name of the file
	 * @param newName The new name of the file
	 * @param dir The media directory
	 * @param video The media file information
	 * @param part The part number of the file or null
	 */
	public ImportedEntry(File oldName, File newName,MediaDirectory dir,IVideo video,Integer part) {
		super();
		this.oldName = oldName;
		this.newName = newName;
		this.dir = dir;
		this.video = video;
		this.part = part;
	}

	/**
	 * Used to get the old file name
	 * @return the old file name
	 */
	public File getOldName() {
		return oldName;
	}

	/**
	 * Used to get the new filename
	 * @return the new filename
	 */
	public File getNewName() {
		return newName;
	}

	/**
	 * Get the media directory of the entry
	 * @return The media directory
	 */
	public MediaDirectory getMediaDirectory() {
		return dir;
	}

	/**
	 * Get the video information of the entry
	 * @return The video info of the entry
	 */
	public IVideo getVideo() {
		return video;
	}

	/**
	 * Get the part number of the entry
	 * @return the part number of the entry
	 */
	public Integer getPart() {
		return part;
	}

}
