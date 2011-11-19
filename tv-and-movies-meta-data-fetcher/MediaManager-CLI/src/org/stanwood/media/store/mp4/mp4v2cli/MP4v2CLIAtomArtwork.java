package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4ArtworkType;
import org.stanwood.media.store.mp4.MP4AtomKey;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.util.FileHelper;

/**
 * Used to store mp4 artwork atom data
 */
public class MP4v2CLIAtomArtwork extends AbstractCLIMP4v2Atom implements IAtom {

	private byte[] data;
	private int size;
	private MP4ArtworkType type;
	private MP4v2CLIManager manager;

	/**
	 * The constructor
	 * @param manager The MP4 manager
	 * @param name The name of the atom
	 * @param type The type of the artwork
	 * @param size the size of the artwork
	 * @param data the artwork data
	 */
	public MP4v2CLIAtomArtwork(MP4v2CLIManager manager,MP4AtomKey name, MP4ArtworkType type, int size, byte[] data) {
		super(name);
		this.type = type;
		this.size = size;
		this.data = data.clone();
		this.manager = manager;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object>args) throws MP4Exception {
		if (getName().equals("covr")) { //$NON-NLS-1$
			File file = null;
			try {
				try {
					file = File.createTempFile("artwork", type.getExtension()); //$NON-NLS-1$
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(file);
						fos.write(data);
					}
					finally {
						if (fos!=null) {
							fos.close();
						}
					}
					manager.getCommandOutput(false, false, true, manager.getMP4ArtCommand(), "--add",file,mp4File); //$NON-NLS-1$
				}
				finally {
					if (file!=null) {
						FileHelper.delete(file);
					}
				}
			}
			catch (IOException e) {
				throw new MP4Exception(MessageFormat.format(Messages.getString("MP4v2CLIAtomArtwork.UNABLE_UPDATE_ARTWORK"),mp4File)); //$NON-NLS-1$
			}
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format(Messages.getString("MP4v2CLIAtomArtwork.ATOM_TYPE_NOT_SUPPORTED"),getName())); //$NON-NLS-1$
		}
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format(Messages.getString("MP4v2CLIAtomArtwork.ARTWORK_TOSTRING"),getDisplayName(),getName(),getDisplayType(),size); //$NON-NLS-1$
	}

	private String getDisplayType() {
		switch (type) {
			case  MP4_ART_BMP: return "BMP"; //$NON-NLS-1$
			case  MP4_ART_JPEG: return "JPEG"; //$NON-NLS-1$
			case  MP4_ART_PNG: return "PNG"; //$NON-NLS-1$
			case  MP4_ART_GIF: return "GIF"; //$NON-NLS-1$
			default: return "UNDEFINED"; //$NON-NLS-1$
		}
	}
}
