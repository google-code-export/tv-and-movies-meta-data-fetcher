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
 * This is used to store information about a link. A link consists of the URL and the 
 * title of the link.
 */
public class Link {

	private String title;
	private String link;	
	
	/**
	 * The constructor used to create a instance of the link class
	 * @param link The URL of the link
	 * @param title The title of the link
	 */
	public Link(String link, String title) {
		super();
		this.link = link;
		this.title = title;
	}
	
	/**
	 * Used to get the title of the link
	 * @return The title of the link
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * USed to set the title of the link
	 * @param title The title of the link
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Used to get the URL of the link
	 * @return the URL of the link
	 */
	public String getLink() {
		return link;
	}
	
	/**
	 * Used to set the URL of the link
	 * @param link The URL of the link
	 */
	public void setLink(String link) {
		this.link = link;
	}
	
	/**
	 * Used to get the string version of the link. This is it's HTML output.
	 * @return The HTML version of the link 
	 */
	@Override
	public String toString() {	
		return "<a href=\""+link+"\">"+title+"</a>";
	}
	
	
	
}
