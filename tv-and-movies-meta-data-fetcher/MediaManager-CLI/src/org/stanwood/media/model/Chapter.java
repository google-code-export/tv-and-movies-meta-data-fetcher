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

import java.io.Serializable;

/**
 * This is used to store DVD Chapter information for films
 */
public class Chapter implements Serializable {

	private int number;
	private String name;

	/**
	 * Used to create a instance of the chapter class.
	 * @param name The chapter name
	 * @param number The chapter number
	 */
	public Chapter(String name, int number) {
		setName(name);
		setNumber(number);
	}

	/**
	 * Used to get the chapter number
	 * @return the chapter number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Used to set the chapter number
	 * @param number The chapter number
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Used to get the chapter name
	 * @return The chapter name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Used to set the chapter name
	 * @param name The chapter name
	 */
	public void setName(String name) {
		this.name = name;
	}

}
