package org.stanwood.media.store.mp4.mp4v2.lib;

import org.stanwood.media.store.mp4.taglib.jna.LibraryHelper;

import com.sun.jna.Library;
import com.sun.jna.Platform;

/**
 * The native lib MP4v2 mapping
 */
public interface MP4v2Library extends MP4v2File,ItmfTags,Library {

	/** A instance of the mp4v2 lib */
	//TODO sort out windows
	MP4v2Library INSTANCE = (MP4v2Library)LibraryHelper.loadLibrary((Platform.isWindows() ? "tag" : "mp4v2"),MP4v2Library.class);
}
