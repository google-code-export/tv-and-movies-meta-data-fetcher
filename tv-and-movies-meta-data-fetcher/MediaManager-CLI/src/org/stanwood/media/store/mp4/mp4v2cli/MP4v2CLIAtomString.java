package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4AtomKey;

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
	public MP4v2CLIAtomString(MP4AtomKey name,String value) {
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
		if (getName().equals(MP4AtomKey.NAME.getId())) {
			args.add("-song"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.RELEASE_DATE.getId())) {
			args.add("-year"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.TV_SHOW_NAME.getId())) {
			args.add("-show"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.TV_EPISODE_ID.getId())) {
			args.add("-episodeid"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.DESCRIPTION_SHORT.getId())) {
			args.add("-description"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.DESCRIPTION_LONG.getId())) {
			args.add("-longdesc"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.GENRE_USER_DEFINED.getId())) {
			args.add("-genre"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.CATEGORY.getId())) {
			if (extended) {
				args.add("-category"); //$NON-NLS-1$
				args.add(value);
			}
		}
		else if (getName().equals(MP4AtomKey.ENCODING_TOOL.getId())) {
			args.add("-tool"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.ARTIST.getId())) {
			args.add("-artist"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getName().equals(MP4AtomKey.ALBUM.getId())) {
			args.add("-album"); //$NON-NLS-1$
			args.add(value);
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("MP4v2CLIAtomString.ATOM_TYPE_NOT_SUPPORTED"),getName())); //$NON-NLS-1$
		}
	}
}
