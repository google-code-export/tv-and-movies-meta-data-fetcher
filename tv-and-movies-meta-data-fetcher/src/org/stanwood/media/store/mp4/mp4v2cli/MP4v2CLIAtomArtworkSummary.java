package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4ArtworkType;

/**
 * The artwork summary atom. This cannot be written back to the file as it just
 * allows
 */
public class MP4v2CLIAtomArtworkSummary extends AbstractCLIMP4v2Atom implements IAtom {

	private long size;
	private MP4ArtworkType artType;

	/**
	 * The artwork summary constructor
	 * @param name Name of the atom
	 * @param index The index in the file of the artwork
	 * @param size The size of the artwork
	 * @param artType The type of the artwork
	 */
	public MP4v2CLIAtomArtworkSummary(String name,int index, long size, MP4ArtworkType artType) {
		super(name);
		this.size = size;
		this.artType = artType;
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
		switch (artType) {
			case  MP4_ART_BMP: return "BMP";
			case  MP4_ART_JPEG: return "JPEG";
			case  MP4_ART_PNG: return "PNG";
			case  MP4_ART_GIF: return "GIF";
			default: return "UNDEFINED";
		}
	}

	/** {@inheritDoc}*/
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		throw new UnsupportedOperationException("The summary artwork atom can not be wrttien to a mp4 file");
	}
}
