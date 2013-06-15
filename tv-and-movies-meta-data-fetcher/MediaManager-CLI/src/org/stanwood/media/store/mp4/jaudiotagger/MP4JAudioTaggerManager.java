package org.stanwood.media.store.mp4.jaudiotagger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp4.Mp4FileReader;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4DiscNoField;
import org.jaudiotagger.tag.mp4.field.Mp4TagCoverField;
import org.jaudiotagger.tag.mp4.field.Mp4TagReverseDnsField;
import org.jaudiotagger.tag.mp4.field.Mp4TagTextField;
import org.jaudiotagger.tag.mp4.field.Mp4TrackField;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.MP4ArtworkType;
import org.stanwood.media.store.mp4.MP4AtomKey;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.util.FileHelper;

/**
 * This class is a wrapper the the atomic parsley application {@link "http://atomicparsley.sourceforge.net/"} It is used
 * to store and retrieve atoms to a MP4 file.
 */
public class MP4JAudioTaggerManager implements IMP4Manager {


	// http://help.mp3tag.de/main_tags.html

	/**
	 * Used to get a list of atoms in the MP4 file.
	 *
	 * @param mp4File The MP4 file
	 * @return A list of atoms
	 * @throws MP4Exception Thrown if their is a problem reading the MP4 file
	 */
	@Override
	public List<IAtom> listAtoms(File mp4File) throws MP4Exception {
		try {
			List<IAtom> atoms = new ArrayList<IAtom>();
			Mp4FileReader reader = new Mp4FileReader();
			AudioFile mp4 = reader.read(mp4File);
			Tag tag = mp4.getTag();
			Iterator<TagField> it = tag.getFields();
			while (it.hasNext()) {
				TagField field = it.next();
				IAtom atom;
				if (field instanceof Mp4TagCoverField) {
					MP4AtomKey key = MP4AtomKey.fromKey(field.getId());
					Mp4TagCoverField cover = (Mp4TagCoverField)field;

					StandardArtwork artwork = new StandardArtwork();
					artwork.setBinaryData(cover.getData());
					artwork.setImageFromData();

					atom = new JATArtworkAtom(key,artwork);
				}
				else if (field instanceof Mp4TrackField ) {
					MP4AtomKey key = MP4AtomKey.fromKey(field.getId());
					atom = new JATAtomRange(key, ((Mp4TrackField)field).getTrackNo(), ((Mp4TrackField)field).getTrackTotal());
				}
				else if (field instanceof Mp4DiscNoField ) {
					MP4AtomKey key = MP4AtomKey.fromKey(field.getId());
					atom = new JATAtomRange(key, ((Mp4DiscNoField)field).getDiscNo(), ((Mp4DiscNoField)field).getDiscTotal());
				}
				else if (field instanceof Mp4TagTextField){
					MP4AtomKey key = MP4AtomKey.fromKey(field.getId());
					atom = new JATStringAtom(key,getAtomTextValue(field));
				}
				else if (field instanceof Mp4TagReverseDnsField) {
					Mp4TagReverseDnsField f = ((Mp4TagReverseDnsField)field);
					MP4AtomKey key = MP4AtomKey.fromRDNS(f.getDescriptor(),f.getIssuer());
					atom = new JATStringAtom(key,f.getContent());
				}
				else {
					throw new MP4Exception(MessageFormat.format("Unsupported atom type {0}",field.getClass().getName()));
				}
				atoms.add(atom);
			}
			return atoms;
		} catch (CannotReadException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_LIST_ATOMS"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (TagException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_LIST_ATOMS"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (ReadOnlyFileException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_LIST_ATOMS"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (InvalidAudioFrameException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_LIST_ATOMS"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (IOException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_LIST_ATOMS"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		}
	}

	private String getAtomTextValue(TagField field) throws UnsupportedEncodingException,MP4Exception {
		if (field instanceof Mp4TagTextField) {
			return ((Mp4TagTextField)field).getContent();
		}
//		else if (field instanceof Mp4TagByteField) {
//			return ((Mp4TagByteField)field).getContent();
//		}
		throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNSUPPORTED_FIELD"),field.getClass() ,field.getId())); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception {
		try {
			Mp4FileReader reader = new Mp4FileReader();
			AudioFile mp4 = reader.read(mp4File);
			Mp4Tag tag = (Mp4Tag) mp4.getTag();
			for (IAtom a : atoms) {
				AbstractJATAtom  atom = (AbstractJATAtom)a;
				atom.updateField(tag);
			}
			mp4.commit();
		}
		catch (CannotReadException e) {
			e.printStackTrace();
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_READ"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (IOException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_UPDATE"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (TagException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_UPDATE"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (ReadOnlyFileException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_UPDATE"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (InvalidAudioFrameException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_UPDATE"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		} catch (CannotWriteException e) {
			throw new MP4Exception(MessageFormat.format(Messages.getString("MP4Manager.UNABLE_WRITE"),mp4File.getAbsolutePath()),e); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc } */
	@Override
	public IAtom createAtom(MP4AtomKey name, String value) {
		return new JATStringAtom(name,value);
	}

	/** {@inheritDoc } */
	@Override
	public IAtom createAtom(MP4AtomKey name, short number, short total) {
		return new JATAtomRange(name, number, total);
	}

	/** {@inheritDoc } */
	@Override
	public IAtom createAtom(MP4AtomKey name, int value) {
		return new JATAtomNumber(name,value);
	}

	/** {@inheritDoc } */
	@Override
	public IAtom createAtom(MP4AtomKey name, MP4ArtworkType type, int size, byte[] data) {

		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc } */
	@Override
	public void init(File nativeDir) throws MP4Exception {

	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String string, String mp4infoPath) {
		// TODO Auto-generated method stub

	}

	private static File downloadToTempFile(URL url,String extension) throws IOException {
		File file = FileHelper.createTempFile("artwork", extension); //$NON-NLS-1$
		if (!file.delete()) {
			throw new IOException(MessageFormat.format("Unable to delete temp file {0}",file.getAbsolutePath())); //$NON-NLS-1$
		}
		FileHelper.copy(url, file);
		return file;
	}

	/** {@inheritDoc} */
	@Override
	public  File getArtworkFile(URL imageUrl) throws IOException {
		File tmp = downloadToTempFile(imageUrl,".tmp"); //$NON-NLS-1$
		return tmp;
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createAtom(MP4AtomKey name, boolean value) {
		if (value) {
			return createAtom(name,1);
		}
		else {
			return createAtom(name,0);
		}
	}

	/** {@inheritDoc} */
	@Override
	public IAtom createArtworkAtomFromFile(MP4AtomKey key, File artworkFile) throws IOException {
		Artwork artwork = ArtworkFactory.createArtworkFromFile(artworkFile);
		IAtom atom = new JATArtworkAtom(key,artwork);
		return atom;
	}

	/** {@inheritDoc} */
	@Override
	public boolean supportedAtom(IAtom atom) {
		return atom instanceof AbstractJATAtom;
	}

}
