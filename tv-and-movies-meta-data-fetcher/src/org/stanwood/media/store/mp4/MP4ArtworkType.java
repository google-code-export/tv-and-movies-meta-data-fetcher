package org.stanwood.media.store.mp4;

/**
 * The types of artwork that can be stored in a MP4 file
 */
public enum MP4ArtworkType {

	/** Unknown type */
	MP4_ART_UNDEFINED(0,".data"),
	/** Windows BMP type */
    MP4_ART_BMP(1,".bmp"),
    /** Gif type */
    MP4_ART_GIF(2,".gif"),
    /** JPEG type */
    MP4_ART_JPEG(3,".jpeg"),
    /** PNG type */
    MP4_ART_PNG(4,".png");

    private int value;
    private String extension;

	private MP4ArtworkType(int value,String extension) {
		this.value = value;
		this.extension = extension;
	}

	/**
	 * Used to get the value of the type as stored in the atom
	 * @return the value of the type as stored in the atom
	 */
	public int getIntValue() {
        return this.value;
    }

	/**
	 * Used to get a type from the value stored in a atom
	 * @param value the value stored in a atom
	 * @return The type
	 */
	public static MP4ArtworkType getForValue(int value) {
        for (MP4ArtworkType o : values()) {
            if (o.getIntValue() == value) {
                return o;
            }
        }
        return MP4_ART_UNDEFINED;
    }

	/**
	 * Get the file extension
	 * @return the file extension
	 */
	public String getExtension() {
		return extension;
	}

}
