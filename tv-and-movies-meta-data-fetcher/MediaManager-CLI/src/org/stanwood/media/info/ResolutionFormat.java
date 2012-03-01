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

public enum ResolutionFormat {
	Format_480i(false),
	Format_480p(false),
	Format_576i(false),
	Format_576p(false),
	Format_720p(true),
	Format_1080i(true),
	Format_1080p(true);

	private boolean highDef;

	private ResolutionFormat(boolean highDef) {
		this.highDef = highDef;
	}

	public boolean isHighDef() {
		return highDef;
	}
}
