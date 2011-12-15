package org.stanwood.media.store.mp4.atomicparsley;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.MP4AtomKey;

/**
 * Used to store mp4 string atom data
 */
public class APAtomString extends AbstractAPAtom implements IAtomString {

	private String value;

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param value The value of the atom
	 */
	public APAtomString(MP4AtomKey name,String value) {
		super(name);
		this.value = value;
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}: [{1}={2}]",getDisplayName(),getKey().toString(),value); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object> args) {
		if (getKey() == MP4AtomKey.NAME) {
			args.add("--title"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.RELEASE_DATE) {
			args.add("--year"); //$NON-NLS-1$ // TODO turn into a release date
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.TV_SHOW_NAME) {
			args.add("--TVShowName"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.TV_EPISODE_ID) {
			args.add("--TVEpisode"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.DESCRIPTION_SHORT) {
			args.add("--description"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.DESCRIPTION_LONG) {
			if (extended) {
				args.add("--longdesc"); //$NON-NLS-1$
				args.add(value);
			}
		}
		else if (getKey() == MP4AtomKey.GENRE_USER_DEFINED) {
			args.add("--genre"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.CATEGORY) {
			args.add("--category"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.ENCODING_TOOL) {
			args.add("--encodingTool"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.ARTIST) {
			args.add("--artist"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.ALBUM) {
			args.add("--album"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.ALBUM_ARTIST) {
			args.add("--albumArtist"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.SORT_ALBUM) {
			args.add("--sortOrder"); //$NON-NLS-1$
			args.add("album"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.SORT_ALBUM_ARTIST) {
			args.add("--sortOrder"); //$NON-NLS-1$
			args.add("albumartist"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.SORT_ARTIST) {
			args.add("--sortOrder"); //$NON-NLS-1$
			args.add("artist"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.SORT_COMPOSER) {
			args.add("--sortOrder"); //$NON-NLS-1$
			args.add("composer"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.SORT_NAME) {
			args.add("--sortOrder"); //$NON-NLS-1$
			args.add("name"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.SORT_SHOW) {
			args.add("--sortOrder"); //$NON-NLS-1$
			args.add("show"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.COMPOSER) {
			args.add("--composer"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey() == MP4AtomKey.COPYRIGHT) {
			args.add("--copyright"); //$NON-NLS-1$
			args.add(value);
		}
		else if (getKey().getDnsName() != null && getKey().getDnsDomain()!=null) {
			if (extended) {
				args.add("--rDNSatom"); //$NON-NLS-1$
				args.add(value);
				args.add("name="+getKey().getDnsName()); //$NON-NLS-1$
				args.add("domain="+getKey().getDnsDomain()); //$NON-NLS-1$
			}
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format("Atom type ''{0}'' not supported",value));
		}
	}

	@Override
	public String getValue() {
		return value;
	}
}
