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
 * Used to enumerate know video resolution formats
 */
public enum ResolutionFormat {

	/** Used to represent the video resolution format 480i */
	Format_480i(false,640,480,true),
	/** Used to represent the video resolution format 480p */
	Format_480p(false,640,480,false),
	/** Used to represent the video resolution format 576i */
	Format_576i(false,576,576,true),
	/** Used to represent the video resolution format 576p */
	Format_576p(false,576,576,false),
	/** Used to represent the video resolution format 720p */
	Format_720p(true,1280,720,false),
	/** Used to represent the video resolution format 720i */
	Format_720i(true,1280,720,true),
	/** Used to represent the video resolution format 1080i */
	Format_1080i(true,1280,1080,true),
	/** Used to represent the video resolution format 1080p */
	Format_1080p(true,1920,1080,false);

	private boolean highDef;
	private int width;
	private int height;
	private AspectRatio ratio;
	private boolean interlaced;

	private ResolutionFormat(boolean highDef,int x,int y, boolean interlaced) {
		this.highDef = highDef;
		this.width = x;
		this.height = y;
		this.ratio = AspectRatio.fromRatio(((double)x)/((double)y));
		this.interlaced = interlaced;
	}

	/**
	 * Returns true if the format is high def
	 * @return true if the format is high def
	 */
	public boolean isHighDef() {
		return highDef;
	}

	/**
	 * Used to get the formats width in pixels at the default aspect ratio
	 * @return the formats width in pixels
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Used to get the formats height in pixels at the default aspect ratio
	 * @return the formats height in pixels
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Used to get the formats default aspect ratio
	 * @return the formats default aspect ratio
	 */
	public AspectRatio getRatio() {
		return ratio;
	}

	/**
	 * Used to find out if the format is interlaced
	 * @return true if the format is interlaced
	 */
	public boolean isInterlaced() {
		return interlaced;
	}

	/**
	 * Used to get the format based on a videos parameters
	 * @param width The width of the video in pixels
	 * @param height The height of the video in pixels
	 * @param interlaced True if the video scan type is interlaced
	 * @return The resolution format or null if it can't be determined
	 */
	public static ResolutionFormat getForamt(int width,int height,boolean interlaced) {
		for (ResolutionFormat f : values()) {
			if (f.interlaced == interlaced && f.width == width) {
				for (AspectRatio aspectRatio : AspectRatio.values()) {
					int height1 = (int) (width * aspectRatio.getValue());
					if (height == height1) {
						return f;
					}
				}
			}
		}
		return null;
	}
}
