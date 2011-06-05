package org.stanwood.media.store.mp4.mp4v2cli;

import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4ArtworkType;

/**
 * Used to store mp4 artwork atom data
 */
public class MP4v2CLIAtomArtwork extends AbstractCLIMP4v2Atom implements IAtom {

	private byte[] data;
	private int size;
	private MP4ArtworkType type;

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param type The type of the artwork
	 * @param size the size of the artwork
	 * @param data the artwork data
	 */
	public MP4v2CLIAtomArtwork(String name, MP4ArtworkType type, int size, byte[] data) {
		super(name);
		this.type = type;
		this.size = size;
		this.data = data;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(List<String>args) {
		throw new UnsupportedOperationException("Atom type '"+getName()+"' not supported");
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
