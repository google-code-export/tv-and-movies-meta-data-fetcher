package org.stanwood.media.store.mp4.atomicparsley;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtomNumber;
import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * Used to store mp4 number atom data
 */
public class APAtomNumber extends AbstractAPAtom implements IAtomNumber {

	private long value;

	/**
	 * The constructor
	 *
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public APAtomNumber(MP4AtomKey name,long value) {
		super(name);
		this.value = value;
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}: [{1}={2}]",getDisplayName(),getName(),value); //$NON-NLS-1$
	}

	/**
	 * Used to get the value of the atom
	 * @return The value of the atom
	 */
	@Override
	public long getValue() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		if (getKey() == MP4AtomKey.TV_SEASON) {
			args.add("--TVSeasonNum"); //$NON-NLS-1$
			args.add(String.valueOf(value));
		}
		else if (getKey() == MP4AtomKey.TV_EPISODE) {
			args.add("--TVEpisodeNum"); //$NON-NLS-1$
			args.add(String.valueOf(value));
		}
		else if (getKey() == MP4AtomKey.MEDIA_TYPE) {
			args.add("--stik"); //$NON-NLS-1$
			args.add("value="+value); //$NON-NLS-1$
		}
		else if (getKey() == MP4AtomKey.RATING) {
			args.add("--advisory"); //$NON-NLS-1$
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
		else if (getKey() == MP4AtomKey.HD) {
			args.add("--hdvideo"); //$NON-NLS-1$
			args.add(String.valueOf(value));
		}
		else if (getKey().getDnsName() != null && getKey().getDnsDomain()!=null) {
			// TODO handle these
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format("Atom type ''{0}'' not supported",getName()));
		}
	}

}
