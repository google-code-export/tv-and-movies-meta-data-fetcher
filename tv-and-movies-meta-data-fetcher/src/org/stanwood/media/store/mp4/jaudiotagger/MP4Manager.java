package org.stanwood.media.store.mp4.jaudiotagger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TagByteField;
import org.jaudiotagger.tag.mp4.field.Mp4TagCoverField;
import org.jaudiotagger.tag.mp4.field.Mp4TagTextField;
import org.stanwood.media.store.mp4.AtomNameLookup;
import org.stanwood.media.store.mp4.IAtom;
import org.stanwood.media.store.mp4.IMP4Manager;
import org.stanwood.media.store.mp4.MP4Exception;
import org.stanwood.media.store.mp4.StikValue;

/**
 * This class is a wrapper the the atomic parsley application {@link "http://atomicparsley.sourceforge.net/"} It is used
 * to store and retrieve atoms to a MP4 file.
 */
public class MP4Manager implements IMP4Manager {

	private final static AtomNameLookup nameLookup = new AtomNameLookup();

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
			MP4VideoFileReader reader = new MP4VideoFileReader();
			AudioFile mp4 = reader.read(mp4File);
			Tag tag = mp4.getTag();
			Iterator<TagField> it = tag.getFields();
			while (it.hasNext()) {
				TagField field = it.next();
				Atom atom = new Atom("",field.getId(),getAtomTextValue(field));
				atoms.add(atom);
			}
			return atoms;
		} catch (CannotReadException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File.getAbsolutePath(),e);
		} catch (TagException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File.getAbsolutePath(),e);
		} catch (ReadOnlyFileException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File.getAbsolutePath(),e);
		} catch (InvalidAudioFrameException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File.getAbsolutePath(),e);
		} catch (IOException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File.getAbsolutePath(),e);
		}
	}

	private String getAtomTextValue(TagField field) throws UnsupportedEncodingException,MP4Exception {
		if (field instanceof Mp4TagTextField) {
			return ((Mp4TagTextField)field).getContent();
		}
		else if (field instanceof Mp4TagByteField) {
			return ((Mp4TagByteField)field).getContent();
		}
		else if (field instanceof Mp4TagCoverField) {
			Mp4TagCoverField cover = (Mp4TagCoverField)field;
			return "Artwork of type " + cover.getFieldType() +" and size " + cover.getDataSize();
		}
		throw new MP4Exception("Unsupported field type "+field.getClass()+" of field '" + field.getId());
	}

	/** {@inheritDoc} */
	@Override
	public void update(File mp4File, List<IAtom> atoms) throws MP4Exception {
		try {
			MP4VideoFileReader reader = new MP4VideoFileReader();
			AudioFile mp4 = reader.read(mp4File);
			Mp4Tag tag = (Mp4Tag) mp4.getTag();
			for (IAtom atom : atoms) {
				if (atom.getName().equals("covr")) {
					Artwork art = new Artwork();
					art.setFromFile(new File(atom.getValue()));
					tag.setField(art);
				}
				else {
					tag.setField(new Mp4TagTextField(atom.getName(),atom.getValue()));
				}
			}
			mp4.commit();
		}
		catch (CannotReadException e) {
			e.printStackTrace();
			throw new MP4Exception("Unable to read mp4 file: " + mp4File.getAbsolutePath(),e);
		} catch (IOException e) {
			throw new MP4Exception("Unable to update mp4 file: " + mp4File.getAbsolutePath(),e);
		} catch (TagException e) {
			throw new MP4Exception("Unable to update mp4 file: " + mp4File.getAbsolutePath(),e);
		} catch (ReadOnlyFileException e) {
			throw new MP4Exception("Unable to update mp4 file: " + mp4File.getAbsolutePath(),e);
		} catch (InvalidAudioFrameException e) {
			throw new MP4Exception("Unable to update mp4 file: " + mp4File.getAbsolutePath(),e);
		} catch (CannotWriteException e) {
			throw new MP4Exception("Unable to write mp4 file: " + mp4File.getAbsolutePath(),e);
		}
	}

	/** {@inheritDoc } */
	@Override
	public IAtom createAtom(String name, String value) {
		return new Atom(nameLookup.getDisplayName(name),name,value);
	}

	/** {@inheritDoc } */
	@Override
	public IAtom createAtom(StikValue value) {
		return createAtom("stik",value.getId());
	}

	/** {@inheritDoc } */
	@Override
	public IAtom createDiskAtom(byte diskNumber, byte numberOfDisks) {
		return createAtom("disk",diskNumber+"/"+numberOfDisks);
	}


}
