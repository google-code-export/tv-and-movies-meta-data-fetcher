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
package org.stanwood.media.store;

import org.stanwood.media.util.Version;

/**
 * Used to represent store version
 */
public class StoreVersion implements Comparable<StoreVersion> {

	private Version version;
	private int revision;

	/**
	 * The constructor
	 * @param version The version
	 * @param revision The revision
	 */
	public StoreVersion(Version version, int revision) {
		this.version = version;
		this.revision = revision;
	}

	/**
	 * Used to get the version
	 * @return The version
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Used to set the version
	 * @param version The version
	 */
	public void setVersion(Version version) {
		this.version = version;
	}

	/**
	 * Used to get the revision
	 * @return The revision
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Used to set the revision
	 * @param revision The revision
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return version.toString()+" "+revision; //$NON-NLS-1$
	}

	/**
	 * Used to compare this store version to anther
	 * @param storeVersion The store version to compare to
	 * @return a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(StoreVersion storeVersion) {
		if (this.getVersion().compareTo(storeVersion.getVersion())==0) {
			return Integer.valueOf(this.getRevision()).compareTo(storeVersion.getRevision());
		}
		return this.getVersion().compareTo(storeVersion.getVersion());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + revision;
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StoreVersion other = (StoreVersion) obj;
		if (revision != other.revision) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}


}
