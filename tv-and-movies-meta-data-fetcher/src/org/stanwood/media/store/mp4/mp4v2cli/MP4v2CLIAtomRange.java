package org.stanwood.media.store.mp4.mp4v2cli;

import java.util.List;

import org.stanwood.media.store.mp4.IAtom;

/**
 * Used to store mp4 range atom data
 */
public class MP4v2CLIAtomRange extends AbstractCLIMP4v2Atom implements IAtom {

	private short number;
	private short total;

	/**
	 * The constructor
	 * @param lib The mp4v2 lib used to perform operations on the file
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
	public void writeAtom(List<String>args) {
		if (getName().equals("disk")) {
			args.add("-disk");
			args.add(String.valueOf(number));
			args.add("-disks");
			args.add(String.valueOf(total));
		}
		else {
			throw new UnsupportedOperationException("Atom type '"+getName()+"' not supported");
		}
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return getDisplayName()+": ["+getName() +"="+number+" of "+total+"]";
	}
}
