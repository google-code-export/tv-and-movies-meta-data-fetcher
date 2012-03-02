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
package org.stanwood.media.info;

import java.io.File;

import org.stanwood.media.logging.StanwoodException;
import org.w3c.dom.Document;

/**
 * Factory call used to get the correct {@link IMediaFileInfo} implemention for the file.
 */
public class MediaInfoFactory {

	/**
	 * Used to get the media information object
	 * @param mediaFile The media file
	 * @param document The XML information about the file
	 * @return The file information
	 * @throws StanwoodException Thrown if their are any problems
	 */
	public static IMediaFileInfo createMediaInfo(File mediaFile,Document document) throws StanwoodException {
		VideoInfoParser parser = new VideoInfoParser(document);
		return new VideoFileInfo(mediaFile,parser);
	}

}
