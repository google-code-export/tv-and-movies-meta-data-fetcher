package org.stanwood.media.store.mp4.atomicparsley;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * Used to store mp4 range atom data
 */
public class APAtomRange extends AbstractAPAtom implements IAtom {

	private short number;
	private short total;

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param number the number of items in the rage
	 * @param total the maximum number in the range
	 */
	public APAtomRange(MP4AtomKey name,short number,short total) {
		super(name);
		this.number = number;
		this.total = total;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object>args) {
		if (getKey() == MP4AtomKey.DISK_NUMBER) {
			args.add("--disk"); //$NON-NLS-1$
			args.add(number+"/"+total);			 //$NON-NLS-1$
		}
		else if (getKey() == MP4AtomKey.TRACK_NUMBER) {
			args.add("--tracknum"); //$NON-NLS-1$
			args.add(number+"/"+total);			 //$NON-NLS-1$
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("APAtomRange.AtomNotSupported"),getName())); //$NON-NLS-1$
		}
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}: [{1}={2} of {3}]",getDisplayName(),getName(),number,total); //$NON-NLS-1$
	}
}
