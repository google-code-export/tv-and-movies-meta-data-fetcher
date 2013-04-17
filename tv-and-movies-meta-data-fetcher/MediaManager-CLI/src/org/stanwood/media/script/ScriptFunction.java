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
package org.stanwood.media.script;

/**
 * This enum contains information about script functions
 */
public enum ScriptFunction {

	/** The script function executed before importing media */
	PRE_MEDIA_IMPORT("onEventPreMediaImport"), //$NON-NLS-1$
	/** The script function executed after importing media */
	POST_MEDIA_IMPORT("onEventPostMediaImport"); //$NON-NLS-1$

	private String name;

	private ScriptFunction(String name) {
		this.name = name;
	}

	/**
	 * Used to get the function name
	 * @return The function name
	 */
	public String getName() {
		return this.name;
	}
}
