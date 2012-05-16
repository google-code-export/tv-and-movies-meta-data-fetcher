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
package org.stanwood.media.collections;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A Hash map cache that deletes entries that were access last when a capacity limit is reached
 * @param <K> The map key
 * @param <V> The map value
 */
public class LRUMapCache<K,V> extends LinkedHashMap<K,V> {

	private final int capacity;
	private long accessCount = 0;
	private long hitCount = 0;

	/**
	 * The constructor
	 * @param capacity The maximum capacity (number of entries) of the cache
	 */
	public LRUMapCache(int capacity) {
		super(capacity + 1, 1.1f, true);
		this.capacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return size() > capacity;
	}

	/** {@inheritDoc} */
	@Override
	public V get(Object key) {
		accessCount++;
		if (containsKey(key)) {
			hitCount++;
		}
		V value = super.get(key);
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(Object key) {
		V value = super.get(key);
		return value!=null;
	}

	
}