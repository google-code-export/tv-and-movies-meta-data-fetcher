package org.stanwood.media.store.mp4.jaudiotagger;

import java.text.MessageFormat;

import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.stanwood.media.store.mp4.IAtomNumber;
import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * Used to store mp4 number atom data
 */
public class JATAtomNumber extends AbstractJATAtom implements IAtomNumber {

	private long value;

	/**
	 * The constructor
	 *
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public JATAtomNumber(MP4AtomKey name,long value) {
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
	@Override
	public long getValue() {
		return value;
	}

	@Override
	public void updateField(Mp4Tag tag) throws FieldDataInvalidException {
		if (getKey() == MP4AtomKey.TV_SEASON) {
			tag.setField(tag.createField(Mp4FieldKey.TV_SEASON, String.valueOf(getValue())));
		}
		else if (getKey() == MP4AtomKey.TV_EPISODE) {
			tag.setField(tag.createField(Mp4FieldKey.TV_EPISODE, String.valueOf(getValue())));
		}
		else if (getKey() == MP4AtomKey.MEDIA_TYPE) {
			tag.setField(tag.createField(Mp4FieldKey.CONTENT_TYPE, String.valueOf(getValue())));
		}
		else if (getKey() == MP4AtomKey.RATING) {
			tag.setField(tag.createField(Mp4FieldKey.RATING, String.valueOf(getValue())));
		}
		else if (getKey() == MP4AtomKey.HD) {
			tag.setField(tag.createField(Mp4FieldKey.HD, String.valueOf(getValue())));
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("APAtomNumber.UnsportedAtom"),getName())); //$NON-NLS-1$
		}
	}

}
