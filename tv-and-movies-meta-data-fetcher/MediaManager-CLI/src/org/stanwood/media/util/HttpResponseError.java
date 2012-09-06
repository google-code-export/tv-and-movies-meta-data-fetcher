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
package org.stanwood.media.util;

import java.io.IOException;
import java.net.URL;

/**
 * This is thrown when fetching the contents of a URL causes a non 200 response code
 */
public class HttpResponseError extends IOException {

	private int statusCode;
	private URL url;
	private String data;

	/**
	 * The constructor
	 * @param message The message
	 * @param statusCode The response code
	 * @param url The URL that caused the problem
	 * @param data The response text from the remote server
	 */
	public HttpResponseError(String message,URL url,int statusCode,String data) {
		super(message);
		this.statusCode = statusCode;
		this.url = url;
		this.data = data;
	}

	/**
	 * Used to get the status code
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Used to get the URL
	 * @return the URL
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Used to get the data
	 * @return the data
	 */
	public String getData() {
		return data;
	}




}
