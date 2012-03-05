/*
 *  Copyright (C) 2008-2012  John-Paul.Stanford <dev@stanwood.org.uk>
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

import org.stanwood.media.xml.XMLParserException;

/** A implementation of the video file information interface */
public class VideoFileInfo implements IVideoFileInfo {

	private Integer height;
	private Integer width;
	private long size;
	private Float frameRate;
	private AspectRatio aspectRatio;
	private boolean interlaced;
	private ResolutionFormat resolutionFormat;

	/**
	 * The constructor
	 * @param mediaFile The file information is been retrieved on
	 * @param parser The parse of the media information
	 * @throws XMLParserException Thrown if their their is a XML parser problem
	 */
	public VideoFileInfo(File mediaFile, VideoInfoParser parser) throws XMLParserException {
		height = parser.getHeight();
		width = parser.getWidth();
		size = mediaFile.length();
		frameRate = parser.getFrameRate();
		aspectRatio = AspectRatio.fromString(parser.getAspectRatio());
		interlaced = parser.getInterlaced();
		if (width!=null && height!=null) {
			resolutionFormat = ResolutionFormat.getFormat(width, height, interlaced);
		}
	}

	/** {@inheritDoc} */
	@Override
	public long getFileSize() {
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public int getWidth() {
		return width;
	}

	/** {@inheritDoc} */
	@Override
	public int getHeight() {
		return height;
	}

	/** {@inheritDoc} */
	@Override
	public Float getFrameRate() {
		return frameRate;
	}

	/** {@inheritDoc} */
	@Override
	public AspectRatio getAspectRatio() {
		return aspectRatio;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isWideScreen() {
		return aspectRatio.isWideScreen();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isHighDef() {
		if (resolutionFormat!=null) {
			return resolutionFormat.isHighDef();
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public ResolutionFormat getResolutionFormat() {
		return resolutionFormat;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isInterlaced() {
		return interlaced;
	}
}
