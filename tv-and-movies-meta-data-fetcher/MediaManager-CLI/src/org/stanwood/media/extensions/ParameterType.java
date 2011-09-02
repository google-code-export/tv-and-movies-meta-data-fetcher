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
package org.stanwood.media.extensions;

/**
 * Used to store information about parameter types
 */
public class ParameterType {

	private String name;
	private Class<?>type;
	private boolean required;

	/**
	 * The constructor
	 * @param name The name of the parameter
	 * @param type The type of the parameter
	 * @param required Used to indercate if the parameter is required
	 */
	public ParameterType(String name, Class<?> type,boolean required) {
		super();
		this.name = name;
		this.type = type;
		this.required = required;
	}

	/**
	 * Used to get the name of the parameter
	 * @return the name of the parameter
	 */
	public String getName() {
		return name;
	}

	/**
	 * Used to set the name of the parameter
	 * @param name the name of the parameter
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Used to get the type of the parameter
	 * @return the type type of the parameter
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Used to get the type of the parameter
	 * @param type the type of the parameter
	 */
	public void setType(Class<?> type) {
		this.type = type;
	}

	/**
	 * Return true if the parameter is required
	 * @return true if the parameter is required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Used to set if a parameter is required
	 * @param required true if the parameter is required
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}


}
