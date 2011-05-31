package org.stanwood.media.store.mp4;


/** Used to represent the values of the atom */
public enum StikValue {
	/** The old movie type */
	 MOVIE("0", "Movie"),
	 /** The music type */
    MUSIC("1", "Music"),
    /** Audio book type */
    AUDIO_BOOK("2", "Audiobook"),
    /** Music video type */
    MUSIC_VIDEO("6", "Music Video"),
    /** Movie type */
    SHORT_FILM("9", "Short Film"),
    /** TV show type */
    TV_SHOW("10", "TV Show"),
    /** Booklet type */
    BOOKLET("11", "Booklet"),
    /** Ring tone type */
    RINGTONE("14", "Ringtone");

	private String id;
	private String desc;

	private StikValue(String id, String desc) {
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
	public static StikValue fromId(String value) {
		for (StikValue v : values()) {
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
