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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.jna.NativeHelper;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.mp4.mp4v2cli.MP4v2CLIManager;
import org.stanwood.media.util.FileHelper;

/**
 * <p>
 * This store is used to store Film/TV show information in .mp4/.m4v files used
 * by iTunes. This allows iTunes to use the meta data and see the files complete with
 * their meta data.
 * </p>
 * <p>
 * In order to function, store uses the command line tools provided by the MP4v2 project
 * {@link "http://code.google.com/p/mp4v2/"}. These must be installed on the PATH, or pointed
 * to by the optional store parameters.
 * </p>
 * <p>
 * The mp4tags application does not support the setting of all fields that this store can handle,
 * a patched version of this file can be downloaded or installed via the installer. A warning will
 * be printed if this was not found.
 * </p>
 * <p>This store has following optional parameters:
 * 	<ul>
 * 		<li>mp4art - The path to the mp4art command</li>
 *      <li>mp4info - The path to the mp4info command</li>
 *      <li>mp4tags - The path to the mp4tags command</li>
 *      <li>mp4file - The path to the mp4file command</li>
 *  </ul>
 * </p>
 */
public class MP4ITunesStore implements IStore {

	private final static Log log = LogFactory.getLog(MP4ITunesStore.class);

	private IMP4Manager mp4Manager;
	private Class<? extends IMP4Manager> manager = MP4v2CLIManager.class;
	private String mp4infoPath;
	private String mp4artPath;
	private String mp4tagsPath;
	private String mp4filePath;

	/** {@inheritDoc} */
	@Override
	public void init(File nativeDir) throws StoreException {
		if (mp4infoPath == null) {
			mp4infoPath = NativeHelper.getNativeApplication(nativeDir,MP4ITunesStoreInfo.PARAM_MP4INFO_KEY.getName());
		}
		if (mp4artPath == null) {
			mp4artPath = NativeHelper.getNativeApplication(nativeDir,MP4ITunesStoreInfo.PARAM_MP4ART_KEY.getName());
		}
		if (mp4tagsPath == null) {
			mp4tagsPath = NativeHelper.getNativeApplication(nativeDir,MP4ITunesStoreInfo.PARAM_MP4TAGS_KEY.getName());
		}
		if (mp4filePath == null) {
			mp4filePath = NativeHelper.getNativeApplication(nativeDir,MP4ITunesStoreInfo.PARAM_MP4FILE_KEY.getName());
		}
		try {
			getMP4Manager().init(nativeDir);
		} catch (MP4Exception e) {
			throw new StoreException(Messages.getString("MP4ITunesStore.UNABLE_SETUP_STORE"),e); //$NON-NLS-1$
		}
	}

