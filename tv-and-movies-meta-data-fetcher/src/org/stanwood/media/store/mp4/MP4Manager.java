package org.stanwood.media.store.mp4;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;

/**
 * This class is a wrapper the the atomic parsley application {@link "http://atomicparsley.sourceforge.net/"} It is used
 * to store and retrieve atoms to a MP4 file.
 */
public class MP4Manager implements IMP4Manager {

	private final static Log log = LogFactory.getLog(MP4Manager.class);
	
	/**
	 * Used to get a list of atoms in the MP4 file.
	 *
	 * @param mp4File The MP4 file
	 * @return A list of atoms
	 * @throws MP4Exception Thrown if their is a problem reading the MP4 file
	 */
	@Override
	public List<Atom> listAttoms(File mp4File) throws MP4Exception {
		try {
			List<Atom> atoms = new ArrayList<Atom>();
			MP4VideoFileReader reader = new MP4VideoFileReader();
			AudioFile mp4 = reader.read(mp4File);
			Tag tag = mp4.getTag();
			Iterator<TagField> it = tag.getFields();
			while (it.hasNext()) {
				TagField field = it.next();							
				Atom atom = new Atom(field.getId(),getAtomTextValue(field));
				atoms.add(atom);
			}
			return atoms;
		} catch (CannotReadException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File,e);
		} catch (TagException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File,e);
		} catch (ReadOnlyFileException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File,e);
		} catch (InvalidAudioFrameException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File,e);
		} catch (IOException e) {
			throw new MP4Exception("Unable to list the atoms in the file: " + mp4File,e);
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


	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a TV Show episode
	 *
	 * @param mp4File The MP4 file
	 * @param episode The episode details
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public void updateEpsiode(File mp4File, Episode episode) throws MP4Exception {
		List<Atom> atoms = new ArrayList<Atom>();
		atoms.add(new Atom("stik", "TV Show"));
		atoms.add(new Atom("tven", String.valueOf(episode.getShowEpisodeNumber())));
		atoms.add(new Atom("tvsh", episode.getSeason().getShow().getName()));
		atoms.add(new Atom("tvsn", String.valueOf(episode.getSeason().getSeasonNumber())));
		atoms.add(new Atom("tves", String.valueOf(episode.getEpisodeNumber())));
		atoms.add(new Atom("©day", episode.getDate().toString()));
		atoms.add(new Atom("©nam", episode.getTitle()));
		atoms.add(new Atom("desc", episode.getSummary()));

		if (episode.getSeason().getShow().getGenres().size() > 0) {
			atoms.add(new Atom("©gen", episode.getSeason().getShow().getGenres().get(0)));
			atoms.add(new Atom("catg", episode.getSeason().getShow().getGenres().get(0)));
		}

		update(mp4File, atoms);
	}

	private void update(File mp4File, List<Atom> atoms) throws MP4Exception {
		try {
			MP4VideoFileReader reader = new MP4VideoFileReader();
			AudioFile mp4 = reader.read(mp4File);
			Mp4Tag tag = (Mp4Tag) mp4.getTag();			
			for (Atom atom : atoms) {
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
			throw new MP4Exception("Unable to read mp4 file: " + mp4File,e);
		} catch (IOException e) {
			throw new MP4Exception("Unable to update mp4 file: " + mp4File,e);
		} catch (TagException e) {
			throw new MP4Exception("Unable to update mp4 file: " + mp4File,e);
		} catch (ReadOnlyFileException e) {
			throw new MP4Exception("Unable to update mp4 file: " + mp4File,e);
		} catch (InvalidAudioFrameException e) {
			throw new MP4Exception("Unable to update mp4 file: " + mp4File,e);
		} catch (CannotWriteException e) {
			throw new MP4Exception("Unable to write mp4 file: " + mp4File,e);
		}
	}


	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a Film. It also removes any artwork before adding the
	 * Film atoms and artwork.
	 *
	 * @param mp4File The MP4 file
	 * @param film The film details
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public void updateFilm(File mp4File, Film film) throws MP4Exception {
		List<Atom> atoms = new ArrayList<Atom>();
		atoms.add(new Atom("stik", "Movie"));
		atoms.add(new Atom("©day", film.getDate().toString()));
		atoms.add(new Atom("©nam", film.getTitle()));
		atoms.add(new Atom("desc", film.getDescription()));
		if (film.getImageURL() != null) {
			File artwork;
			try {
				artwork = downloadToTempFile(film.getImageURL());
				atoms.add(new Atom("covr", artwork.getAbsolutePath()));
			} catch (IOException e) {
				log.error("Unable to download artwork from " + film.getImageURL().toExternalForm()+". Unable to update " + mp4File.getName(),e);
				return;
			}
		}
		if (film.getPreferredGenre() != null) {
			atoms.add(new Atom("©gen", film.getPreferredGenre()));
			atoms.add(new Atom("catg", film.getPreferredGenre()));
		} else {
			if (film.getGenres().size() > 0) {
				atoms.add(new Atom("©gen", film.getGenres().get(0)));
				atoms.add(new Atom("catg", film.getGenres().get(0)));
			}
		}
		update(mp4File, atoms);
	}
	
	private File downloadToTempFile(URL url) throws IOException {
		File file = File.createTempFile("artwork", ".jpg");
		if (!file.delete()) {
			throw new IOException("Unable to delete temp file "+file.getAbsolutePath());
		}
		OutputStream out = null;
		URLConnection conn = null;
		InputStream in = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}
			file.deleteOnExit();
			return file;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
			}
		}
	}
}
