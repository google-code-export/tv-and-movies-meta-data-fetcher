package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4ArtworkType;
import org.stanwood.media.store.mp4.MP4AtomKey;

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
	public MP4v2CLIAtomArtworkSummary(MP4AtomKey name,int index, long size, MP4ArtworkType artType) {
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
		return MessageFormat.format(Messages.getString("MP4v2CLIAtomArtworkSummary.ARTWORK_TOSTRING"),getDisplayName(),getName(),getDisplayType(),size); //$NON-NLS-1$
	}

	private String getDisplayType() {
		switch (artType) {
			case  MP4_ART_BMP: return "BMP"; //$NON-NLS-1$
			case  MP4_ART_JPEG: return "JPEG"; //$NON-NLS-1$
			case  MP4_ART_PNG: return "PNG"; //$NON-NLS-1$
			case  MP4_ART_GIF: return "GIF"; //$NON-NLS-1$
			default: return "UNDEFINED"; //$NON-NLS-1$
		}
	}

	/** {@inheritDoc}*/
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		throw new UnsupportedOperationException(Messages.getString("MP4v2CLIAtomArtworkSummary.UNABLE_WRITE")); //$NON-NLS-1$
	}
}
