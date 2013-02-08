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
package org.stanwood.media.cli.importer;

import java.io.File;

import org.stanwood.media.MediaDirectory;

public class RenamedEntry {

	private File oldName;
	private File newName;
	private MediaDirectory dir;

	public RenamedEntry(File oldName, File newName,MediaDirectory dir) {
		super();
		this.oldName = oldName;
		this.newName = newName;
		this.dir = dir;
	}

	public File getOldName() {
		return oldName;
	}

	public void setOldName(File oldName) {
		this.oldName = oldName;
	}
	public File getNewName() {
		return newName;
	}
	public void setNewName(File newName) {
		this.newName = newName;
	}
	public MediaDirectory getMediaDirectory() {
		return dir;
	}

}
