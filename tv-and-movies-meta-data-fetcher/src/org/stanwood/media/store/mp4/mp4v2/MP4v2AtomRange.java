package org.stanwood.media.store.mp4.mp4v2;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4Tags;

/**
 * Used to store mp4 range atom data
 */
public class MP4v2AtomRange extends AbstractMP4v2Atom implements IAtom {

	private short number;
	private short total;

	/**
	 * The constructor
	 * @param lib The mp4v2 lib used to perform operations on the file
	 * @param name The name of the atom
	 * @param number the number of items in the rage
	 * @param total the maximum number in the range
	 */
	public MP4v2AtomRange(MP4v2Library lib,String name,short number,short total) {
		super(lib,name);
		this.number = number;
		this.total = total;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(MP4Tags tags) {
		// range
		if (getName().equals("disk")) {

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
