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
package org.stanwood.media.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.collections.LRUMapCache;

/**
 * This class is used to cache HTTP downloads
 */
public class HttpCache {

	private final static Log log = LogFactory.getLog(HttpCache.class);

	// TODO find way of storing more entries, disk based?
	private static LRUMapCache<String, List<String>> cache = new LRUMapCache<String,List<String>>(300);
	private static HttpCache instance;

	private HttpCache() {

	}

	/**
	 * Get element from cache or null if it can't be found
	 * @param key The key of the item to get
	 * @return The item, or null if it can't be found
	 */
	public List<String> get(String key) {
		return cache.get(key);
	}

	/**
	 * Put a item into the cache
	 * @param key The key of the item
	 * @param value The item
	 */
	public void put(String key,List<String> value) {
		if (log.isDebugEnabled()) {
			log.debug("Caching key "+key); //$NON-NLS-1$
		}
		cache.put(key, value);
	}

	/**
	 * Used to get a singleton instance of the cache
	 * @return a singleton instance of the cache
	 */
	public static HttpCache getInstance() {
		if (instance==null) {
			instance = new HttpCache();
		}
		return instance;
	}

}
