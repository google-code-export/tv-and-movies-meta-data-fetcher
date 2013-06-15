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
package org.stanwood.media.store.mp4.jaudiotagger;

import java.text.MessageFormat;

import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TagReverseDnsField;
import org.jaudiotagger.tag.mp4.field.Mp4TagTextField;
import org.stanwood.media.store.mp4.MP4AtomKey;
import org.stanwood.media.store.mp4.atomicparsley.Messages;


/**
 * Used to store mp4 atom information.
 */
public class JATStringAtom extends AbstractJATAtom {

	private String value;

	/**
	 * Used to create a instance of the atom and set the name and value
	 * @param key The key of the atom
	 * @param value The value of the atom
	 */
	public JATStringAtom(MP4AtomKey key, String value) {
		super(key);
		this.value = value;
	}

	/**
	 * Used to get the value of the atom
	 *
	 * @return The value of the atom
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Used to set the value of the atom
	 *
	 * @param value The value of the atom
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JATStringAtom other = (JATStringAtom) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}: [{1}={2}]",getDisplayName(),getKey().toString(),value); //$NON-NLS-1$
	}

	@Override
	public void updateField(Mp4Tag tag) throws FieldDataInvalidException {
		if (getKey() == MP4AtomKey.NAME) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.RELEASE_DATE) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.PURCHASED_DATE) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.TV_SHOW_NAME) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.TV_EPISODE_ID) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.DESCRIPTION_STORE) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.DESCRIPTION) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.DESCRIPTION_LONG) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.GENRE_USER_DEFINED) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.CATEGORY) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.ENCODING_TOOL) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.ARTIST) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.ALBUM) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.ALBUM_ARTIST) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.SORT_ALBUM) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.SORT_ALBUM_ARTIST) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.SORT_ARTIST) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.SORT_COMPOSER) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.SORT_NAME) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.SORT_SHOW) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.COMPOSER) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.COPYRIGHT) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey() == MP4AtomKey.FLAVOUR) {
			tag.setField(new Mp4TagTextField(getName(),getValue()));
		}
		else if (getKey().getDnsName() != null && getKey().getDnsDomain()!=null) {
			tag.setField(new Mp4TagReverseDnsField(getKey().getId()+":"+getKey().getDnsDomain()+":"+getKey().getDnsName(),getKey().getDnsDomain(),getKey().getDnsName(),value.trim())); //$NON-NLS-1$
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("APAtomString.UnsupportedAtom"),getKey().getId())); //$NON-NLS-1$
		}
	}

}
