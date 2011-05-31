package org.stanwood.media.store.mp4.taglib;


public interface ItmfGeneric {

	/** Get list of all items from file.
	 *  @param hFile handle of file to operate on.
	 *  @return On succes, list of items, which must be free'd. On failure, NULL.
	 */
	MP4ItmfItemList MP4ItmfGetItems( int hFile );
}
