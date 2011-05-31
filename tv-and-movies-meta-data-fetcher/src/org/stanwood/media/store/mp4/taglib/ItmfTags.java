package org.stanwood.media.store.mp4.taglib;

public interface ItmfTags {

	public MP4Tags MP4TagsAlloc();

	/** Fetch data from mp4 file and populate structure.
	 *
	 *  The tags structure and its hidden data-cache is updated to
	 *  reflect the actual tags values found in the <b>hFile</b>.
	 *
	 *  @param tags structure to fetch (write) into.
	 *  @param hFile handle of file to fetch data from.
	 */
	public void MP4TagsFetch( MP4Tags tags, int hFile );

	public void MP4TagsStore(MP4Tags tags,int hFile);

	public void MP4TagsFree(MP4Tags tags);
}
