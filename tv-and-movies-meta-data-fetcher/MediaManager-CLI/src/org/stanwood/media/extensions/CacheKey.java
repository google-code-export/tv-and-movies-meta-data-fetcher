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
package org.stanwood.media.extensions;

import org.stanwood.media.setup.MediaDirConfig;

/**
 * Used as the key when caching extensions
 */
public class CacheKey {

	private MediaDirConfig config;
	private int number;

	/**
	 * The constructor
	 * @param config The media directory configuration
	 * @param number The index number of the extension
	 */
	public CacheKey(MediaDirConfig config, int number) {
		super();
		this.config = config;
		this.number = number;
	}

	/**
	 * Used to get media directory configuration
	 * @return the media directory configuration
	 */
	public MediaDirConfig getConfig() {
		return config;
	}

	/**
	 * Used to set the media directory configuration
	 * @param config the media directory configuration
	 */
	public void setConfig(MediaDirConfig config) {
		this.config = config;
	}

	/**
	 * Used to get the extension index number
	 * @return the extension index number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Used to set the extension index number
	 * @param number the extension index number
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		result = prime * result + number;
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
		CacheKey other = (CacheKey) obj;
		if (config == null) {
			if (other.config != null) {
				return false;
			}
		} else if (!config.equals(other.config)) {
			return false;
		}
		if (number != other.number) {
			return false;
		}
		return true;
	}


}
