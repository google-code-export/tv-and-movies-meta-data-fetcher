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

public class VideoFileInfo implements IVideoFileInfo {

	private Integer height;
	private Integer width;
	private long size;
	private Float frameRate;
	private AspectRatio aspectRatio;

	public VideoFileInfo(File mediaFile, VideoInfoParser parser) throws XMLParserException {
		height = parser.getHeight();
		width = parser.getWidth();
		size = mediaFile.length();
		frameRate = parser.getFrameRate();
		aspectRatio = AspectRatio.fromString(parser.getAspectRatio());
	}

	@Override
	public long getFileSize() {
		return size;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Float getFrameRate() {
		return frameRate;
	}

	@Override
	public AspectRatio getAspectRatio() {
		return aspectRatio;
	}

	@Override
	public boolean isWideScreen() {
		return false;
	}

	@Override
	public boolean isHighDef() {
		return false;
	}

	@Override
	public ResolutionFormat getResolutionFormat() {
		return null;
	}

}
