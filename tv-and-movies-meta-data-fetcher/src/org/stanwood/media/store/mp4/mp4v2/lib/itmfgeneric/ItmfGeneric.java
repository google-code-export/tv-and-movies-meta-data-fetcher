package org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric;

/**
 * Generic ITMF functions
 */
public interface ItmfGeneric {

	/**
	 * < for use with tags for which no type needs to be indicated<br>
	 * <i>native declaration : line 4</i>
	 */
	public static final int MP4_ITMF_BT_IMPLICIT = 0;
	/**
	 * < without any count or null terminator<br>
	 * <i>native declaration : line 5</i>
	 */
	public static final int MP4_ITMF_BT_UTF8 = 1;
	/**
	 * < also known as UTF-16BE<br>
	 * <i>native declaration : line 6</i>
	 */
	public static final int MP4_ITMF_BT_UTF16 = 2;
	/**
	 * < deprecated unless it is needed for special Japanese characters<br>
	 * <i>native declaration : line 7</i>
	 */
	public static final int MP4_ITMF_BT_SJIS = 3;
	/**
	 * < the HTML file header specifies which HTML version<br>
	 * <i>native declaration : line 8</i>
	 */
	public static final int MP4_ITMF_BT_HTML = 6;
	/**
	 * < the XML header must identify the DTD or schemas<br>
	 * <i>native declaration : line 9</i>
	 */
	public static final int MP4_ITMF_BT_XML = 7;
	/**
	 * < also known as GUID; stored as 16 bytes in binary (valid as an ID)<br>
	 * <i>native declaration : line 10</i>
	 */
	public static final int MP4_ITMF_BT_UUID = 8;
	/**
	 * < stored as UTF-8 text (valid as an ID)<br>
	 * <i>native declaration : line 11</i>
	 */
	public static final int MP4_ITMF_BT_ISRC = 9;
	/**
	 * < stored as UTF-8 text (valid as an ID)<br>
	 * <i>native declaration : line 12</i>
	 */
	public static final int MP4_ITMF_BT_MI3P = 10;
	/**
	 * < (deprecated) a GIF image<br>
	 * <i>native declaration : line 13</i>
	 */
	public static final int MP4_ITMF_BT_GIF = 12;
	/**
	 * < a JPEG image<br>
	 * <i>native declaration : line 14</i>
	 */
	public static final int MP4_ITMF_BT_JPEG = 13;
	/**
	 * < a PNG image<br>
	 * <i>native declaration : line 15</i>
	 */
	public static final int MP4_ITMF_BT_PNG = 14;
	/**
	 * < absolute, in UTF-8 characters<br>
	 * <i>native declaration : line 16</i>
	 */
	public static final int MP4_ITMF_BT_URL = 15;
	/**
	 * < in milliseconds, 32-bit integer<br>
	 * <i>native declaration : line 17</i>
	 */
	public static final int MP4_ITMF_BT_DURATION = 16;
	/**
	 * < in UTC, counting seconds since midnight, January 1, 1904; 32 or 64-bits<br>
	 * <i>native declaration : line 18</i>
	 */
	public static final int MP4_ITMF_BT_DATETIME = 17;
	/**
	 * < a list of enumerated values<br>
	 * <i>native declaration : line 19</i>
	 */
	public static final int MP4_ITMF_BT_GENRES = 18;
	/**
	 * < a signed big-endian integer with length one of { 1,2,3,4,8 } bytes<br>
	 * <i>native declaration : line 20</i>
	 */
	public static final int MP4_ITMF_BT_INTEGER = 21;
	/**
	 * < RIAA parental advisory; { -1=no, 1=yes, 0=unspecified }, 8-bit ingteger<br>
	 * <i>native declaration : line 21</i>
	 */
	public static final int MP4_ITMF_BT_RIAA_PA = 24;
	/**
	 * < Universal Product Code, in text UTF-8 format (valid as an ID)<br>
	 * <i>native declaration : line 22</i>
	 */
	public static final int MP4_ITMF_BT_UPC = 25;
	/**
	 * < Windows bitmap image<br>
	 * <i>native declaration : line 23</i>
	 */
	public static final int MP4_ITMF_BT_BMP = 27;
	/**
	 * < undefined<br>
	 * <i>native declaration : line 25</i>
	 */
	public static final int MP4_ITMF_BT_UNDEFINED = 255;

	/** Get list of items by code from file.
	 *  @param hFile handle of file to operate on.
	 *  @param code four-char code identifying atom type. NULL-terminated.
	 *  @return On succes, list of items, which must be free'd. On failure, NULL.
	 */
	public MP4ItmfItemList.ByReference MP4ItmfGetItemsByCode( int hFile, String code );

	/** Get list of all items from file.
	 *  @param hFile handle of file to operate on.
	 *  @return On succes, list of items, which must be free'd. On failure, NULL.
	 */
	public MP4ItmfItemList.ByReference MP4ItmfGetItems( int hFile );

	/** Allocate an item on the heap.
	 *  @param code four-char code identifying atom type. NULL-terminated.
	 *  @param numData number of data elements to allocate. Must be >= 1.
	 *  @return newly allocated item.
	 */
	public MP4ItmfItem.ByReference MP4ItmfItemAlloc( String code, int numData );

	/** Free an item (deep free).
	 *  @param item to be free'd.
	 */
	public void MP4ItmfItemFree( MP4ItmfItem.ByReference item );

	/** Free an item list (deep free).
	 *  @param itemList to be free'd.
	 */
	public void MP4ItmfItemListFree( MP4ItmfItemList.ByReference itemList );

}
