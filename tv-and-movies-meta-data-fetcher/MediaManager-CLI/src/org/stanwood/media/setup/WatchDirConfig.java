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
package org.stanwood.media.setup;

import java.io.File;

/**
 * Used to store information about a watched directory
 */
public class WatchDirConfig {

	private File watchDir;

	/**
	 * Used to get the directory been watched
	 * @return the directory been watched
	 */
	public File getWatchDir() {
		return watchDir;
	}

	/**
	 * Used to set the directory been watched
	 * @param watchDir the directory been watched
	 */
	public void setWatchDir(File watchDir) {
		this.watchDir = watchDir;
	}


}
