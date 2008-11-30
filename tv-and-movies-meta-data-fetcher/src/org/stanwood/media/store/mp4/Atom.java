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
package org.stanwood.media.store.mp4;

/**
 * Used to store mp4 atom information.
 */
public class Atom {

	private String name;
	private String value;
	
	
	
	/**
	 * Used to get the name of the atom
	 * @return The name of the atom
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Used to set the name of the atom
	 * @param name The name of the atom
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/** 
	 * Used to get the value of the atom
	 * @return The value of the atom
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Used to set the value of the atom
	 * @param value The value of the atom
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
