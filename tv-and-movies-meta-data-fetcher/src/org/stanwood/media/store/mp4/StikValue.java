package org.stanwood.media.store.mp4;


/** Used to represent the values of the atom */
public enum StikValue {
	/** The old movie type */
	MOVIE((byte)0, "Movie"),
	 /** The music type */
    MUSIC((byte)1, "Music"),
    /** Audio book type */
    AUDIO_BOOK((byte)2, "Audiobook"),
    /** Music video type */
    MUSIC_VIDEO((byte)6, "Music Video"),
    /** Movie type */
    SHORT_FILM((byte)9, "Short Film"),
    /** TV show type */
    TV_SHOW((byte)10, "TV Show"),
    /** Booklet type */
    BOOKLET((byte)11, "Booklet"),
    /** Ring tone type */
    RINGTONE((byte)14, "Ringtone");

	private byte id;
	private String desc;

	private StikValue(byte id, String desc) {
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
	public static StikValue fromId(int value) {
		for (StikValue v : values()) {
			if (v.id==value) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Get the atom value id
	 * @return the atom value id
	 */
	public byte getId() {
		return id;
	}

}
