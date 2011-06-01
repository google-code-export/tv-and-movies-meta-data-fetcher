package org.stanwood.media.store.mp4.mp4v2.lib;

import com.sun.jna.Structure;

/** Data structure.
 *  Models an iTMF data atom contained in an iTMF metadata item atom.
 */
public class MP4ItmfData extends Structure {

	/** always zero. */
	public byte  typeSetIdentifier;
	/** iTMF basic type. */
	public MP4ItmfBasicType typeCode;
	/** always zero. */
	public int locale;
	/** may be NULL. */
	public byte value;
	/** value size in bytes. */
	public int valueSize;
}
