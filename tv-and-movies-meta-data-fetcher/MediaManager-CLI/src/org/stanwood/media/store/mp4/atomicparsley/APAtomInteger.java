package org.stanwood.media.store.mp4.atomicparsley;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtomInteger;
import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * Used to store mp4 number atom data
 */
public class APAtomInteger extends AbstractAPAtom implements IAtomInteger {

	private int value;

	/**
	 * The constructor
	 *
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public APAtomInteger(MP4AtomKey name,int value) {
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
	public int getValue() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		if (getName().equals("tvsn")) { //$NON-NLS-1$
			args.add("--TVSeasonNum"); //$NON-NLS-1$
			args.add(String.valueOf(value));
		}
		else if (getName().equals("tves")) { //$NON-NLS-1$
			args.add("--TVEpisodeNum"); //$NON-NLS-1$
			args.add(String.valueOf(value));
		}
		else if (getKey() == MP4AtomKey.MEDIA_TYPE) {
			args.add("--stik"); //$NON-NLS-1$
			args.add("value="+value); //$NON-NLS-1$
		}
		else if (getKey() == MP4AtomKey.RATING) {
			args.add("--advisory"); //$NON-NLS-1$
			if (value==2) {
				args.add("clean"); //$NON-NLS-1$
			}
			else if (value==4) {
				args.add("explicit"); //$NON-NLS-1$
			}
			else {
				args.add("none"); //$NON-NLS-1$
			}
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format("Atom type ''{0}'' not supported",getName()));
		}
	}

}
