package org.stanwood.media.store.mp4.mp4v2;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4Tags;

/**
 * Used to store mp4 string atom data
 */
public class MP4v2AtomString extends AbstractMP4v2Atom implements IAtom {

	private String value;

	/**
	 * The constructor
	 * @param lib The mp4v2 lib used to perform operations on the file
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public MP4v2AtomString(MP4v2Library lib, String name,String value) {
		super(lib,name);
		this.value = value;
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return getDisplayName()+": ["+getName() +"="+value+"]";
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(MP4Tags tags) {
		if (getName().equals("©nam")) {
			getLib().MP4TagsSetName(tags, value);
		}
		else if (getName().equals("©day")) {
			getLib().MP4TagsSetReleaseDate(tags, value);
		}
		else if (getName().equals("tvsh")) {
			getLib().MP4TagsSetTVShow(tags, value);
		}
		else if (getName().equals("tven")) {
			getLib().MP4TagsSetTVEpisodeID(tags, value);
		}
		else if (getName().equals("desc")) {
			getLib().MP4TagsSetDescription(tags, value);
		}
		else if (getName().equals("ldes")) {
			getLib().MP4TagsSetLongDescription(tags, value);
		}
		else if (getName().equals("©gen")) {
			getLib().MP4TagsSetGenre(tags, value);
		}
		else if (getName().equals("catg")) {
			getLib().MP4TagsSetCategory(tags, value);
		}
		else if (getName().equals("©too")) {
			getLib().MP4TagsSetEncodingTool(tags, value);
		}
		else if (getName().equals("©ART")) {
			getLib().MP4TagsSetArtist(tags, value);
		}
	}
}
