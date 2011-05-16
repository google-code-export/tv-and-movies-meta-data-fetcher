package org.stanwood.media.store.mp4;

import com.coremedia.iso.boxes.apple.AbstractAppleMetaDataBox;
import com.coremedia.iso.boxes.apple.AppleMediaTypeBox;

/**
 * This is a &atom;Stik&atom; used to store media type information
 */
public class AtomStik extends Atom {

	/** Used to represent the values of the atom */
	public enum Value {
		 /** The old movie type */
		 MOVIE_OLD("0", "Movie (is now 9)"),
		 /** The music type */
	     MUSIC("1", "Music"),
	     /** Audio book type */
	     AUDIO_BOOK("2", "Audiobook"),
	     /** Music video type */
	     MUSIC_VIDEO("6", "Music Video"),
	     /** Movie type */
	     MOVIE("9", "Movie"),
	     /** TV show type */
	     TV_SHOW("10", "TV Show"),
	     /** Booklet type */
	     BOOKLET("11", "Booklet"),
	     /** Ring tone type */
	     RINGTONE("14", "Ringtone");

		private String id;
		private String desc;

		private Value(String id, String desc) {
			this.id = id;
			this.desc = desc;
		}

		/**
		 * Get the description of the atom value
		 * @return the description of the atom value
		 */
		public String getDescription() {
			return desc;
		}

		/**
		 * Get the atom value associated with a ID
		 * @param value The id
		 * @return the atom value
		 */
		public static Value fromId(String value) {
			for (Value v : values()) {
				if (v.id.equals(value)) {
					return v;
				}
			}
			return null;
		}

		/**
		 * Get the atom value id
		 * @return the atom value id
		 */
		public String getId() {
			return id;
		}
	}

	/**
	 * The constructor
	 * @param value The value
	 */
	public AtomStik(String value) {
		super("stik", value);
	}

	/**
	 * Used to set the value of the atom
	 * @param value the value of the atom
	 */
	public void setTypedValue(Value value) {
		setValue(value.id);
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
	public Value getTypedValue() {
		return Value.fromId(getValue());
	}

	/** {@inheritDoc} */
	@Override
	public void updateBoxValue(AbstractAppleMetaDataBox b) {
		if (b instanceof AppleMediaTypeBox) {
			AppleMediaTypeBox box = (AppleMediaTypeBox)b;
			box.setValue(getValue());
		}
	}
}
