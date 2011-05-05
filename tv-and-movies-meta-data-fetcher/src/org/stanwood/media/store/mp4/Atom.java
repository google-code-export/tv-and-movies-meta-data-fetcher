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

import com.coremedia.iso.boxes.apple.AbstractAppleMetaDataBox;




/**
 * Used to store mp4 atom information.
 */
public class Atom {


	private String name;
	private String value;

	/**
	 * Used to create a instance of the atom and set the name and value
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public Atom(String name, String value) {
		setName(name);
		setValue(value);
	}

	/**
	 * Used to get the name of the atom
	 *
	 * @return The name of the atom
	 */
	public String getName() {
		return name;
	}

	/**
	 * Used to set the name of the atom
	 *
	 * @param name
	 *            The name of the atom
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Used to get the value of the atom
	 *
	 * @return The value of the atom
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Used to set the value of the atom
	 *
	 * @param value
	 *            The value of the atom
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Used to get a hash code of the atom, based on the name and value
	 * @return The hash code
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Used to see if the atom equals another atom
	 * @param obj The object it should be compared against
	 * @return True of equal otherwise false
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Atom other = (Atom) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return name +"="+value;
	}

	public void updateBoxValue(AbstractAppleMetaDataBox b) {
		b.setValue(getValue());
	}

}
