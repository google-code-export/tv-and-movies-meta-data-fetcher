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
package org.stanwood.media.search;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.lang.StringUtils;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.setup.WatchDirConfig;

/**
 * Helper class for search operations releated to NFO files
 */
public class NFOSearchHelper {

	private static File findNFOfileInDir(MediaDirectory rootMediaDir,File parentDir) {
		while (isMediaDir(rootMediaDir, parentDir) ) {

			// Now check that their is one nfo file in the directory
			File files[] = parentDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File arg0, String arg1) {
					return StringUtils.endsWithIgnoreCase(arg1, ".nfo"); //$NON-NLS-1$
				}
			});
			if (files.length==1) {
				 return files[0];
			}
			parentDir = parentDir.getParentFile();
		}
		return null;
	}

	private static boolean isMediaDir(MediaDirectory rootMediaDir,File parentDir) {
		if (parentDir==null) {
			return false;
		}
		else if (parentDir.equals(rootMediaDir.getMediaDirConfig().getMediaDir())) {
			return false;
		}
		else {
			for (WatchDirConfig wd : rootMediaDir.getController().getWatchDirectories()) {
				if (parentDir.equals(wd.getWatchDir())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Used to find a NFO file if their is one for a given media file
	 * @param rootMediaDir The media directory been processed
	 * @param mediaFile The media file
	 * @return The NFO file or NULL if not found
	 */
	public static File findNFOfile(MediaDirectory rootMediaDir, File mediaFile) {
		return findNFOfileInDir(rootMediaDir,mediaFile.getParentFile());
	}
}
