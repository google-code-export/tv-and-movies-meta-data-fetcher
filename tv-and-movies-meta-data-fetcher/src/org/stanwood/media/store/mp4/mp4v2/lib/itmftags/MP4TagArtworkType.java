package org.stanwood.media.store.mp4.mp4v2.lib.itmftags;

import org.stanwood.media.jna.JnaEnum;
import org.stanwood.media.store.mp4.MP4ArtworkType;

/** Enumeration of possible MP4TagArtwork::type values. */
@SuppressWarnings("all")
public enum MP4TagArtworkType implements JnaEnum<MP4TagArtworkType> {

	MP4_ART_UNDEFINED(MP4ArtworkType.MP4_ART_UNDEFINED.getIntValue()),
    MP4_ART_BMP(MP4ArtworkType.MP4_ART_BMP.getIntValue()),
    MP4_ART_GIF(MP4ArtworkType.MP4_ART_GIF.getIntValue()),
    MP4_ART_JPEG(MP4ArtworkType.MP4_ART_JPEG.getIntValue()),
    MP4_ART_PNG(MP4ArtworkType.MP4_ART_PNG.getIntValue());

    private int value;

	private MP4TagArtworkType(int value) {
		this.value = value;
	}

	@Override
	public int getIntValue() {
        return this.value;
    }

    @Override
	public MP4TagArtworkType getForValue(int i) {
        for (MP4TagArtworkType o : this.values()) {
            if (o.getIntValue() == i) {
                return o;
            }
        }
        return MP4_ART_UNDEFINED;
    }

    public static MP4TagArtworkType fromValue(int i) {
        for (MP4TagArtworkType o : values()) {
            if (o.getIntValue() == i) {
                return o;
            }
        }
        return MP4_ART_UNDEFINED;
    }
}
