package org.stanwood.media.store.mp4.mp4v2;

import org.stanwood.media.store.mp4.AtomNameLookup;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4Tags;

/**
 * Base class for atoms
 */
public abstract class AbstractMP4v2Atom implements IAtom {

	private final static AtomNameLookup nameLookup = new AtomNameLookup();

	private String displayName;
	private String name;
	private MP4v2Library lib;

	/**
	 * The constructor
	 * @param lib The mp4v2 lib used to perform operations on the file
	 * @param name The name of the atom
	 */
	public AbstractMP4v2Atom(MP4v2Library lib, String name) {
		setDisplayName(nameLookup.getDisplayName(name));
		setName(name);
		this.lib = lib;
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

	protected MP4v2Library getLib() {
		return lib;
	}

	/**
	 * This is called to update the tags structure with the atom value
	 * @param tags The tags structure
	 */
	abstract public void writeAtom(MP4Tags tags);
}
