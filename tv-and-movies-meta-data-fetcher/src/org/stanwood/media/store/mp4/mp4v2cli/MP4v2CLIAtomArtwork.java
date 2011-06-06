package org.stanwood.media.store.mp4.mp4v2cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.MP4ArtworkType;
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
	public MP4v2CLIAtomArtwork(MP4v2CLIManager manager,String name, MP4ArtworkType type, int size, byte[] data) {
		super(name);
		this.type = type;
		this.size = size;
		this.data = data;
		this.manager = manager;
	}

	/** {@inheritDoc} */
	@Override
	public void writeAtom(File mp4File,boolean extended,List<Object>args) throws MP4Exception {
		if (getName().equals("covr")) {
			File file = null;
			try {
				try {
					file = File.createTempFile("artwork", type.getExtension());
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
					manager.getCommandOutput(false, false, true, manager.getMP4ArtCommand(), "--add",file,mp4File);
				}
				finally {
					if (file!=null) {
						FileHelper.delete(file);
					}
				}
			}
			catch (IOException e) {
				throw new MP4Exception("Unable to update the artwork of file " + mp4File);
			}
		}
		else {
			throw new UnsupportedOperationException("Atom type '"+getName()+"' not supported");
		}
	}

	/**
	 * Print out the contents of the atom
	 * @return Textual value of the atom
	 */
	@Override
	public String toString() {
		return getDisplayName()+": ["+getName() +"=Artwork of type "+getDisplayType()+" of size "+size+"]";
	}

	private String getDisplayType() {
		switch (type) {
			case  MP4_ART_BMP: return "BMP";
			case  MP4_ART_JPEG: return "JPEG";
			case  MP4_ART_PNG: return "PNG";
			case  MP4_ART_GIF: return "GIF";
			default: return "UNDEFINED";
		}
	}
}
