package org.stanwood.media.store.mp4.taglib;

import org.stanwood.media.store.mp4.taglib.jna.LibraryHelper;

import com.sun.jna.Library;
import com.sun.jna.Platform;

public interface MP4v2Library extends MP4v2File,ItmfGeneric,ItmfTags,Library {

	MP4v2Library INSTANCE = (MP4v2Library)LibraryHelper.loadLibrary((Platform.isWindows() ? "tag" : "mp4v2"),MP4v2Library.class);
}
