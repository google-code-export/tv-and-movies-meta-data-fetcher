package org.stanwood.media.store.mp4.taglib;

import com.sun.jna.Structure;

/** List of data. */
public class MP4ItmfDataList extends Structure {

	/** flat array. NULL when size is zero. */
	MP4ItmfData elements;
	/** number of elements. */
	int size;
}
