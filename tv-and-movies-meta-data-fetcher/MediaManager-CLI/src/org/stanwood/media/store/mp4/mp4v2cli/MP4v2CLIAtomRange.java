package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * Used to store mp4 range atom data
 */
public class MP4v2CLIAtomRange extends AbstractCLIMP4v2Atom implements IAtom {

	private short number;
	private short total;

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param number the number of items in the rage
	 * @param total the maximum number in the range
	 */
	public MP4v2CLIAtomRange(String name,short number,short total) {
		super(name);
		this.number = number;
		this.total = total;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object>args) {
		if (getName().equals(MP4AtomKey.DISK_NUMBER.getId())) {
			args.add("-disk"); //$NON-NLS-1$
			args.add(String.valueOf(number));
			args.add("-disks"); //$NON-NLS-1$
			args.add(String.valueOf(total));
		}
		else if (getName().equals(MP4AtomKey.TRACK_NUMBER.getId())) {
			args.add("-track"); //$NON-NLS-1$
			args.add(String.valueOf(number));
			args.add("-tracks"); //$NON-NLS-1$
			args.add(String.valueOf(total));
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("MP4v2CLIAtomRange.ATOM_TYPE_NOT_SUPPORTED"),getName())); //$NON-NLS-1$
		}
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format(Messages.getString("MP4v2CLIAtomRange.TOSTRING"),getDisplayName(),getName(),number,total); //$NON-NLS-1$
	}
}
