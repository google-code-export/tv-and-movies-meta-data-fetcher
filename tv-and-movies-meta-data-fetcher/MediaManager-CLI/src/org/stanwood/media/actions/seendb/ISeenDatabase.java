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
package org.stanwood.media.actions.seendb;

import java.io.File;
import java.util.Collection;

import org.stanwood.media.progress.IProgressMonitor;

/**
 * Should be implemented by classes used to implement a seen media file database
 */
public interface ISeenDatabase {

	/**
	 * Used to work out if a file has been seen already
	 * @param mediaDirectory The media directory the file lives in
	 * @param file The file
	 * @return True if seen, otherwise false
	 * @throws SeenDBException Thrown if their is a problem
	 */
	public boolean isSeen(File mediaDirectory,File file) throws SeenDBException;

	/**
	 * Used to mark a file as seen after actions have seen it
	 * @param mediaDirectory The media directory the file lives in
	 * @param file The file
	 * @throws SeenDBException Thrown if their is a problem
	 */
	public void markAsSeen(File mediaDirectory,File file) throws SeenDBException;

	/**
	 * Used to write the database to disc
	 * @param parentMonitor Parent progress monitor
	 * @throws SeenDBException Thrown if their is a problem
	 */
	public void write(IProgressMonitor parentMonitor) throws SeenDBException;

	/**
	 * Used to read the database from disk
	 * @param progress Progress monitor
	 * @throws SeenDBException Thrown if their is a problem
	 */
	public void read(IProgressMonitor progress) throws SeenDBException;

	/**
	 * Used to notify the seen database when a file has been renamed
	 * @param mediaDirectory The media directory the file is located in
	 * @param oldFile The old filename
	 * @param newFile The new filename
	 * @throws SeenDBException Thrown if their is a problem
	 */
	public void renamedFile(File mediaDirectory, File oldFile, File newFile) throws SeenDBException;

	/**
	 * Used to remove files from the Seen database
	 * @param mediaDirectory The media directory of the file to remove
	 * @param file The file to remove
	 * @throws SeenDBException Thrown if their is a problem
	 */
	public void removeFile(File mediaDirectory, File file) throws SeenDBException;

	/**
	 * Uses to get a list of seen entries for a media directory
	 * @param mediaDirectory The media directory
	 * @return The entries
	 */
	public Collection<SeenEntry>getEntries();
}
