package org.stanwood.media.store.mp4.mp4v2cli;

import java.util.List;

import org.stanwood.media.store.mp4.AtomNameLookup;
import org.stanwood.media.store.mp4.IAtom;

/**
 * Base class for atoms
 */
public abstract class AbstractCLIMP4v2Atom implements IAtom {

	private final static AtomNameLookup nameLookup = new AtomNameLookup();

	private String displayName;
	private String name;


	/**
	 * The constructor
	 * @param lib The mp4v2 lib used to perform operations on the file
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

	abstract public void writeAtom(List<String> args);
}
