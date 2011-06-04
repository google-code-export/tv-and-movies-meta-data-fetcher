package org.stanwood.media.store.mp4.mp4v2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4TagArtwork;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4Tags;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4Tags.ByReference;

import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * This is a wrapper class for the native library {@link MP4v2Library}. It's used to log the calls to the library.
 */
public class LibWrapper implements MP4v2Library {

	private final static Log log = LogFactory.getLog(LibWrapper.class);
	private MP4v2Library lib;

	/**
	 * The constructor
	 *
	 * @param lib The library to wrap
	 */
	public LibWrapper(MP4v2Library lib) {
		this.lib = lib;
	}

	/** {@inheritDoc} */
	@Override
	public void MP4SetVerbosity(int hFile, int verbosity) {
		if (log.isDebugEnabled()) {
			log.debug("MP4SetVerbosity - called");
		}
		lib.MP4SetVerbosity(hFile, verbosity);
		if (log.isDebugEnabled()) {
			log.debug("MP4SetVerbosity - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean MP4Dump(int hFile, String tpDumpFile, boolean dumpImplicits) {
		if (log.isDebugEnabled()) {
			log.debug("MP4Dump - called");
		}
		boolean result = lib.MP4Dump(hFile, tpDumpFile, dumpImplicits);
		if (log.isDebugEnabled()) {
			log.debug("MP4Dump - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String MP4FileInfo(String fileName, int trackId) {
		if (log.isDebugEnabled()) {
			log.debug("MP4FileInfo - called");
		}
		String result = lib.MP4FileInfo(fileName, trackId);
		if (log.isDebugEnabled()) {
			log.debug("MP4FileInfo - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void MP4Close(int hFile) {
		if (log.isDebugEnabled()) {
			log.debug("MP4Close - called");
		}
		lib.MP4Close(hFile);
		if (log.isDebugEnabled()) {
			log.debug("MP4Close - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public int MP4Create(String fileName, int verbosity, int flags) {
		if (log.isDebugEnabled()) {
			log.debug("MP4Create - called");
		}
		int result = lib.MP4Create(fileName, verbosity, flags);
		if (log.isDebugEnabled()) {
			log.debug("MP4Create - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public int MP4Modify(String fileName, int verbosity, int flags) {
		if (log.isDebugEnabled()) {
			log.debug("MP4Modify - called");
		}
		int result = lib.MP4Modify(fileName, verbosity, flags);
		if (log.isDebugEnabled()) {
			log.debug("MP4Modify - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean MP4Optimize(String fileName, String newFileName, int verbosity) {
		if (log.isDebugEnabled()) {
			log.debug("MP4Optimize - called");
		}
		boolean result = lib.MP4Optimize(fileName, newFileName, verbosity);
		if (log.isDebugEnabled()) {
			log.debug("MP4Optimize - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public int MP4Read(String fileName, int verbosity) {
		if (log.isDebugEnabled()) {
			log.debug("MP4Read - called");
		}
		int result = lib.MP4Read(fileName, verbosity);
		if (log.isDebugEnabled()) {
			log.debug("MP4Read - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public ByReference MP4TagsAlloc() {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsAlloc - called");
		}
		ByReference result = lib.MP4TagsAlloc();
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsAlloc - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsFetch(MP4Tags tags, int hFile) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsFetch - called");
		}
		lib.MP4TagsFetch(tags, hFile);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsFetch - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsStore(MP4Tags tags, int hFile) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsStore - called");
		}
		lib.MP4TagsStore(tags, hFile);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsStore - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsFree(MP4Tags tags) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsFree - called");
		}
		lib.MP4TagsFree(tags);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsFree - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetName(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetName - called");
		}
		lib.MP4TagsSetName(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetName - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetArtist(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetArtist - called");
		}
		lib.MP4TagsSetArtist(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetArtist - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetAlbumArtist(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetAlbumArtist - called");
		}
		lib.MP4TagsSetAlbumArtist(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetAlbumArtist - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetAlbum(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetAlbum - called");
		}
		lib.MP4TagsSetAlbum(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetAlbum - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetGrouping(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGrouping - called");
		}
		lib.MP4TagsSetGrouping(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGrouping - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetComposer(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetComposer - called");
		}
		lib.MP4TagsSetComposer(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetComposer - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetComments(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetComments - called");
		}
		lib.MP4TagsSetComments(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetComments - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetGenre(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGenre - called");
		}
		lib.MP4TagsSetGenre(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGenre - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetGenreType(MP4Tags tags, ShortByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGenreType - called");
		}
		lib.MP4TagsSetGenreType(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGenreType - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetReleaseDate(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetReleaseDate - called");
		}
		lib.MP4TagsSetReleaseDate(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetReleaseDate - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetTrack(MP4Tags tags,
			org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4TagTrack.ByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTrack - called");
		}
		lib.MP4TagsSetTrack(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTrack - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetDisk(MP4Tags tags,
			org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4TagDisk.ByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetDisk - called");
		}
		lib.MP4TagsSetDisk(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetDisk - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetTempo(MP4Tags tags, ShortByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTempo - called");
		}
		lib.MP4TagsSetTempo(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTempo - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetCompilation(MP4Tags tags, ByteByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetCompilation - called");
		}
		lib.MP4TagsSetCompilation(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetCompilation - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetTVShow(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVShow - called");
		}
		lib.MP4TagsSetTVShow(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVShow - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetTVNetwork(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVNetwork - called");
		}
		lib.MP4TagsSetTVNetwork(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVNetwork - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetTVEpisodeID(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVEpisodeID - called");
		}
		lib.MP4TagsSetTVEpisodeID(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVEpisodeID - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetTVSeason(MP4Tags tags, IntByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVSeason - called");
		}
		lib.MP4TagsSetTVSeason(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVSeason - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetTVEpisode(MP4Tags tags, IntByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVEpisode - called");
		}
		lib.MP4TagsSetTVEpisode(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetTVEpisode - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetDescription(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetDescription - called");
		}
		lib.MP4TagsSetDescription(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetDescription - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetLongDescription(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetLongDescription - called");
		}
		lib.MP4TagsSetLongDescription(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetLongDescription - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetLyrics(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetLyrics - called");
		}
		lib.MP4TagsSetLyrics(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetLyrics - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetSortName(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortName - called");
		}
		lib.MP4TagsSetSortName(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortName - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetSortArtist(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortArtist - called");
		}
		lib.MP4TagsSetSortArtist(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortArtist - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetSortAlbumArtist(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortAlbumArtist - called");
		}
		lib.MP4TagsSetSortAlbumArtist(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortAlbumArtist - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetSortAlbum(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortAlbum - called");
		}
		lib.MP4TagsSetSortAlbum(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortAlbum - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetSortComposer(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortComposer - called");
		}
		lib.MP4TagsSetSortComposer(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortComposer - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetSortTVShow(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortTVShow - called");
		}
		lib.MP4TagsSetSortTVShow(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetSortTVShow - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsAddArtwork(MP4Tags tags, MP4TagArtwork artwork) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsAddArtwork - called");
		}
		lib.MP4TagsAddArtwork(tags, artwork);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsAddArtwork - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetArtwork(MP4Tags tags, int artworkIndex, MP4TagArtwork artwork) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetArtwork - called");
		}
		lib.MP4TagsSetArtwork(tags, artworkIndex, artwork);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetArtwork - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsRemoveArtwork(MP4Tags tags, int artworkIndex) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsRemoveArtwork - called");
		}
		lib.MP4TagsRemoveArtwork(tags, artworkIndex);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsRemoveArtwork - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetCopyright(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetCopyright - called");
		}
		lib.MP4TagsSetCopyright(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetCopyright - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetEncodingTool(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetEncodingTool - called");
		}
		lib.MP4TagsSetEncodingTool(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetEncodingTool - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetEncodedBy(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetEncodedBy - called");
		}
		lib.MP4TagsSetEncodedBy(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetEncodedBy - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetPurchaseDate(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetPurchaseDate - called");
		}
		lib.MP4TagsSetPurchaseDate(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetPurchaseDate - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetPodcast(MP4Tags tags, ByteByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetPodcast - called");
		}
		lib.MP4TagsSetPodcast(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetPodcast - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetKeywords(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetKeywords - called");
		}
		lib.MP4TagsSetKeywords(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetKeywords - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetCategory(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetCategory - called");
		}
		lib.MP4TagsSetCategory(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetCategory - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetHDVideo(MP4Tags tags, ByteByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetHDVideo - called");
		}
		lib.MP4TagsSetHDVideo(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetHDVideo - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetMediaType(MP4Tags tags, ByteByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetMediaType - called");
		}
		lib.MP4TagsSetMediaType(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetMediaType - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetContentRating(MP4Tags tags, ByteByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetContentRating - called");
		}
		lib.MP4TagsSetContentRating(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetContentRating - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetGapless(MP4Tags tags, ByteByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGapless - called");
		}
		lib.MP4TagsSetGapless(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGapless - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetITunesAccount(MP4Tags tags, String value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetITunesAccount - called");
		}
		lib.MP4TagsSetITunesAccount(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetITunesAccount - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetITunesAccountType(MP4Tags tags, ByteByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetITunesAccountType - called");
		}
		lib.MP4TagsSetITunesAccountType(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetITunesAccountType - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetITunesCountry(MP4Tags tags, IntByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetITunesCountry - called");
		}
		lib.MP4TagsSetITunesCountry(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetITunesCountry - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetCNID(MP4Tags tags, IntByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetCNID - called");
		}
		lib.MP4TagsSetCNID(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetCNID - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetATID(MP4Tags tags, IntByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetATID - called");
		}
		lib.MP4TagsSetATID(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetATID - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetPLID(MP4Tags tags, LongByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetPLID - called");
		}
		lib.MP4TagsSetPLID(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetPLID - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4TagsSetGEID(MP4Tags tags, IntByReference value) {
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGEID - called");
		}
		lib.MP4TagsSetGEID(tags, value);
		if (log.isDebugEnabled()) {
			log.debug("MP4TagsSetGEID - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItemList.ByReference MP4ItmfGetItemsByCode(
			int hFile, String code) {
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfGetItemsByCode - called");
		}
		org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItemList.ByReference result = lib
				.MP4ItmfGetItemsByCode(hFile, code);
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfGetItemsByCode - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItemList.ByReference MP4ItmfGetItems(int hFile) {
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfGetItems - called");
		}
		org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItemList.ByReference result = lib
				.MP4ItmfGetItems(hFile);
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfGetItems - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItem.ByReference MP4ItmfItemAlloc(String code,
			int numData) {
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfItemAlloc - called");
		}
		org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItem.ByReference result = lib.MP4ItmfItemAlloc(code,
				numData);
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfItemAlloc - retured");
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void MP4ItmfItemFree(org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItem.ByReference item) {
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfItemFree - called");
		}
		lib.MP4ItmfItemFree(item);
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfItemFree - retured");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void MP4ItmfItemListFree(
			org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.MP4ItmfItemList.ByReference itemList) {
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfItemListFree - called");
		}
		lib.MP4ItmfItemListFree(itemList);
		if (log.isDebugEnabled()) {
			log.debug("MP4ItmfItemListFree - retured");
		}
	}

}
