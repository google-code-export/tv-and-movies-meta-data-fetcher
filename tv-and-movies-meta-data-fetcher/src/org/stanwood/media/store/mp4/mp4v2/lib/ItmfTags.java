package org.stanwood.media.store.mp4.mp4v2.lib;

import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * itmf_tags.h mapping
 */
@SuppressWarnings("all")
public interface ItmfTags {

	/**
	 *  Allocate tags convenience structure for reading and settings tags.
	 *
	 *  This function allocates a new structure which represents a snapshot
	 *  of all the tags therein, tracking if the tag is missing,
	 *  or present and with value. It is the caller's responsibility to free
	 *  the structure with MP4TagsFree().
	 *
	 *  @return structure with all tags missing.
	 */
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

	/** Store data to mp4 file from structure.
	 *
	 *  The tags structure is pushed out to the mp4 file,
	 *  adding tags if needed, removing tags if needed, and updating
	 *  the values to modified tags.
	 *
	 *  @param tags structure to store (read) from.
	 *  @param hFile handle of file to store data to.
	 */
	public void MP4TagsStore(MP4Tags tags,int hFile);

	/** Free tags convenience structure.
	 *
	 *  This function frees memory associated with the structure.
	 *
	 *  @param tags structure to destroy.
	 */

	public void MP4TagsFree(MP4Tags tags);

	public void MP4TagsSetName            ( MP4Tags tags, String value );
	public void MP4TagsSetArtist          ( MP4Tags tags, String value );
	public void MP4TagsSetAlbumArtist     ( MP4Tags tags, String value );
	public void MP4TagsSetAlbum           ( MP4Tags tags, String value );
	public void MP4TagsSetGrouping        ( MP4Tags tags, String value );
	public void MP4TagsSetComposer        ( MP4Tags tags, String value );
	public void MP4TagsSetComments        ( MP4Tags tags, String value );
	public void MP4TagsSetGenre           ( MP4Tags tags, String value );
	public void MP4TagsSetGenreType       ( MP4Tags tags, ShortByReference value );
	public void MP4TagsSetReleaseDate     ( MP4Tags tags, String value );
	public void MP4TagsSetTrack           ( MP4Tags tags, MP4TagTrack.ByReference value );
	public void MP4TagsSetDisk            ( MP4Tags tags, MP4TagDisk.ByReference value );
	public void MP4TagsSetTempo           ( MP4Tags tags, ShortByReference value );
	public void MP4TagsSetCompilation     ( MP4Tags tags, ByteByReference value );

	public void MP4TagsSetTVShow          ( MP4Tags tags, String value );
	public void MP4TagsSetTVNetwork       ( MP4Tags tags, String value );
	public void MP4TagsSetTVEpisodeID     ( MP4Tags tags, String value );
	public void MP4TagsSetTVSeason        ( MP4Tags tags, IntByReference value);
	public void MP4TagsSetTVEpisode       ( MP4Tags tags, IntByReference value );

	public void MP4TagsSetDescription     ( MP4Tags tags, String value );
	public void MP4TagsSetLongDescription ( MP4Tags tags, String value );
	public void MP4TagsSetLyrics          ( MP4Tags tags, String value );

	public void MP4TagsSetSortName        ( MP4Tags tags, String value );
	public void MP4TagsSetSortArtist      ( MP4Tags tags, String value );
	public void MP4TagsSetSortAlbumArtist ( MP4Tags tags, String value );
	public void MP4TagsSetSortAlbum       ( MP4Tags tags, String value );
	public void MP4TagsSetSortComposer    ( MP4Tags tags, String value );
	public void MP4TagsSetSortTVShow      ( MP4Tags tags, String value );

	public void MP4TagsAddArtwork         ( MP4Tags tags, MP4TagArtwork.ByReference artwork );
	public void MP4TagsSetArtwork         ( MP4Tags tags, int type, MP4TagArtwork.ByReference artwork );
	public void MP4TagsRemoveArtwork      ( MP4Tags tags, int type );

	public void MP4TagsSetCopyright       ( MP4Tags tags, String value );
	public void MP4TagsSetEncodingTool    ( MP4Tags tags, String value );
	public void MP4TagsSetEncodedBy       ( MP4Tags tags, String value );
	public void MP4TagsSetPurchaseDate    ( MP4Tags tags, String value );

	public void MP4TagsSetPodcast         ( MP4Tags tags, ByteByReference value);
	public void MP4TagsSetKeywords        ( MP4Tags tags, String value );
	public void MP4TagsSetCategory        ( MP4Tags tags, String value );

	public void MP4TagsSetHDVideo         ( MP4Tags tags, ByteByReference value );
	public void MP4TagsSetMediaType       ( MP4Tags tags, ByteByReference value );
	public void MP4TagsSetContentRating   ( MP4Tags tags, ByteByReference value );
	public void MP4TagsSetGapless         ( MP4Tags tags, ByteByReference value );

	public void MP4TagsSetITunesAccount     ( MP4Tags tags, String value );
	public void MP4TagsSetITunesAccountType ( MP4Tags tags, ByteByReference value );
	public void MP4TagsSetITunesCountry     ( MP4Tags tags, IntByReference value );
	public void MP4TagsSetCNID              ( MP4Tags tags, IntByReference value );
	public void MP4TagsSetATID              ( MP4Tags tags, IntByReference value );
	public void MP4TagsSetPLID              ( MP4Tags tags, LongByReference value );
	public void MP4TagsSetGEID              ( MP4Tags tags, IntByReference value );
}
