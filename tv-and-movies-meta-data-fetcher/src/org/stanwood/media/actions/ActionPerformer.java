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
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;


/**
 * This class is used to perform a list of actions upon files in a media directory
 */
public class ActionPerformer implements IActionEventHandler {

	private final static Log log = LogFactory.getLog(ActionPerformer.class);

	private List<String> exts;
	private MediaDirectory dir;
	private List<IAction> actions;

	private ArrayList<File> dirs;

	private ArrayList<File> sortedFiles;


	/**
	 * Constructor used to create a instance of the class
	 * @param actions List of actions to perform
	 * @param dir The media directory
	 * @param exts The extensions to search for
	 */
	public ActionPerformer(List<IAction> actions,MediaDirectory dir,List<String> exts) {
		this.dir = dir;
		this.exts = exts;
		this.actions = actions;
	}

	/**
	 * Used to perform the actions
	 * @throws ActionException Thrown if their are any errors with the actions
	 */
	public void performActions() throws ActionException {
		findMediaFiles();

		performActionsFiles(sortedFiles);
		performActionsDirs(sortedFiles);

		for (IStore store : dir.getStores()) {
			try {
				store.performedActions(dir);
			} catch (StoreException e) {
				log.error("Unable to clean up store: " + store.getClass().getName(),e);
			}
		}
	}

	protected void findMediaFiles() throws ActionException {
		List<File>mediaFiles = new ArrayList<File>();
		dirs = new ArrayList<File>();
		findMediaFiles(dir.getMediaDirConfig().getMediaDir(),mediaFiles,dirs);
		sortedFiles = new ArrayList<File>();
		for (File file : mediaFiles) {
			sortedFiles.add(file);
		}
		Collections.sort(sortedFiles,new Comparator<File>() {
			@Override
			public int compare(File arg0, File arg1) {
				return arg0.getAbsolutePath().compareTo(arg1.getAbsolutePath());
			}
		});
	}

	private void findMediaFiles(File parentDir,List<File>mediaFiles,List<File>mediaDirs) throws ActionException {
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
		for (File f : files) {
			mediaFiles.add(f);
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
			mediaDirs.add(dir);
			findMediaFiles(dir,mediaFiles,mediaDirs);
		}
	}

	private void performActionsFiles(List<File> files) throws ActionException {
		log.info(("Processing "+files.size()+" files"));
		for (File file : files) {
			for (IAction action : actions) {
				action.perform(dir, file,this);
			}
		}
		log.info("Finished");
	}

	private void performActionsDirs(List<File> files) throws ActionException {
		log.info(("Processing "+files.size()+" dirs"));
		for (File file : files) {
			for (IAction action : actions) {
				action.performOnDirectory(dir, file,this);
			}
		}
		log.info("Finished");
	}

	@Override
	public void sendEventNewFile(File file) {
	}

	@Override
	public void sendEventDeletedFile(File file) {
	}

	@Override
	public void sendEventRenamedFile(File oldFile, File newFile) throws ActionException {
		try {
			dir.renamedFile(dir.getMediaDirConfig().getMediaDir(),oldFile,newFile);
		} catch (StoreException e) {
			throw new ActionException("Unable to rename file",e);
		}
	}

}
