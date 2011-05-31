package org.stanwood.media.store.mp4.taglib;

import org.stanwood.media.store.mp4.taglib.jna.JnaEnum;

/** Enumeration of possible MP4TagArtwork::type values. */
public enum MP4TagArtworkType implements JnaEnum<MP4TagArtworkType> {

	MP4_ART_UNDEFINED(0),
    MP4_ART_BMP(1),
    MP4_ART_GIF(2),
    MP4_ART_JPEG(3),
    MP4_ART_PNG(4);

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
}
