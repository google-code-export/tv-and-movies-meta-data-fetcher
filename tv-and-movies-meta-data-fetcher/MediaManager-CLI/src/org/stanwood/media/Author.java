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
package org.stanwood.media;

/**
 * Used to store author information
 */
public class Author {

	private String name;
	private String email;
	private String description;

	/**
	 * The constructor
	 * @param name The name of the author
	 * @param email The email of the author
	 * @param description The description of the author's role on the project
	 */
	public Author(String name, String email, String description) {
		super();
		this.name = name;
		this.email = email;
		this.description = description;
	}

	/**
	 * Used to get the name of the author
	 * @return The name of the author
	 */
	public String getName() {
		return name;
	}

	/**
	 * Used to set the name of the author
	 * @param name The name of the author
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Used to get the email of the author
	 * @return The email of the author
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Used to set the email of the author
	 * @param email The email of the author
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Used to get the description of the author's role on the project
	 * @return the description of the author's role on the project
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Used to set the description of the author's role on the project
	 * @param description the description of the author's role on the project
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
