/*
 *  Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.store.mp4;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.mp4.isoparser.ISOParserMP4Manager;
import org.stanwood.media.util.FileHelper;

/**
 * <p>
 * This store is used to store and retrieve film/TV show information from .mp4/.m4v files used
 * by iTunes. This allows iTuness to use the meta data retrieved by this tool and this tool
 * too use the meta data of iTunes.
 * </p>
 */
public class MP4ITunesStore implements IStore {

	private final static Log log = LogFactory.getLog(MP4ITunesStore.class);
	private IMP4Manager mp4Manager;
	private Class<? extends IMP4Manager> manager;


	/**
	 * This is used to store episode information of a TVShow MP4 file into the
	 * file as meta data so that iTunes can read it.
	 * @param episodeFile The mp4 episode file
	 * @param episode The episode details
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public void cacheEpisode(File rootMediaDir,File episodeFile,Episode episode) throws StoreException {
		String name = episodeFile.getName();
		if (name.endsWith(".mp4") || name.endsWith(".m4v")) {
			validate();
			writeEpisode(episodeFile,episode);
		}
	}

	private void writeEpisode(File file, Episode episode) throws StoreException {
		try {
			updateEpsiode(getMP4Manager(),file,episode);
		} catch (MP4Exception e) {
			throw new StoreException(e.getMessage(),e);
		}
	}

	/**
	 * This does nothing as the season information can't be stored by this store
	 * @param episodeFile The mp4 episode file
	 * @param season The season details
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public void cacheSeason(File rootMediaDir,File episodeFile,Season season) throws StoreException {
		validate();
	}

	/**
	 * This does nothing as the show information can't be stored by this store
	 * @param episodeFile The mp4 episode file
	 * @param show The show details
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public void cacheShow(File rootMediaDir,File episodeFile,Show show) throws StoreException {
		validate();
	}

	/**
	 * This will always return null as this is a write only store
	 * @param episodeFile the file which the episode is stored in
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Episode getEpisode(File rootMediaDir,File episodeFile,Season season, int episodeNum) throws StoreException {
		validate();
		return null;
	}

	/**
	 * This will always return null as this is a write only store
	 * @param episodeFile the file which the episode is stored in
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Season getSeason(File rootMediaDir,File episodeFile,Show show, int seasonNum) throws StoreException {
		validate();
		return null;
	}

	/**
	 * This will always return null as this is a write only store
	 * @param episodeFile the file which the episode is stored in
	 * @param showId The show Id of the show too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Show getShow(File rootMediaDir,File episodeFile, String showId) throws StoreException {
		validate();
		return null;
	}

	private void validate() throws StoreException {
	}

	/**
	 * This is used to write a film to the store.
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheFilm(File rootMediaDir,File filmFile, Film film,Integer part) throws StoreException {
		// TODO make use of the part number
		String name = filmFile.getName();
		if (name.endsWith(".mp4") || name.endsWith(".m4v")) {
			validate();
			writeFilm(filmFile,film,part);
		}
	}

	private void writeFilm(File filmFile, Film film, Integer part) throws StoreException {
		try {
			updateFilm(getMP4Manager(),filmFile,film,part);
		} catch (MP4Exception e) {
			throw new StoreException(e.getMessage(),e);
		}
	}

	/**
	 * This will always return null as this is a write only store
	 * @param episodeFile the file which the special episode is stored in
	 * @param season The season the episode belongs too
	 * @param specialNumber The number of the special episode too get
	 * @return Always returns null
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public Episode getSpecial(File rootMediaDir,File episodeFile, Season season, int specialNumber) throws MalformedURLException,
			IOException, StoreException {
		return null;
	}

	/**
	 * This does nothing as the meta data is stored in the actual file
	 * @param oldFile The old file
	 * @param newFile The new file
	 */
	@Override
	public void renamedFile(File rootMediaDir,File oldFile, File newFile) {

	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * @param filmFile The file the film is stored in
	 * @param filmId The id of the film
	 */
	@Override
	public Film getFilm(File rootMediaDir,File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		return null;
	}

	private IMP4Manager getMP4Manager() throws StoreException {
		if (mp4Manager==null) {
			if (manager!=null) {
				try {
					mp4Manager = manager.newInstance();
				} catch (Exception e) {
					throw new StoreException("Unable to create manager: " + manager.getClass(),e);
				}
			}
			else {
				mp4Manager=new ISOParserMP4Manager();
			}
		}
		return mp4Manager;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void setParameter(String key, String value) throws StoreException {
		if (key.equalsIgnoreCase("manager")) {
			try {
				manager = (Class<? extends IMP4Manager>) Class.forName(value);
			} catch (ClassNotFoundException e) {
				throw new StoreException("Unable to find MP4 manager class: " + value,e);
			}
		}
		throw new StoreException("Unsupported parameter '" + key+"'");
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws StoreException {
		throw new StoreException("Unsupported parameter '" + key+"'");
	}

	/** {@inheritDoc} */
	@Override
	public void performedActions(MediaDirectory dir) {
	}

	/** {@inheritDoc} */
	@Override
	public void fileDeleted(MediaDirectory dir, File file) {
	}

	/** {@inheritDoc} */
	@Override
	public Episode getEpisode(MediaDirectory dir, File file)
			throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Film getFilm(MediaDirectory dir, File file) throws StoreException {
		return null;
	}

	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a TV Show episode
	 * @param mp4Manager MP4 Manager
	 * @param mp4File The MP4 file
	 * @param episode The episode details
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public static void updateEpsiode(IMP4Manager mp4Manager,File mp4File, Episode episode) throws MP4Exception {

		DateFormat YEAR_DF = new SimpleDateFormat("yyyy");
		// http://code.google.com/p/mp4v2/wiki/iTunesMetadata
		List<IAtom> atoms = new ArrayList<IAtom>();
		atoms.add(mp4Manager.createAtom(StikValue.TV_SHOW));
		atoms.add(mp4Manager.createAtom("tven", episode.getEpisodeId()));
		atoms.add(mp4Manager.createAtom("tvsh", episode.getSeason().getShow().getName()));
		atoms.add(mp4Manager.createAtom("tvsn", String.valueOf(episode.getSeason().getSeasonNumber())));
		atoms.add(mp4Manager.createAtom("tves", String.valueOf(episode.getEpisodeNumber())));
		if (episode.getDate()!=null) {
			atoms.add(mp4Manager.createAtom("©day", YEAR_DF.format(episode.getDate())));
		}
		atoms.add(mp4Manager.createAtom("©nam", episode.getTitle()));
		atoms.add(mp4Manager.createAtom("desc", episode.getSummary()));
//		atoms.add(new Atom("rtng", )); // None = 0, clean = 2, explicit  = 4

		if (episode.getSeason().getShow().getGenres().size() > 0) {
			atoms.add(mp4Manager.createAtom("©gen", episode.getSeason().getShow().getGenres().get(0)));
			atoms.add(mp4Manager.createAtom("catg", episode.getSeason().getShow().getGenres().get(0)));
		}
		File artwork = null;
		try {
			URL imageUrl = null;
			if (episode.getImageURL()!=null) {
				imageUrl = episode.getImageURL();
			}
			else if (episode.getSeason().getShow().getImageURL()!=null) {
				imageUrl = episode.getSeason().getShow().getImageURL();
			}
			if (imageUrl != null) {
				try {
					artwork = downloadToTempFile(imageUrl);
					atoms.add(mp4Manager.createAtom("covr", artwork.getAbsolutePath()));
				} catch (IOException e) {
					log.error("Unable to download artwork from " +imageUrl+". Unable to update " + mp4File.getName(),e);
					return;
				}
			}

			mp4Manager.update(mp4File, atoms);
		}
		finally {
			if (artwork!=null) {
				try {
					FileHelper.delete(artwork);
				} catch (IOException e) {
					log.error("Unable to delete temp file",e);
				}
			}
		}
	}

	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a Film. It also removes any artwork before adding the
	 * Film atoms and artwork.
	 * @param mp4Manager MP4 Manager
	 * @param mp4File The MP4 file
	 * @param film The film details
	 * @param part The part number of the film, or null if it does not have parts
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public static void updateFilm(IMP4Manager mp4Manager ,File mp4File, Film film,Integer part) throws MP4Exception {
		DateFormat YEAR_DF = new SimpleDateFormat("yyyy");
		List<IAtom> atoms = new ArrayList<IAtom>();
		atoms.add(mp4Manager.createAtom(StikValue.MOVIE));
		if (film.getDate()!=null) {
			atoms.add(mp4Manager.createAtom("©day", YEAR_DF.format(film.getDate())));
		}
		atoms.add(mp4Manager.createAtom("©nam", film.getTitle()));
		atoms.add(mp4Manager.createAtom("desc", film.getDescription()));
//		atoms.add(mp4Manager.createAtom("rtng", )); // None = 0, clean = 2, explicit  = 4
		if (part!=null) {
			byte total =0;
			for (VideoFile vf : film.getFiles()) {
				if (vf.getPart()!=null && vf.getPart()>total) {
					total = (byte)(int)vf.getPart();
				}
			}

			if (part>total) {
				total = (byte)(int)part;
			}
			atoms.add(mp4Manager.createDiskAtom((byte)(int)part,total));
		}

		File artwork = null;
		try {
			if (film.getImageURL() != null) {
				try {
					artwork = downloadToTempFile(film.getImageURL());
					atoms.add(mp4Manager.createAtom("covr", artwork.getAbsolutePath()));
				} catch (IOException e) {
					log.error("Unable to download artwork from " + film.getImageURL().toExternalForm()+". Unable to update " + mp4File.getName(),e);
					return;
				}
			}
			if (film.getPreferredGenre() != null) {
				atoms.add(mp4Manager.createAtom("©gen", film.getPreferredGenre()));
				atoms.add(mp4Manager.createAtom("catg", film.getPreferredGenre()));
			} else {
				if (film.getGenres().size() > 0) {
					atoms.add(mp4Manager.createAtom("©gen", film.getGenres().get(0)));
					atoms.add(mp4Manager.createAtom("catg", film.getGenres().get(0)));
				}
			}
			mp4Manager.update(mp4File, atoms);
		}
		finally {
			if (artwork!=null) {
				try {
					FileHelper.delete(artwork);
				} catch (IOException e) {
					log.error("Unable to delete temp file",e);
				}
			}
		}
	}

	private static File downloadToTempFile(URL url) throws IOException {
		File file = FileHelper.createTempFile("artwork", ".jpg");
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
				log.error("Unable to close file: " + file);
			}
		}
	}
}
