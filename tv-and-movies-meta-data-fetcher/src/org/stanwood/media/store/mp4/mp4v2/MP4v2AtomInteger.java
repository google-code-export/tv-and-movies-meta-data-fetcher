package org.stanwood.media.store.mp4.mp4v2;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;
import org.stanwood.media.store.mp4.mp4v2.lib.itmftags.MP4Tags;

import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * Used to store mp4 number atom data
 */
public class MP4v2AtomInteger extends AbstractMP4v2Atom implements IAtom {

	private int value;

	/**
	 * The constructor
	 * @param lib The mp4v2 lib used to perform operations on the file
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public MP4v2AtomInteger(MP4v2Library lib,String name,int value) {
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
		if (getName().equals("tvsn")) {
			getLib().MP4TagsSetTVSeason(tags,new IntByReference(value));
		}
		else if (getName().equals("tves")) {
			getLib().MP4TagsSetTVEpisode(tags, new IntByReference(value));
		}
		else if (getName().equals("stik")) {
			getLib().MP4TagsSetMediaType(tags, new ByteByReference((byte)value));
		}
		else if (getName().equals("rtng")) {
			getLib().MP4TagsSetContentRating(tags, new ByteByReference((byte)value));
		}
		else if (getName().equals("gnre")) {
			getLib().MP4TagsSetGenreType(tags, new ShortByReference((short)value));
		}
	}

}
