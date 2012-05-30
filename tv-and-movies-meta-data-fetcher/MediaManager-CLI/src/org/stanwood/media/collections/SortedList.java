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
package org.stanwood.media.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * This is a sorted array list class
 * @param <T> The element type to insert into the list
 */
public class SortedList<T> extends ArrayList<T> {

	private Comparator<? super T> comparator;

	/**
	 * The constructor
	 * @param comparator The comparator used to sort the list
	 */
	public SortedList(Comparator<? super T> comparator) {
		this.comparator = comparator;
	}

	/**
	 * Adds this element to the list at the proper sorting position. If the
	 * element already exists, don't do anything.
	 * @param e The element to add
	 */
	@Override
	public boolean add(T e) {
		if (size() == 0) {

			return super.add(e);
		} else {

			// find insertion index
			int idx = -Collections.binarySearch(this, e, comparator) - 1;

			if (idx < 0) {
				return true; // already added
			}

			// add at this position
			super.add(idx, e);
			return true;
		}
	}
}