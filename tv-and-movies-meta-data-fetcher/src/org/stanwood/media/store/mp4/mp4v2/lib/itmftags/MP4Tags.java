package org.stanwood.media.store.mp4.mp4v2.lib.itmftags;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.ShortByReference;

@SuppressWarnings("all")
public class MP4Tags extends Structure {

	/**
	 * internal use only<br>
	 * C type : void*
	 */
	public Pointer __handle;
	/// C type : const char*
	public Pointer name;
	/// C type : const char*
	public Pointer artist;
	/// C type : const char*
	public Pointer albumArtist;
	/// C type : const char*
	public Pointer album;
	/// C type : const char*
	public Pointer grouping;
	/// C type : const char*
	public Pointer composer;
	/// C type : const char*
	public Pointer comments;
	/// C type : const char*
	public Pointer genre;
	/// C type : const uint16_t*
	public ShortByReference genreType;
	/// C type : const char*
	public Pointer releaseDate;
	/// C type : const MP4TagTrack*
	public MP4TagTrack.ByReference track;
	/// C type : const MP4TagDisk*
	public MP4TagDisk.ByReference disk;
	/// C type : const uint16_t*
	public ShortByReference tempo;
	/// C type : const uint8_t*
	public Pointer compilation;
	/// C type : const char*
	public Pointer tvShow;
	/// C type : const char*
	public Pointer tvNetwork;
	/// C type : const char*
	public Pointer tvEpisodeID;
	/// C type : const uint32_t*
	public IntByReference tvSeason;
	/// C type : const uint32_t*
	public IntByReference tvEpisode;
	/// C type : const char*
	public Pointer description;
	/// C type : const char*
	public Pointer longDescription;
	/// C type : const char*
	public Pointer lyrics;
	/// C type : const char*
	public Pointer sortName;
	/// C type : const char*
	public Pointer sortArtist;
	/// C type : const char*
	public Pointer sortAlbumArtist;
	/// C type : const char*
	public Pointer sortAlbum;
	/// C type : const char*
	public Pointer sortComposer;
	/// C type : const char*
	public Pointer sortTVShow;
	/// C type : const MP4TagArtwork*
	public MP4TagArtwork.ByReference artwork;
	public int artworkCount;
	/// C type : const char*
	public Pointer copyright;
	/// C type : const char*
	public Pointer encodingTool;
	/// C type : const char*
	public Pointer encodedBy;
	/// C type : const char*
	public Pointer purchaseDate;
	/// C type : const uint8_t*
	public Pointer podcast;
	/**
	 * TODO: Needs testing<br>
	 * C type : const char*
	 */
	public Pointer keywords;
	/// C type : const char*
	public Pointer category;
	/// C type : const uint8_t*
	public Pointer hdVideo;
	/// C type : const uint8_t*
	public Pointer mediaType;
	/// C type : const uint8_t*
	public Pointer contentRating;
	/// C type : const uint8_t*
	public Pointer gapless;
	/// C type : const char*
	public Pointer iTunesAccount;
	/// C type : const uint8_t*
	public Pointer iTunesAccountType;
	/// C type : const uint32_t*
	public IntByReference iTunesCountry;
	/// C type : const uint32_t*
	public IntByReference cnID;
	/// C type : const uint32_t*
	public IntByReference atID;
	/// C type : const uint64_t*
	public LongByReference plID;
	/// C type : const uint32_t*
	public IntByReference geID;
	public MP4Tags() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"__handle", "name", "artist", "albumArtist", "album", "grouping", "composer", "comments", "genre", "genreType", "releaseDate", "track", "disk", "tempo", "compilation", "tvShow", "tvNetwork", "tvEpisodeID", "tvSeason", "tvEpisode", "description", "longDescription", "lyrics", "sortName", "sortArtist", "sortAlbumArtist", "sortAlbum", "sortComposer", "sortTVShow", "artwork", "artworkCount", "copyright", "encodingTool", "encodedBy", "purchaseDate", "podcast", "keywords", "category", "hdVideo", "mediaType", "contentRating", "gapless", "iTunesAccount", "iTunesAccountType", "iTunesCountry", "cnID", "atID", "plID", "geID"});
	}
	public static class ByReference extends MP4Tags implements Structure.ByReference {

	};
	public static class ByValue extends MP4Tags implements Structure.ByValue {

	};
}
