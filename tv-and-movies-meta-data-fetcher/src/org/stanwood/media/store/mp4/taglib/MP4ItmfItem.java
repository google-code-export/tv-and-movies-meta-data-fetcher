package org.stanwood.media.store.mp4.taglib;

import com.sun.jna.Structure;

/**
 * Item structure. Models an iTMF metadata item atom contained in an ilst atom.
 */
public class MP4ItmfItem extends Structure {
	/** < 0-based index of item in ilst container. -1 if undefined. */
	int index;
	/** < four-char code identifing atom type. NULL-terminated. */
	String code;
	/** < may be NULL. UTF-8 meaning. NULL-terminated. */
	String mean;
	/** < may be NULL. UTF-8 name. NULL-terminated. */
	String name;
	/** < list of data. size is always >= 1. */
	MP4ItmfDataList.ByValue dataList;
}
