/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
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
 * Used to store information on script files
 */
public class ScriptFile {

	private String language;
	private File location;

	/**
	 * Used to set the language of the script file
	 * @return the language of the script file
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Used to get the language of the script file
	 * @param language the language of the script file
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Used to get the location of the script file
	 * @return the location of the script file
	 */
	public File getLocation() {
		return location;
	}

	/**
	 * Used to set the location of the script file
	 * @param location the location of the script file
	 */
	public void setLocation(File location) {
		this.location = location;
	}
}
