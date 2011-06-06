package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;

/**
 * Used to store mp4 number atom data
 */
public class MP4v2CLIAtomInteger extends AbstractCLIMP4v2Atom implements IAtom {

	private int value;

	/**
	 * The constructor
	 *
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public MP4v2CLIAtomInteger(String name,int value) {
		super(name);
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
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		if (getName().equals("tvsn")) {
			args.add("-season");
			args.add(String.valueOf(value));
		}
		else if (getName().equals("tves")) {
			args.add("-episode");
			args.add(String.valueOf(value));
		}
		else if (getName().equals("stik")) {
			String mediaType = getMediaTypeName(value);
			args.add("-type");
			args.add(mediaType);
		}
		else if (getName().equals("rtng")) {
			if (extended) {
				args.add("-rating");
				if (value==2) {
					args.add("clean");
				}
				else if (value==4) {
					args.add("explicit");
				}
				else {
					args.add("none");
				}
			}
		}
		else {
			throw new UnsupportedOperationException("Atom type '"+getName()+"' not supported");
		}
	}

	private String getMediaTypeName(int value) {
		switch (value) {
		case 0: return "oldmovie";
		case 1: return "normal";
		case 2: return "audiobook";
		case 6: return "musicvideo";
		case 9: return "movie";
		case 10: return "tvshow";
		case 11: return "booklet";
		case 14: return "ringtone";
		}
		throw new UnsupportedOperationException("Unsupoorted media type of "+value);
	}

}
