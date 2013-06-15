package org.stanwood.media.store.mp4.jaudiotagger;

import java.text.MessageFormat;

import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4DiscNoField;
import org.jaudiotagger.tag.mp4.field.Mp4TrackField;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * Used to store mp4 range atom data
 */
public class JATAtomRange extends AbstractJATAtom implements IAtom {

	private short number;
	private short total;

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param number the number of items in the rage
	 * @param total the maximum number in the range
	 */
	public JATAtomRange(MP4AtomKey name,short number,short total) {
		super(name);
		this.number = number;
		this.total = total;
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}: [{1}={2} of {3}]",getDisplayName(),getName(),number,total); //$NON-NLS-1$
	}

	@Override
	public void updateField(Mp4Tag tag) throws FieldDataInvalidException {
		if (getKey().equals(MP4AtomKey.TRACK_NUMBER)) {
			tag.setField(new Mp4TrackField(number,total));
		}
		else {
			tag.setField(new Mp4DiscNoField(number,total));
		}
	}
}