	/**
	 * This is used to store episode information of a TVShow MP4 file into the
	 * file as meta data so that iTunes can read it.
	 * @param episodeFile The mp4 episode file
	 * @param episode The episode details
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public void cacheEpisode(File rootMediaDir,File episodeFile,IEpisode episode) throws StoreException {
		String name = episodeFile.getName();
		if (name.endsWith(".mp4") || name.endsWith(".m4v")) {  //$NON-NLS-1$//$NON-NLS-2$
			validate();
			writeEpisode(episodeFile,episode);
		}
	}

	private void writeEpisode(File file, IEpisode episode) throws StoreException {
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
	public void cacheSeason(File rootMediaDir,File episodeFile,ISeason season) throws StoreException {
		validate();
	}

	/**
	 * This does nothing as the show information can't be stored by this store
	 * @param episodeFile The mp4 episode file
	 * @param show The show details
	 * @throws StoreException Thrown if their is a problem storing the meta data
	 */
	@Override
	public void cacheShow(File rootMediaDir,File episodeFile,IShow show) throws StoreException {
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
	public IEpisode getEpisode(File rootMediaDir,File episodeFile,ISeason season, int episodeNum) throws StoreException {
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
	public ISeason getSeason(File rootMediaDir,File episodeFile,IShow show, int seasonNum) throws StoreException {
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
	public IShow getShow(File rootMediaDir,File episodeFile, String showId) throws StoreException {
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
	public void cacheFilm(File rootMediaDir,File filmFile, IFilm film,Integer part) throws StoreException {
		// TODO make use of the part number
		String name = filmFile.getName();
		if (name.endsWith(".mp4") || name.endsWith(".m4v")) {  //$NON-NLS-1$//$NON-NLS-2$
			validate();
			writeFilm(filmFile,film,part);
		}
	}

	private void writeFilm(File filmFile, IFilm film, Integer part) throws StoreException {
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
	public IEpisode getSpecial(File rootMediaDir,File episodeFile, ISeason season, int specialNumber) throws MalformedURLException,
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
	public IFilm getFilm(File rootMediaDir,File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		return null;
	}

	IMP4Manager getMP4Manager() throws StoreException {
		if (mp4Manager==null) {
			try {
				mp4Manager = manager.newInstance();
			} catch (Exception e) {
				throw new StoreException(MessageFormat.format(Messages.getString("MP4ITunesStore.UNABLE_CREATE_MANAGER"),manager.getClass()),e); //$NON-NLS-1$
			}

			mp4Manager.setParameter(MP4ITunesStoreInfo.PARAM_MP4ART_KEY.getName(),mp4artPath);
			mp4Manager.setParameter(MP4ITunesStoreInfo.PARAM_MP4TAGS_KEY.getName(),mp4tagsPath);
			mp4Manager.setParameter(MP4ITunesStoreInfo.PARAM_MP4INFO_KEY.getName(),mp4infoPath);
			mp4Manager.setParameter(MP4ITunesStoreInfo.PARAM_MP4FILE_KEY.getName(),mp4filePath);
		}
		return mp4Manager;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void setParameter(String key, String value) throws StoreException {
		if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MANAGER_KEY.getName())) {
			try {
				manager = (Class<? extends IMP4Manager>) Class.forName(value);
			} catch (ClassNotFoundException e) {
				throw new StoreException(MessageFormat.format(Messages.getString("MP4ITunesStore.UNABLE_FIND_MANAGER") ,value),e); //$NON-NLS-1$
			}
		}
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MP4ART_KEY.getName())){
			mp4artPath = value;
		}
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MP4INFO_KEY.getName())) {
			mp4infoPath = value;
		}
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MP4TAGS_KEY.getName())) {
			mp4tagsPath = value;
		}
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MP4FILE_KEY.getName())) {
			mp4filePath = value;
		}
		throw new StoreException(MessageFormat.format(Messages.getString("MP4ITunesStore.UNSUPPORTED_PARAM"),key)); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws StoreException {
		if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MANAGER_KEY.getName())) {
			return manager.getName();
		}
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MP4ART_KEY.getName())){
			return mp4artPath;
		}
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MP4INFO_KEY.getName())) {
			return mp4infoPath;
		}
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MP4TAGS_KEY.getName())) {
			return mp4tagsPath;
		}
		throw new StoreException(MessageFormat.format(Messages.getString("MP4ITunesStore.UNSUPPORTED_PARAM"),key)); //$NON-NLS-1$
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
	public IEpisode getEpisode(MediaDirectory dir, File file)
			throws StoreException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IFilm getFilm(MediaDirectory dir, File file) throws StoreException {
		return null;
	}

	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a TV Show episode
	 * @param mp4Manager MP4 Manager
	 * @param mp4File The MP4 file
	 * @param episode The episode details
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public static void updateEpsiode(IMP4Manager mp4Manager,File mp4File, IEpisode episode) throws MP4Exception {

		DateFormat YEAR_DF = new SimpleDateFormat("yyyy"); //$NON-NLS-1$
		// http://code.google.com/p/mp4v2/wiki/iTunesMetadata
		List<IAtom> atoms = new ArrayList<IAtom>();
		atoms.add(mp4Manager.createAtom("stik",StikValue.TV_SHOW.getId())); //$NON-NLS-1$
		atoms.add(mp4Manager.createAtom("tven", episode.getEpisodeId())); //$NON-NLS-1$
		atoms.add(mp4Manager.createAtom("tvsh", episode.getSeason().getShow().getName())); //$NON-NLS-1$
		atoms.add(mp4Manager.createAtom("tvsn", episode.getSeason().getSeasonNumber())); //$NON-NLS-1$
		atoms.add(mp4Manager.createAtom("tves", episode.getEpisodeNumber())); //$NON-NLS-1$
		if (episode.getDate()!=null) {
			atoms.add(mp4Manager.createAtom("©day", YEAR_DF.format(episode.getDate()))); //$NON-NLS-1$
		}
		atoms.add(mp4Manager.createAtom("©nam", episode.getTitle())); //$NON-NLS-1$
		if (episode.getSummary()!=null && episode.getSummary().length()>0) {
			atoms.add(mp4Manager.createAtom("desc", episode.getSummary())); //$NON-NLS-1$
		}
//		atoms.add(new Atom("rtng", )); // None = 0, clean = 2, explicit  = 4

		if (episode.getSeason().getShow().getGenres().size() > 0) {
			atoms.add(mp4Manager.createAtom("©gen", episode.getSeason().getShow().getGenres().get(0))); //$NON-NLS-1$
			atoms.add(mp4Manager.createAtom("catg", episode.getSeason().getShow().getGenres().get(0))); //$NON-NLS-1$
		}
		IAtom artworkAtom = getArtworkAtom(mp4Manager, mp4File, episode);
		if (artworkAtom!=null) {
			atoms.add(artworkAtom);
		}
		mp4Manager.update(mp4File, atoms);
	}

	protected static IAtom getArtworkAtom(IMP4Manager mp4Manager, File mp4File,
			IVideo video) {
		File artwork = null;
		try {
			URL imageUrl = null;
			if (video instanceof IEpisode) {
				IEpisode episode = (IEpisode)video;
				if (episode.getImageURL()!=null) {
					imageUrl = episode.getImageURL();
				}
				else if (episode.getSeason().getShow().getImageURL()!=null) {
					imageUrl = episode.getSeason().getShow().getImageURL();
				}
			}
			else if (video instanceof IFilm) {
				imageUrl = ((IFilm) video).getImageURL();
			}
			if (imageUrl != null) {
				try {
					artwork = downloadToTempFile(imageUrl);
					byte data[] = getBytesFromFile(artwork);
					return mp4Manager.createAtom("covr", MP4ArtworkType.MP4_ART_JPEG,data.length,data ); //$NON-NLS-1$

				} catch (IOException e) {
					log.error(MessageFormat.format(Messages.getString("MP4ITunesStore.UNABLE_DOWNLOAD_ARTWORK"),imageUrl, mp4File.getName()),e); //$NON-NLS-1$
				}
			}
		}
		finally {
			if (artwork!=null) {
				try {
					FileHelper.delete(artwork);
				} catch (IOException e) {
					log.error(Messages.getString("MP4ITunesStore.UNABLE_DELETE_TEMP_FILE"),e); //$NON-NLS-1$
				}
			}
		}
		return null;
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
	public static void updateFilm(IMP4Manager mp4Manager ,File mp4File, IFilm film,Integer part) throws MP4Exception {
		DateFormat YEAR_DF = new SimpleDateFormat("yyyy"); //$NON-NLS-1$
		List<IAtom> atoms = new ArrayList<IAtom>();
		atoms.add(mp4Manager.createAtom("stik",StikValue.MOVIE.getId())); //$NON-NLS-1$
		if (film.getDate()!=null) {
			atoms.add(mp4Manager.createAtom("©day", YEAR_DF.format(film.getDate()))); //$NON-NLS-1$
		}
		atoms.add(mp4Manager.createAtom("©nam", film.getTitle())); //$NON-NLS-1$
		if (film.getSummary()!=null && film.getSummary().length()>0) {
			atoms.add(mp4Manager.createAtom("desc", film.getSummary())); //$NON-NLS-1$
		}
		if (film.getDescription()!=null && film.getDescription().length()>0) {
			atoms.add(mp4Manager.createAtom("ldes", film.getDescription())); //$NON-NLS-1$
		}
		if (film.getDirectors()!=null) {
			for (String director : film.getDirectors()) {
				atoms.add(mp4Manager.createAtom("©ART", director)); //$NON-NLS-1$
			}
		}
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
			atoms.add(mp4Manager.createAtom("disk",(byte)(int)part,total)); //$NON-NLS-1$
		}

		if (film.getPreferredGenre() != null) {
			atoms.add(mp4Manager.createAtom("©gen", film.getPreferredGenre())); //$NON-NLS-1$
			atoms.add(mp4Manager.createAtom("catg", film.getPreferredGenre())); //$NON-NLS-1$
		} else {
			if (film.getGenres().size() > 0) {
				atoms.add(mp4Manager.createAtom("©gen", film.getGenres().get(0))); //$NON-NLS-1$
				atoms.add(mp4Manager.createAtom("catg", film.getGenres().get(0))); //$NON-NLS-1$
			}
		}
		IAtom artworkAtom = getArtworkAtom(mp4Manager,mp4File,film);
		if (artworkAtom!=null) {
			atoms.add(artworkAtom);
		}
		mp4Manager.update(mp4File, atoms);
	}

	private static File downloadToTempFile(URL url) throws IOException {
		File file = FileHelper.createTempFile("artwork", ".jpg"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!file.delete()) {
			throw new IOException(MessageFormat.format(Messages.getString("MP4ITunesStore.UNABLE_DELETE_TEMP_FILE1"),file.getAbsolutePath())); //$NON-NLS-1$
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

			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);

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
				log.error(MessageFormat.format(Messages.getString("MP4ITunesStore.UNABLE_CLOSE_FILE"),file)); //$NON-NLS-1$
			}
		}
	}

	// Returns the contents of the file in a byte array.
	private static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(file);

		    // Get the size of the file
		    long length = file.length();

		    // You cannot create an array using a long type.
		    // It needs to be an int type.
		    // Before converting to an int type, check
		    // to ensure that file is not larger than Integer.MAX_VALUE.
		    if (length > Integer.MAX_VALUE) {
		        // File is too large
		    }

		    // Create the byte array to hold the data
		    byte[] bytes = new byte[(int)length];

		    // Read in the bytes
		    int offset = 0;
		    int numRead = 0;
		    while (offset < bytes.length
		           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		        offset += numRead;
		    }

		    // Ensure all the bytes have been read in
		    if (offset < bytes.length) {
		        throw new IOException(MessageFormat.format(Messages.getString("MP4ITunesStore.COULD_NOT_READ_FILE"),file.getName())); //$NON-NLS-1$
		    }
		    return bytes;
		}
	    // Close the input stream and return bytes
	    finally {
	    	try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ioe) {
				log.error(MessageFormat.format(Messages.getString("MP4ITunesStore.UNABLE_CLOSE_FILE"), file)); //$NON-NLS-1$
			}
	    }
	}

	/** {@inheritDoc} */
	@Override
	public List<IEpisode> listEpisodes(MediaDirConfig dirConfig,IProgressMonitor monitor) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public List<IFilm> listFilms(MediaDirConfig dirConfig,IProgressMonitor monitor) {
		return null;
	}

}
