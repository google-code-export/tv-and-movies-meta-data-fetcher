package org.stanwood.media.store.mp4.isoparser;

import org.stanwood.media.store.mp4.StikValue;

import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.apple.AppleMediaTypeBox;

/**
 * This is a &atom;Stik&atom; used to store media type information
 */
public class AtomStik extends Atom {


	/**
	 * The constructor
	 * @param value The value
	 */
	public AtomStik(String value) {
		super("Media Type","stik", value);
	}

	/**
	 * Used to set the value of the atom
	 * @param value the value of the atom
	 */
	public void setTypedValue(StikValue value) {
		setValue(String.valueOf(value.getId()));
	}

	/** {@inheritDoc} */
	@Override
	public void setValue(String value) {
		super.setValue(value);
	}

	/** {@inheritDoc} */
	@Override
	public String getValue() {
		String value = super.getValue();
		return value;
	}

	/**
	 * Used to get the value of the atom
	 * @return The value of the atom
	 */
	public StikValue getTypedValue() {
		return StikValue.fromId(Byte.parseByte(getValue()));
	}

	/** {@inheritDoc} */
	@Override
	public void updateBoxValue(AbstractBox b) {
		if (b instanceof AppleMediaTypeBox) {
			AppleMediaTypeBox box = (AppleMediaTypeBox)b;
			box.setValue(getValue());
		}
	}
}
