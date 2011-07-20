package org.stanwood.media.store.mp4;


/** Used to represent the values of the atom */
public enum StikValue {
	/** The old movie type */
	MOVIE((byte)0, Messages.getString("StikValue.MOVIE")), //$NON-NLS-1$
	 /** The music type */
    MUSIC((byte)1, Messages.getString("StikValue.MUSIC")), //$NON-NLS-1$
    /** Audio book type */
    AUDIO_BOOK((byte)2, Messages.getString("StikValue.AUDIOBOOK")), //$NON-NLS-1$
    /** Music video type */
    MUSIC_VIDEO((byte)6, Messages.getString("StikValue.MUSIC_VIDEO")), //$NON-NLS-1$
    /** Movie type */
    SHORT_FILM((byte)9, Messages.getString("StikValue.SHORT_FILM")), //$NON-NLS-1$
    /** TV show type */
    TV_SHOW((byte)10, Messages.getString("StikValue.TV_SHOW")), //$NON-NLS-1$
    /** Booklet type */
    BOOKLET((byte)11, Messages.getString("StikValue.BOOKLET")), //$NON-NLS-1$
    /** Ring tone type */
    RINGTONE((byte)14, Messages.getString("StikValue.RINGTONE")); //$NON-NLS-1$

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
