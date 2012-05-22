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
 * Used to hold a certification rating of a episode or film
 */
public class Certification {

	private String certification;
	private String type;

	/**
	 * The constructor
	 */
	public Certification() {

	}

	/**
	 * Used to create a instance of the certification class that holds certification information
	 * @param certification The certification
	 * @param type The type of certification
	 */
	public Certification(String certification, String type) {
		super();
		setCertification(certification);
		setType(type);
	}

	/**
	 * Used to get the type of certification
	 * @return the type of certification
	 */
	public String getType() {
		return type;
	}

	/**
	 * Used to set the type of certification
	 * @param type the type of certification
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Used to get a free text string of the certification
	 * @return The certification
	 */
	public String getCertification() {
		return certification;
	}

	/**
	 * Used to set the certification as a free text string
	 * @param certification The certification
	 */
	public void setCertification(String certification) {
		this.certification = certification;
	}


}
