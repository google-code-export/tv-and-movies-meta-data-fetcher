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

package org.stanwood.media.setup;

import java.util.HashMap;
import java.util.Map;

/**
 * This is used to hold the configuration for each store found in the configuration file.
 */
public class StoreConfig {

	private String id;
	private Map<String,String> params = new HashMap<String,String>();
	
	/**
	 * Used to get the ID of the store. The ID is the full class name of the store and
	 * will be used to create the store.
	 * @return The ID of the store
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Used to set the ID of the store. The ID is the full class name of the store and
	 * will be used to create the store. 
	 * @param id The ID of the store.
	 */
	public void setID(String id) {
		this.id = id;
	}
	
	/**
	 * Used to get the key/value pair parameters of the store.
	 * @return A hash map containing store parameters.
	 */
	public Map<String,String> getParams() {
		return params;
	}
	
	/**
	 * Used to add a parameter of the store to it's configuration.
	 * @param key The key of the parameter
	 * @param value The value of the parameter
	 */
	public void addParam(String key,String value) {		
		params.put(key,value);
	}
	
}
