package org.stanwood.media.store.mp4.taglib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public class TagLib {

	// This is the standard, stable way of mapping, which supports extensive
    // customization and mapping of Java to native types.
    public interface TagLibrary extends Library {
    	TagLibrary INSTANCE = (TagLibrary)
            Native.loadLibrary((Platform.isWindows() ? "tag" : "mp4v2"),
            		TagLibrary.class);

    	int MP4Create(String fileName,int verbosity,int flags);
//        void printf(String format, Object... args);
    }

    public static void main(String[] args) {
    	String file = "/home/johsta01/nobackup/home-workspace/tv-and-movies-meta-data-fetcher/tv-and-movies-meta-data-fetcher/tests/org/stanwood/media/testdata/a_video.mp4";
    	TagLibrary blah = TagLibrary.INSTANCE;
    	int fileHandle = blah.MP4Create(file,0,0);
//        CLibrary.INSTANCE.printf("Hello, World\n");
//        for (int i=0;i < args.length;i++) {
//            CLibrary.INSTANCE.printf("Argument %d: %s\n", i, args[i]);
//        }
    }
}
