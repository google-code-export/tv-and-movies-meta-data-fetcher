package org.stanwood.media.store.mp4.mp4v2cli;

import java.util.List;

import org.stanwood.media.store.mp4.IAtom;

/**
 * Used to store mp4 string atom data
 */
public class MP4v2CLIAtomString extends AbstractCLIMP4v2Atom implements IAtom {

	private String value;

	/**
	 * The constructor
	 * @param lib The mp4v2 lib used to perform operations on the file
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public MP4v2CLIAtomString(String name,String value) {
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
	public void writeAtom(List<String> args) {
		if (getName().equals("©nam")) {
			args.add("-name");
			args.add(value);
		}
		else if (getName().equals("©day")) {
			args.add("-year");
			args.add(value);
		}
		else if (getName().equals("tvsh")) {
			args.add("-show");
			args.add(value);
		}
		else if (getName().equals("tven")) {
			args.add("-episodeid");
			args.add(value);
		}
		else if (getName().equals("desc")) {
			args.add("-description");
			args.add(value);
		}
		else if (getName().equals("ldes")) {
			args.add("-longdesc");
			args.add(value);
		}
		else if (getName().equals("©gen")) {
			args.add("-genre");
			args.add(value);
		}
		else if (getName().equals("catg")) {
//			args.add("-year");
//			args.add(value);
		}
		else if (getName().equals("©too")) {
			args.add("-tool");
			args.add(value);
		}
		else if (getName().equals("©ART")) {
			args.add("-artist");
			args.add(value);
		}
		else {
			throw new UnsupportedOperationException("Atom type '"+getName()+"' not supported");
		}
	}
}
