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

/**
 * This interface is used to request information about video files
 */
public interface IVideoFileInfo extends IMediaFileInfo {

	/**
	 * Used to get the width in pixels of the video
	 * @return the width in pixels of the video
	 */
	public int getWidth();

	/**
	 * Used to get the height in pixels of the video
	 * @return the height in pixels of the video
	 */
	public int getHeight();

	/**
	 * Used to get the frame rate of the video in frams per second (fps)
	 * @return the frame rate of the video in frams per second (fps)
	 */
	public Float getFrameRate();

	/**
	 * Used to get the display aspect ratio of the video
	 * @return the display aspect ratio of the video
	 */
	public AspectRatio getAspectRatio();

	/**
	 * Used to find out if the video is wide screen
	 * @return True if the video is widescreen
	 */
	public boolean isWideScreen();

	/**
	 * Used to find out if the video is high definition
	 * @return True if the video is high definition
	 */
	public boolean isHighDef();

	/**
	 * Used to get the video resolution format if it's know. If not known,
	 * them this will return null.
	 * @return The resolution format or null
	 */
	public ResolutionFormat getResolutionFormat();

	/**
	 * Used to find out if the video scan type is interlaced or progressive
	 * @return true if the scan type is interlaced
	 */
	public boolean isInterlaced();
}
