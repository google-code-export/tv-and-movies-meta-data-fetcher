package org.stanwood.media.store.mp4.taglib;

import org.stanwood.media.store.mp4.taglib.jna.JnaEnum;

/** Basic types of value data as enumerated in spec. */
public enum MP4ItmfBasicType implements JnaEnum<MP4ItmfBasicType>{

	/** for use with tags for which no type needs to be indicated */
	MP4_ITMF_BT_IMPLICIT(0),
	/** without any count or null terminator */
	MP4_ITMF_BT_UTF8(1),
	/** also known as UTF-16BE */
	MP4_ITMF_BT_UTF16(2),
	/** deprecated unless it is needed for special Japanese characters */
	MP4_ITMF_BT_SJIS(3),
	 /** the HTML file header specifies which HTML version */
	MP4_ITMF_BT_HTML(6),
	/** the XML header must identify the DTD or schemas */
	MP4_ITMF_BT_XML(7),
	/** also known as GUID; stored as 16 bytes in binary (valid as an ID) */
	MP4_ITMF_BT_UUID(8),
	 /** stored as UTF-8 text (valid as an ID) */
	MP4_ITMF_BT_ISRC(9),
	/** stored as UTF-8 text (valid as an ID) */
	MP4_ITMF_BT_MI3P(10),
	/** (deprecated) a GIF image */
	MP4_ITMF_BT_GIF(12),
	/** a JPEG image */
	MP4_ITMF_BT_JPEG(13),
	/** a PNG image */
	MP4_ITMF_BT_PNG(14),
	/** absolute, in UTF-8 characters */
	MP4_ITMF_BT_URL(15),
	/** in milliseconds, 32-bit integer */
	MP4_ITMF_BT_DURATION(16),
	 /** in UTC, counting seconds since midnight, January 1, 1904; 32 or 64-bits */
	MP4_ITMF_BT_DATETIME(17),
	 /** a list of enumerated values */
	MP4_ITMF_BT_GENRES(18),
	/** a signed big-endian integer with length one of { 1,2,3,4,8 } bytes */
	MP4_ITMF_BT_INTEGER(21),
	/** RIAA parental advisory; { -1=no, 1=yes, 0=unspecified }, 8-bit ingteger */
	MP4_ITMF_BT_RIAA_PA(24),
	/** Universal Product Code, in text UTF-8 format (valid as an ID) */
	MP4_ITMF_BT_UPC(25),
	/** Windows bitmap image */
	MP4_ITMF_BT_BMP(27),

	/** undefined */
	MP4_ITMF_BT_UNDEFINED(255);

	private int value;

	private MP4ItmfBasicType(int value) {
		this.value = value;
	}

	@Override
	public int getIntValue() {
        return this.value;
    }

    @Override
	public MP4ItmfBasicType getForValue(int i) {
        for (MP4ItmfBasicType o : this.values()) {
            if (o.getIntValue() == i) {
                return o;
            }
        }
        return MP4_ITMF_BT_UNDEFINED;
    }

}
