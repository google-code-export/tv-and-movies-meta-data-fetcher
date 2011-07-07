package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;

/**
 * Used to store mp4 string atom data
 */
public class MP4v2CLIAtomString extends AbstractCLIMP4v2Atom implements IAtom {

	private String value;

	/**
	 * The constructor
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
		return MessageFormat.format(Messages.getString("MP4v2CLIAtomString.TOSTRING"),getDisplayName(),getName(),value); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		if (getName().equals("©nam")) { //$NON-NLS-1$
			args.add("-song"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals("©day")) { //$NON-NLS-1$
			args.add("-year"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals("tvsh")) { //$NON-NLS-1$
			args.add("-show"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals("tven")) { //$NON-NLS-1$
			args.add("-episodeid"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals("desc")) { //$NON-NLS-1$
			args.add("-description"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals("ldes")) { //$NON-NLS-1$
			args.add("-longdesc"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals("©gen")) { //$NON-NLS-1$
			args.add("-genre"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals("catg")) { //$NON-NLS-1$
			if (extended) {
				args.add("-category"); //$NON-NLS-1$
				args.add(value);
			}
		}
		else if (getName().equals("©too")) { //$NON-NLS-1$
			args.add("-tool"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals("©ART")) { //$NON-NLS-1$
			args.add("-artist"); //$NON-NLS-1$
			args.add(value);
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("MP4v2CLIAtomString.ATOM_TYPE_NOT_SUPPORTED"),getName())); //$NON-NLS-1$
		}
	}
}
