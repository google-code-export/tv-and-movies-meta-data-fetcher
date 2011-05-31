package org.stanwood.media.store.mp4.taglib;

import com.sun.jna.Structure;

/** Data object representing a single piece of artwork. */
public class MP4TagArtwork extends Structure {
	int               data; /**< raw picture data */
    int          size; /**< data size in bytes */
    MP4TagArtworkType type; /**< data type */
}
