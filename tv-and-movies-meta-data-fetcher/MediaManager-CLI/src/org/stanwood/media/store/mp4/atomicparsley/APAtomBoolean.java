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
package org.stanwood.media.store.mp4.atomicparsley;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * Used to represent a atomic parsley boolean atom
 */
public class APAtomBoolean extends AbstractAPAtom implements IAtom {

	private boolean value;

	/**
	 * The constructor
	 *
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public APAtomBoolean(MP4AtomKey name,boolean value) {
		super(name);
		this.value = value;
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}: [{1}={2}]",getDisplayName(),getName(),value); //$NON-NLS-1$
	}

	/**
	 * Used to get the value of the atom
	 * @return The value of the atom
	 */
	public boolean getValue() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		if (getKey() == MP4AtomKey.COMPILATION) {
			args.add("--compilation"); //$NON-NLS-1$
			args.add(toAPValue(value));
		}
		else if (getKey() == MP4AtomKey.GAPLESS_PLAYBACK) {
			args.add("--gapless"); //$NON-NLS-1$
			args.add(toAPValue(value));
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("APAtomBoolean.UnsupportedAtom"),getName())); //$NON-NLS-1$
		}
	}

	private String toAPValue(boolean value) {
		if (value) {
			return "true"; //$NON-NLS-1$
		}
		else {
			return "false"; //$NON-NLS-1$
		}
	}
}