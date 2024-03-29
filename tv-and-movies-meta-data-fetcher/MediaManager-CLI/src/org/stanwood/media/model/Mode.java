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
package org.stanwood.media.model;

/**
 * This enumeration contains the rename modes
 */
public enum Mode {
	/** When using FILM mode files are treated as films */
	FILM(Messages.getString("Mode.FILMS")), //$NON-NLS-1$
	/** When using TV_SHOW mode files are treated as tv show episodes */
	TV_SHOW(Messages.getString("Mode.TV_SHOWS")); //$NON-NLS-1$

	private String displayName;

	private Mode(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Used to get the display name of the mode
	 * @return The display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Get a list of modes
	 * @return The list of modes
	 */
	public static String modeList() {
		StringBuilder validModes = new StringBuilder();
		for (Mode m : Mode.values()) {
			if (validModes.length()>0) {
				validModes.append(", "); //$NON-NLS-1$
			}
			validModes.append("'"+m+"'");  //$NON-NLS-1$//$NON-NLS-2$

		}
		return validModes.toString();
	}
}