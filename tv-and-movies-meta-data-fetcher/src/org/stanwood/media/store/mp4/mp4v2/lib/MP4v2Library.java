package org.stanwood.media.store.mp4.mp4v2.lib;

import org.stanwood.media.store.mp4.mp4v2.lib.file.MP4v2File;
import org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric.ItmfGeneric;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.ItmfTags;
import org.stanwood.media.store.mp4.taglib.jna.LibraryHelper;

import com.sun.jna.Library;

/**
 * The native lib MP4v2 mapping
 */
public interface MP4v2Library extends MP4v2File,ItmfTags,ItmfGeneric,Library {


	/** A instance of the mp4v2 lib */
	MP4v2Library INSTANCE = LibraryHelper.loadMP4v2Library();
}
