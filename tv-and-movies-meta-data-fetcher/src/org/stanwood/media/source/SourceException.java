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
package org.stanwood.media.source;

/**
 * This is thrown if their is a problem related to sources.
 */
public class SourceException extends Exception {
	
	private static final long serialVersionUID = 3798863549350822214L;

	public SourceException() {
		super();		
	}

	public SourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public SourceException(String message) {
		super(message);
	}

	public SourceException(Throwable cause) {
		super(cause);
	}

}
