package org.stanwood.media.store.mp4.atomicparsley;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * The artwork summary atom. This cannot be written back to the file as it just
 * allows
 */
public class APAtomArtworkSummary extends AbstractAPAtom implements IAtom {

	private int count;

	/**
	 * The artwork summary constructor
	 * @param name Name of the atom
	 * @param count The count of artworks
	 */
	public APAtomArtworkSummary(MP4AtomKey name,int count) {
		super(name);
		this.count = count;
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}: [{1}={2} piece of artwork]",getKey().getDisplayName(),getName(),count);
	}

	/** {@inheritDoc}*/
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		throw new UnsupportedOperationException("The summary artwork atom can not be written to a mp4 file");
	}
}
