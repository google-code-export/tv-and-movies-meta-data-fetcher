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
package org.stanwood.media.store;

/**
 * Thrown is their is a problem with a store
 */
public class StoreException extends Exception {

	private static final long serialVersionUID = 2339844228367668776L;

	public StoreException() {
		super();
	
	}

	public StoreException(String message, Throwable cause) {
		super(message, cause);
	
	}

	public StoreException(String message) {
		super(message);
	
	}

	public StoreException(Throwable cause) {
		super(cause);
	
	}

	
}
