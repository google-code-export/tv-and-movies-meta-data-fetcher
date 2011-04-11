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
package org.stanwood.media.actions;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;


public class ActionPerformer {


	private final static Log log = LogFactory.getLog(ActionPerformer.class);

	private String[] exts;
	private MediaDirectory dir;
	private List<IAction> actions;


	/**
	 * Constructor used to create a instance of the class
	 * @param actions List of actions to perform
	 * @param dir The media directory
	 * @param exts The extensions to search for
	 */
	public ActionPerformer(List<IAction> actions,MediaDirectory dir,String exts[]) {
		this.dir = dir;
		this.exts = exts.clone();
		this.actions = actions;
	}

	public boolean performActions() throws ActionException {
		return processDirectory(dir.getMediaDirConfig().getMediaDir());
	}

	private boolean processDirectory(File parentDir) throws ActionException {
		if (log.isDebugEnabled()) {
			log.debug("Tidying show names in the directory : " + parentDir);
		}
		File files[] = parentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isFile()) {
					String name = file.getName();
					for (String ext : exts) {
						if (name.toLowerCase().endsWith("."+ext.toLowerCase())) {
							return true;
						}
					}
				}
				return false;
			}
		});

		List<File> sortedFiles = new ArrayList<File>();
		for (File file : files) {
			sortedFiles.add(file);
		}
		Collections.sort(sortedFiles,new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				return arg0.getAbsolutePath().compareTo(arg1.getAbsolutePath());
			}
		});

		for (File file : sortedFiles) {
			if (!performActions(file)) {
				return false;
			}
		}

		File dirs[] = parentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		for (File dir : dirs) {
			processDirectory(dir);
		}

		return true;
	}

	private boolean performActions(File file) throws ActionException {
		for (IAction action : actions) {
			file = action.perform(dir, file);
			if (file==null) {
				return false;
			}
		}
		return true;
	}


}
