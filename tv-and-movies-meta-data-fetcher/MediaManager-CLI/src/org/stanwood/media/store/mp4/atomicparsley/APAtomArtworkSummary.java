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
		return MessageFormat.format(Messages.getString("APAtomArtworkSummary.SummaryMsg"),getKey().getDisplayName(),getName(),count); //$NON-NLS-1$
	}

	/** {@inheritDoc}*/
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		throw new UnsupportedOperationException(Messages.getString("APAtomArtworkSummary.ReadOnly")); //$NON-NLS-1$
	}
}
