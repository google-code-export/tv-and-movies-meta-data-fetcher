package org.stanwood.media.store.mp4.atomicparsley;

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
public class APAtomArtwork extends AbstractAPAtom implements IAtom {

	private byte[] data;
	private int size;
	private MP4ArtworkType type;
	private File file;

	/**
	 * The constructor
	 * @param name The name of the atom
	 * @param type The type of the artwork
	 * @param size the size of the artwork
	 * @param data the artwork data
	 */
	public APAtomArtwork(MP4AtomKey name, MP4ArtworkType type, int size, byte[] data) {
		super(name);
		this.type = type;
		this.size = size;
		this.data = data.clone();
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object>args) throws MP4Exception {
		if (getKey() == MP4AtomKey.ARTWORK) {
			file = null;
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
					args.add("--artwork"); //$NON-NLS-1$
					args.add(file);
				}
				finally {

				}
			}
			catch (IOException e) {
				throw new MP4Exception(MessageFormat.format("Unable to update the artwork of file ''{0}''",mp4File));
			}
		}
		else {
			throw new UnsupportedOperationException(MessageFormat.format("Atom type ''{0}'' not supported",getName()));
		}
	}

	@Override
	public void cleanup() throws MP4Exception {
		if (file!=null) {
			try {
				FileHelper.delete(file);
			} catch (IOException e) {
				throw new MP4Exception("Unable to delete temp artwork",e);
			}
		}
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}: [{1}=Artwork of type {2} of size {3}]",getDisplayName(),getName(),getDisplayType(),size);
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
