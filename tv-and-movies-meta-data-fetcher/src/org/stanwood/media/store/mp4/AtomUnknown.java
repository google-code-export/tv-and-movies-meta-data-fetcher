package org.stanwood.media.store.mp4;

import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;

/**
 * Used to store info on unsupported atoms
 */
public class AtomUnknown extends Atom {

	private Box box;

	/**
	 * Used to create a instance of the atom and set the name and value
	 * @param name The name of the atom
	 * @param box The box that was read
	 */
	public AtomUnknown(String name, Box box) {
		super(name, null);
		this.box = box;
	}

	/** {@inheritDoc} */
	@Override
	public void updateBoxValue(AbstractBox b) throws MP4Exception {
		throw new MP4Exception("Unsupported operation of box of type: " + this.getName());
	}

	/** {@inheritDoc} */
	@Override
	public Box getBox() {
		return box;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((box == null) ? 0 : box.hashCode());
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
		AtomUnknown other = (AtomUnknown) obj;
		if (box == null) {
			if (other.box != null) {
				return false;
			}
		} else if (!box.equals(other.box)) {
			return false;
		}
		return true;
	}




}
