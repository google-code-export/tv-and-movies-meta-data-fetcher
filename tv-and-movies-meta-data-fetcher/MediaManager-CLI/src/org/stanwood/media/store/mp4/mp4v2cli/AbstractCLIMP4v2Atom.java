package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4AtomKey;
import org.stanwood.media.store.mp4.MP4Exception;

/**
 * Base class for atoms
 */
public abstract class AbstractCLIMP4v2Atom implements IAtom {

	private MP4AtomKey key;


	/**
	 * The constructor
	 * @param name The key of the atom
	 */
	public AbstractCLIMP4v2Atom(MP4AtomKey key) {
		this.key = key;
	}

	/** {@inheritDoc} */
	@Override
	public String getDisplayName() {
		return key.getDisplayName();
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return key.getId();
	}

	/** {@inheritDoc} */
	@Override
	public MP4AtomKey getKey() {
		return key;
	}

	/**
	 * Used to get the arguments need to write a atom to the mp4 file
	 * @param mp4File The mp4 file
	 * @param extended true if we have the patched mp4tags program support active
	 * @param args The argument list to add to
	 * @throws MP4Exception Thrown if their are any problems
	 */
	abstract public void writeAtom(File mp4File,boolean extended,List<Object> args) throws MP4Exception;
}
