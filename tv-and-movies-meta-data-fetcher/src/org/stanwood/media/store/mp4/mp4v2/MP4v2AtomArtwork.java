package org.stanwood.media.store.mp4.mp4v2;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4ArtworkType;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4TagArtwork;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4Tags;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;

import com.sun.jna.Memory;

/**
 * Used to store mp4 artwork atom data
 */
public class MP4v2AtomArtwork extends AbstractMP4v2Atom implements IAtom {

	private byte[] data;
	private int size;
	private MP4ArtworkType type;

	/**
	 * The constructor
	 * @param lib The mp4v2 lib used to perform operations on the file
	 * @param name The name of the atom
	 * @param type The type of the artwork
	 * @param size the size of the artwork
	 * @param data the artwork data
	 */
	public MP4v2AtomArtwork(MP4v2Library lib,String name, MP4ArtworkType type, int size, byte[] data) {
		super(lib,name);
		this.type = type;
		this.size = size;
		this.data = data;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(MP4Tags tags) {

		MP4TagArtwork artwork = new MP4TagArtwork();
		artwork.data = new Memory(data.length);
		artwork.data.write(0,data,0,data.length);
		artwork.size = size;
		artwork.type = type.getIntValue();

//		getLib().MP4TagsSetArtwork(tags, artwork.type,artwork);
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return getDisplayName()+": ["+getName() +"=Artwork of type "+getDisplayType()+" of size "+size+"]";
	}

	private String getDisplayType() {
		switch (type) {
			case  MP4_ART_BMP: return "BMP";
			case  MP4_ART_JPEG: return "JPEG";
			case  MP4_ART_PNG: return "PNG";
			case  MP4_ART_GIF: return "GIF";
			default: return "UNDEFINED";
		}
	}
}
