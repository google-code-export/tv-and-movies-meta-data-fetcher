package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.util.List;

import org.stanwood.media.store.mp4.AtomNameLookup;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4Exception;

/**
 * Base class for atoms
 */
public abstract class AbstractCLIMP4v2Atom implements IAtom {

	private final static AtomNameLookup nameLookup = new AtomNameLookup();

	private String displayName;
	private String name;


	/**
	 * The constructor
	 * @param name The name of the atom
	 */
	public AbstractCLIMP4v2Atom(String name) {
		setDisplayName(nameLookup.getDisplayName(name));
		setName(name);
	}

	/** {@inheritDoc} */
	@Override
	public String getDisplayName() {
		return displayName;
	}

	/** {@inheritDoc} */
	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public void setName(String name) {
		this.name = name;
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
