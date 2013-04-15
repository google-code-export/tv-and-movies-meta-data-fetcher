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

/**
 * Used to store seen database configuration.
 */
public class SeenDatabaseConfig {

	private String resourceId;

	/**
	 * Used to get the resource ID to use with the Seen DB
	 * @return The resource ID to use with the Seen DB
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * Used to set the resource ID to use with the Seen DB
	 * @param resourceId the resource ID to use with the Seen DB
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

}
