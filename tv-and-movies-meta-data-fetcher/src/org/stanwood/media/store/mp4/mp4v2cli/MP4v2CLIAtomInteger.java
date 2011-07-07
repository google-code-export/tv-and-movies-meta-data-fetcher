package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.text.MessageFormat;
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
		return MessageFormat.format(Messages.getString("MP4v2CLIAtomInteger.TOSTRING"),getDisplayName(),getName(),value); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		if (getName().equals("tvsn")) { //$NON-NLS-1$
			args.add("-season"); //$NON-NLS-1$
			args.add(String.valueOf(value));
		}
		else if (getName().equals("tves")) { //$NON-NLS-1$
			args.add("-episode"); //$NON-NLS-1$
			args.add(String.valueOf(value));
		}
		else if (getName().equals("stik")) { //$NON-NLS-1$
			String mediaType = getMediaTypeName(value);
			args.add("-type"); //$NON-NLS-1$
			args.add(mediaType);
		}
		else if (getName().equals("rtng")) { //$NON-NLS-1$
			if (extended) {
				args.add("-rating"); //$NON-NLS-1$
				if (value==2) {
					args.add("clean"); //$NON-NLS-1$
				}
				else if (value==4) {
					args.add("explicit"); //$NON-NLS-1$
				}
				else {
					args.add("none"); //$NON-NLS-1$
				}
			}
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("MP4v2CLIAtomInteger.ATOM_TYPE_NOT_SUPPORTED"),getName())); //$NON-NLS-1$
		}
	}

	private String getMediaTypeName(int value) {
		switch (value) {
			case 0: return "oldmovie"; //$NON-NLS-1$
			case 1: return "normal"; //$NON-NLS-1$
			case 2: return "audiobook"; //$NON-NLS-1$
			case 6: return "musicvideo"; //$NON-NLS-1$
			case 9: return "movie"; //$NON-NLS-1$
			case 10: return "tvshow"; //$NON-NLS-1$
			case 11: return "booklet"; //$NON-NLS-1$
			case 14: return "ringtone"; //$NON-NLS-1$
		}
		throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("MP4v2CLIAtomInteger.UNSUPPORTED_MEDIA"),value)); //$NON-NLS-1$
	}

}
