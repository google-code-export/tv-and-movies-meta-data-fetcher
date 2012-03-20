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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.actions.ActionException;
import org.stanwood.media.info.IMediaFileInfo;
import org.stanwood.media.info.IVideoFileInfo;
import org.stanwood.media.info.ResolutionFormat;
import org.stanwood.media.logging.StanwoodException;
import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.IVideo;
import org.stanwood.media.model.IVideoFile;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.search.MediaSearcher;
import org.stanwood.media.setup.ConfigException;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.StoreVersion;
import org.stanwood.media.store.mp4.atomicparsley.MP4AtomicParsleyManager;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.NativeHelper;
import org.stanwood.media.util.Version;

/**
 * <p>
 * This store is used to store Film/TV show information in .mp4/.m4v files used
 * by iTunes. This allows iTunes to use the meta data and see the files complete with
 * their meta data.
 * </p>
 * <p>
 * In order to function, store uses the command line tools provided by the AtomicParsley application.
 * Their are different forks of this application on the internet. The most feature rich version I've
 * found is at {@link "https://bitbucket.org/wez/atomicparsley"}. Media manager uses this one to add
 * atoms that some of the other versions can't. The applicaiton must be installed on the PATH, or pointed
 * to by the optional store parameters.
 * </p>
 * <p>
 * If using a version of AtomicParsley that does not support the setting of all fields that this store
 * can handle, then a warning will be printed. A version with the above link that fully supports this store
 * can downloaded from the MediaManager web site or installed via the installer.
 * </p>
 * <p>This store has following optional parameters:
 * 	<ul>
 * 		<li>atomicparsley - The path to the AtomicParsley command</li>
 *  </ul>
 * </p>
 */
public class MP4ITunesStore implements IStore {

	private static final String CONFIG_DIRECTORY_PROP = "directory"; //$NON-NLS-1$
	private static final String CONFIG_NUM_DIRS = "numDirs"; //$NON-NLS-1$
	private static final String CONFIG_VERSION_PROP = "version"; //$NON-NLS-1$
	private static final String CONFIG_REVISION_PROP = "revision"; //$NON-NLS-1$
	private static final String CONFIG_FILE_NAME = "mp4-itunes-store.properties"; //$NON-NLS-1$

	private final static Log log = LogFactory.getLog(MP4ITunesStore.class);

	private IMP4Manager mp4Manager;
	private Class<? extends IMP4Manager> manager = MP4AtomicParsleyManager.class;
	private String atomicParsleyCmd;
	private MP4ITunesStoreInfo storeInfo;
	private Controller controller;

	private final static StoreVersion STORE_VERSION = new StoreVersion(new Version("2.1"),4); //$NON-NLS-1$

	public MP4ITunesStore(MP4ITunesStoreInfo storeInfo) {
		this.storeInfo = storeInfo;
	}

	/** {@inheritDoc} */
	@Override
	public void init(Controller controller,File nativeDir) throws StoreException {
		this.controller = controller;
		if (atomicParsleyCmd == null) {
			atomicParsleyCmd = NativeHelper.getNativeApplication(nativeDir,MP4ITunesStoreInfo.PARAM_ATOMIC_PARSLEY_KEY.getName());
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
			updateEpsiode(controller,getMP4Manager(),file,episode);
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
		String name = filmFile.getName();
		if (name.endsWith(".mp4") || name.endsWith(".m4v")) {  //$NON-NLS-1$//$NON-NLS-2$
			validate();
			writeFilm(filmFile,film,part);
		}
	}

	private void writeFilm(File filmFile, IFilm film, Integer part) throws StoreException {
		try {
			updateFilm(controller,getMP4Manager(),filmFile,film,part);
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

			mp4Manager.setParameter(MP4ITunesStoreInfo.PARAM_ATOMIC_PARSLEY_KEY.getName(),atomicParsleyCmd);
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
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_ATOMIC_PARSLEY_KEY.getName())){
			atomicParsleyCmd = value;
		}
		throw new StoreException(MessageFormat.format(Messages.getString("MP4ITunesStore.UNSUPPORTED_PARAM"),key)); //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws StoreException {
		if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_MANAGER_KEY.getName())) {
			return manager.getName();
		}
		else if (key.equalsIgnoreCase(MP4ITunesStoreInfo.PARAM_ATOMIC_PARSLEY_KEY.getName())){
			return atomicParsleyCmd;
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
	 * @param controller the media file controller
	 * @param mp4Manager MP4 Manager
	 * @param mp4File The MP4 file
	 * @param episode The episode details
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public static void updateEpsiode(Controller controller,IMP4Manager mp4Manager,File mp4File, IEpisode episode) throws MP4Exception {

		DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //$NON-NLS-1$
		DF.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$

		DateFormat DF1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		DF.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$

		// http://code.google.com/p/mp4v2/wiki/iTunesMetadata
		List<IAtom> atoms = new ArrayList<IAtom>();
		IShow show = episode.getSeason().getShow();
		atoms.add(mp4Manager.createAtom(MP4AtomKey.MM_VERSION,STORE_VERSION.toString()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.MEDIA_TYPE,StikValue.TV_SHOW.getId()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.TV_EPISODE_ID, episode.getEpisodeId()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.TV_SHOW_NAME, show.getName()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.TV_SEASON, episode.getSeason().getSeasonNumber()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.TV_EPISODE, episode.getEpisodeNumber()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.ARTIST, show.getName()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.ALBUM, MessageFormat.format("{0}, Series {1}",show.getName(),episode.getSeason().getSeasonNumber()))); //$NON-NLS-1$
		atoms.add(mp4Manager.createAtom(MP4AtomKey.SORT_ALBUM, MessageFormat.format("{0}, Series {1}",show.getName(),episode.getSeason().getSeasonNumber()))); //$NON-NLS-1$
		atoms.add(mp4Manager.createAtom(MP4AtomKey.SORT_ARTIST, show.getName()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.ALBUM_ARTIST, show.getName()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.SORT_ALBUM_ARTIST, show.getName()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.TRACK_NUMBER,(short)episode.getEpisodeNumber(),(short)0));
//		atoms.add(mp4Manager.createAtom(MP4AtomKey.GAPLESS_PLAYBACK, false));
//		atoms.add(mp4Manager.createAtom(MP4AtomKey.COMPILATION, false));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.DISK_NUMBER, (short)1,(short)1));
		if (episode.getDate()!=null) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.RELEASE_DATE, DF.format(episode.getDate())));
		}
		atoms.add(mp4Manager.createAtom(MP4AtomKey.PURCHASED_DATE, DF1.format(getPurchasedDate(mp4File))));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.NAME, episode.getTitle()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.SORT_NAME,  episode.getTitle()));
		if (show.getLongSummary()!=null ) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.DESCRIPTION_STORE, show.getLongSummary()));
		}
		else if (show.getShortSummary()!=null) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.DESCRIPTION_STORE, show.getShortSummary()));
		}
		if (episode.getSummary()!=null && episode.getSummary().length()>0) {

			atoms.add(mp4Manager.createAtom(MP4AtomKey.DESCRIPTION, getShortDescription(episode.getSummary(),100)));
			atoms.add(mp4Manager.createAtom(MP4AtomKey.DESCRIPTION_LONG, episode.getSummary()));
		}

		for (Certification cert : show.getCertifications()) {
			String value = certToItunesCert(cert);
			if (value!=null) {
				atoms.add(mp4Manager.createAtom(MP4AtomKey.CERTIFICATION, value));
			}
		}
//		atoms.add(new Atom("rtng", )); // None = 0, clean = 2, explicit  = 4

		if (episode.getSeason().getShow().getGenres().size() > 0) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.GENRE_USER_DEFINED, episode.getSeason().getShow().getGenres().get(0)));
			atoms.add(mp4Manager.createAtom(MP4AtomKey.CATEGORY, episode.getSeason().getShow().getGenres().get(0)));
		}

		IMediaFileInfo info = null;;
		try {
			if (controller!=null) {
				info = controller.getMediaFileInformation(mp4File);
			}
		} catch (StanwoodException e) {
			log.error("Unable to read media information",e);
		}
		String flavour = getFileFlavor(mp4File,info);
		genericAtoms(info,mp4Manager, atoms, flavour);

		StringBuilder iTuneMOVIValue = new StringBuilder();
		iTuneMOVIValue.append("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">"+FileHelper.LS); //$NON-NLS-1$
		iTuneMOVIValue.append("<plist version=\"1.0\">"+FileHelper.LS);		 //$NON-NLS-1$
		iTuneMOVIValue.append("<dict>"+FileHelper.LS); //$NON-NLS-1$
		iTuneMOVIValue.append("    <key>asset-info</key>"+FileHelper.LS); //$NON-NLS-1$
		genericFileInfo(info,mp4File, flavour, iTuneMOVIValue);
		if (episode.getActors()!=null && episode.getActors().size()>0) {
			iTuneMOVIValue.append("    <key>cast</key>"+FileHelper.LS); //$NON-NLS-1$
			iTuneMOVIValue.append("    <array>"+FileHelper.LS); //$NON-NLS-1$
			for (Actor actor : episode.getActors()) {
				iTuneMOVIValue.append("        <dict>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("            <key>name</key>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("            <string>"+actor.getName()+"</string>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
				iTuneMOVIValue.append("        </dict>"+FileHelper.LS); //$NON-NLS-1$
			}
			iTuneMOVIValue.append("    </array>"+FileHelper.LS); //$NON-NLS-1$
		}
		if (episode.getDirectors()!=null && episode.getDirectors().size()>0) {
			iTuneMOVIValue.append("    <key>directors</key>"+FileHelper.LS); //$NON-NLS-1$
			iTuneMOVIValue.append("    <array>"+FileHelper.LS); //$NON-NLS-1$
			for (String director : episode.getDirectors()) {
				iTuneMOVIValue.append("        <dict>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("            <key>name</key>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("            <string>"+director+"</string>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
				iTuneMOVIValue.append("        </dict>"+FileHelper.LS); //$NON-NLS-1$
			}
			iTuneMOVIValue.append("    </array>"+FileHelper.LS); //$NON-NLS-1$
		}
		if (show.getStudio()!=null) {
			iTuneMOVIValue.append("    <key>studio</key>"+FileHelper.LS); //$NON-NLS-1$
			iTuneMOVIValue.append("    <string>"+show.getStudio()+"</string>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
		}

		iTuneMOVIValue.append("</dict>"+FileHelper.LS); //$NON-NLS-1$
		iTuneMOVIValue.append("</plist>"+FileHelper.LS); //$NON-NLS-1$
		atoms.add(mp4Manager.createAtom(MP4AtomKey.INFO,iTuneMOVIValue.toString() ));

		IAtom artworkAtom = getArtworkAtom(mp4Manager, mp4File, episode);
		if (artworkAtom!=null) {
			atoms.add(artworkAtom);
		}
		mp4Manager.update(mp4File, atoms);
	}

	private static String getShortDescription(String summary, int len) {
		if (summary.length()>=len) {
			return summary.substring(0,len-1)+"…"; //$NON-NLS-1$
		}
		return summary;
	}

	protected static IAtom getArtworkAtom(IMP4Manager mp4Manager, File mp4File,IVideo video) throws MP4Exception {
		URL imageUrl = getVideoURL(video);
		if (imageUrl != null) {
			return getArtworkAtom(mp4Manager, mp4File, imageUrl);
		}
		return null;
	}

	private static IAtom getArtworkAtom(IMP4Manager mp4Manager, File mp4File, URL imageUrl) throws MP4Exception {
		File artwork = null;
		try {
				try {
					artwork = mp4Manager.getArtworkFile(imageUrl);
					byte data[] = getBytesFromFile(artwork);
					String ext = FileHelper.getExtension(artwork);
					MP4ArtworkType type = MP4ArtworkType.MP4_ART_JPEG;
					if (ext.equals("png")) {  //$NON-NLS-1$
						type = MP4ArtworkType.MP4_ART_PNG;
					}
					return mp4Manager.createAtom(MP4AtomKey.ARTWORK, type,data.length,data );
				} catch (SocketTimeoutException e) {
					throw new MP4Exception(MessageFormat.format("Unable to fetch image ''{0}'', timed out",imageUrl.toExternalForm()),e);
				} catch (IOException e) {
					log.error(MessageFormat.format(Messages.getString("MP4ITunesStore.UNABLE_DOWNLOAD_ARTWORK"),imageUrl, mp4File.getName()),e); //$NON-NLS-1$
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



	public static URL getVideoURL(IVideo video) {
		URL imageUrl = null;
		if (video instanceof IEpisode) {
			IEpisode episode = (IEpisode)video;
			if (episode.getSeason().getShow().getImageURL()!=null) {
				imageUrl = episode.getSeason().getShow().getImageURL();
			}
			else if (episode.getImageURL()!=null) {
				imageUrl = episode.getImageURL();
			}
		}
		else if (video instanceof IFilm) {
			imageUrl = ((IFilm) video).getImageURL();
		}
		return imageUrl;
	}



	/**
	 * Used to add atoms to a MP4 file that makes iTunes see it as a Film. It also removes any artwork before adding the
	 * Film atoms and artwork.
	 * @param controller The media controller
	 * @param mp4Manager MP4 Manager
	 * @param mp4File The MP4 file
	 * @param film The film details
	 * @param part The part number of the film, or null if it does not have parts
	 * @throws MP4Exception Thrown if their is a problem updating the atoms
	 */
	public static void updateFilm(Controller controller,IMP4Manager mp4Manager ,File mp4File, IFilm film,Integer part) throws MP4Exception {
		DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //$NON-NLS-1$
		DF.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$

		DateFormat DF1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		DF1.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$

		List<IAtom> atoms = new ArrayList<IAtom>();
		atoms.add(mp4Manager.createAtom(MP4AtomKey.MM_VERSION,STORE_VERSION.toString()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.MEDIA_TYPE,StikValue.SHORT_FILM.getId()));
//		atoms.add(mp4Manager.createAtom(MP4AtomKey.GAPLESS_PLAYBACK, false));
//		atoms.add(mp4Manager.createAtom(MP4AtomKey.COMPILATION, false));
		if (film.getDate()!=null) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.RELEASE_DATE, DF.format(film.getDate())));
		}
		atoms.add(mp4Manager.createAtom(MP4AtomKey.PURCHASED_DATE, DF1.format(getPurchasedDate(mp4File))));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.NAME, film.getTitle()));
		atoms.add(mp4Manager.createAtom(MP4AtomKey.SORT_NAME,  film.getTitle()));
		if (film.getSummary()!=null && film.getSummary().length()>0) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.DESCRIPTION, film.getSummary()));
		}
		if (film.getDescription()!=null && film.getDescription().length()>0) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.DESCRIPTION_LONG, film.getDescription()));
		}
		if (film.getDirectors()!=null) {
			for (String director : film.getDirectors()) {
				atoms.add(mp4Manager.createAtom(MP4AtomKey.ARTIST, director));
				atoms.add(mp4Manager.createAtom(MP4AtomKey.SORT_ARTIST, director));
			}
		}

		for (Certification cert : film.getCertifications()) {
			String value = certToItunesCert(cert);
			if (value!=null) {
				atoms.add(mp4Manager.createAtom(MP4AtomKey.CERTIFICATION, value));
			}
		}

//		atoms.add(mp4Manager.createAtom("rtng", )); // None = 0, clean = 2, explicit  = 4
		if (part!=null) {
			byte total =0;
			for (IVideoFile vf : film.getFiles()) {
				if (vf.getPart()!=null && vf.getPart()>total) {
					total = (byte)(int)vf.getPart();
				}
			}

			if (part>total) {
				total = (byte)(int)part;
			}
			atoms.add(mp4Manager.createAtom(MP4AtomKey.DISK_NUMBER,(byte)(int)part,total));
		}

		if (film.getPreferredGenre() != null) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.GENRE_USER_DEFINED, film.getPreferredGenre()));
			atoms.add(mp4Manager.createAtom(MP4AtomKey.CATEGORY, film.getPreferredGenre()));
		} else {
			if (film.getGenres().size() > 0) {
				atoms.add(mp4Manager.createAtom(MP4AtomKey.GENRE_USER_DEFINED, film.getGenres().get(0)));
				atoms.add(mp4Manager.createAtom(MP4AtomKey.CATEGORY, film.getGenres().get(0)));
			}
		}
		IMediaFileInfo info = null;
		try {
			if (controller!=null) {
				info = controller.getMediaFileInformation(mp4File);
			}
		} catch (StanwoodException e) {
			log.error("Unable to read media information",e);
		}
		String flavour = getFileFlavor(mp4File,info);
		genericAtoms(info,mp4Manager, atoms, flavour);


		StringBuilder iTuneMOVIValue = new StringBuilder();
		iTuneMOVIValue.append("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">"+FileHelper.LS); //$NON-NLS-1$
		iTuneMOVIValue.append("<plist version=\"1.0\">"+FileHelper.LS);		 //$NON-NLS-1$
		iTuneMOVIValue.append("<dict>"+FileHelper.LS); //$NON-NLS-1$
		iTuneMOVIValue.append("    <key>asset-info</key>"+FileHelper.LS); //$NON-NLS-1$
		genericFileInfo(info,mp4File, flavour, iTuneMOVIValue);
		if (film.getActors()!=null && film.getActors().size()>0) {
			iTuneMOVIValue.append("    <key>cast</key>"+FileHelper.LS); //$NON-NLS-1$
			iTuneMOVIValue.append("    <array>"+FileHelper.LS); //$NON-NLS-1$
			for (Actor actor : film.getActors()) {
				iTuneMOVIValue.append("        <dict>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("            <key>name</key>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("            <string>"+actor.getName()+"</string>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
				iTuneMOVIValue.append("        </dict>"+FileHelper.LS); //$NON-NLS-1$
			}
			iTuneMOVIValue.append("    </array>"+FileHelper.LS); //$NON-NLS-1$
		}
		if (film.getDirectors()!=null && film.getDirectors().size()>0) {
			iTuneMOVIValue.append("    <key>directors</key>"+FileHelper.LS); //$NON-NLS-1$
			iTuneMOVIValue.append("    <array>"+FileHelper.LS); //$NON-NLS-1$
			for (String director : film.getDirectors()) {
				iTuneMOVIValue.append("        <dict>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("            <key>name</key>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("            <string>"+director+"</string>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
				iTuneMOVIValue.append("        </dict>"+FileHelper.LS); //$NON-NLS-1$
			}
			iTuneMOVIValue.append("    </array>"+FileHelper.LS); //$NON-NLS-1$
		}
		if (film.getStudio()!=null) {
			iTuneMOVIValue.append("    <key>studio</key>"+FileHelper.LS); //$NON-NLS-1$
			iTuneMOVIValue.append("    <string>"+film.getStudio()+"</string>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
		}

		iTuneMOVIValue.append("</dict>"+FileHelper.LS); //$NON-NLS-1$
		iTuneMOVIValue.append("</plist>"+FileHelper.LS); //$NON-NLS-1$
		atoms.add(mp4Manager.createAtom(MP4AtomKey.INFO,iTuneMOVIValue.toString() ));

		IAtom artworkAtom = getArtworkAtom(mp4Manager,mp4File,film);
		if (artworkAtom!=null) {
			atoms.add(artworkAtom);
		}
		mp4Manager.update(mp4File, atoms);
	}

	protected static void genericAtoms(IMediaFileInfo info, IMP4Manager mp4Manager,
			List<IAtom> atoms, String flavour) {
		if (flavour!=null) {
			atoms.add(mp4Manager.createAtom(MP4AtomKey.FLAVOUR, flavour));
		}
		if (info!=null && info instanceof IVideoFileInfo) {
			IVideoFileInfo vinfo = (IVideoFileInfo)info;
			if (vinfo.isHighDef()) {
				if (vinfo.getResolutionFormat()!=null && vinfo.getResolutionFormat()==ResolutionFormat.Format_1080p) {
					atoms.add(mp4Manager.createAtom(MP4AtomKey.HD, 2));
				}
				else {
					atoms.add(mp4Manager.createAtom(MP4AtomKey.HD, 1));
				}
			}
		}
	}

	private static String getFileFlavor(File mp4File, IMediaFileInfo info) {
		// 1:128 // 128 bit audio
		// 2:256 // 256 bit audio

		if (info!=null && info instanceof IVideoFileInfo) {
			IVideoFileInfo vinfo = (IVideoFileInfo)info;
			if (vinfo.getResolutionFormat() == ResolutionFormat.Format_1080p) {
				return "18:1080p"; //$NON-NLS-1$
			}
			else if (vinfo.getResolutionFormat() == ResolutionFormat.Format_720p) {
				return "7:720p"; //$NON-NLS-1$
			}
			else if (vinfo.getResolutionFormat() == ResolutionFormat.Format_480p) {
				if (vinfo.getAudioFormatProfile()!=null && vinfo.getAudioFormatProfile().equals("LC")) { //$NON-NLS-1$
					if (vinfo.getAudioBitRate()!=null && vinfo.getAudioBitRate()<256) {
						return "4:640x480LC-128"; //$NON-NLS-1$
					}
					else if (vinfo.getAudioBitRate()!=null && vinfo.getAudioBitRate()>=256) {
						return "6:640x480LC-256"; //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}

	private static void genericFileInfo(IMediaFileInfo info, File mp4File, String flavour,StringBuilder iTuneMOVIValue) {
		iTuneMOVIValue.append("    <dict>"+FileHelper.LS); //$NON-NLS-1$
		iTuneMOVIValue.append("        <key>file-size</key>"+FileHelper.LS); //$NON-NLS-1$
		iTuneMOVIValue.append("        <integer>"+mp4File.length()+"</integer>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
		if (flavour!=null) {
			iTuneMOVIValue.append("        <key>flavor</key>"+FileHelper.LS); //$NON-NLS-1$
			iTuneMOVIValue.append("        <string>"+flavour+"</string>"+FileHelper.LS); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (info!=null && info instanceof IVideoFileInfo) {
			IVideoFileInfo vinfo = (IVideoFileInfo)info;
			if (vinfo.isHighDef()) {
				iTuneMOVIValue.append("        <key>high-definition</key>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("        <true/>"+FileHelper.LS); //$NON-NLS-1$
			}
			if (vinfo.isWideScreen()) {
				iTuneMOVIValue.append("        <key>screen-format</key>"+FileHelper.LS); //$NON-NLS-1$
				iTuneMOVIValue.append("        <string>widescreen</string>"+FileHelper.LS); //$NON-NLS-1$
			}
		}
		iTuneMOVIValue.append("    </dict>"+FileHelper.LS); //$NON-NLS-1$
	}

	private static Date getPurchasedDate(File mp4File) {
		Date lastModified = new Date(mp4File.lastModified());
		return lastModified;
	}

	@SuppressWarnings("nls")
	private static String certToItunesCert(Certification cert) {
		if (cert.getType().equalsIgnoreCase("mpaa")) {
			String certValue = cert.getCertification();
			certValue = certValue.replaceAll("Rated ","");
			if (certValue.equalsIgnoreCase("G")) {
				return "mpaa|PG|100|";
			}
			else if (certValue.equalsIgnoreCase("PG")) {
				return "mpaa|PG|200|";
			}
			else if (certValue.equalsIgnoreCase("PG-13")) {
				return "mpaa|PG-13|300|";
			}
			else if (certValue.equalsIgnoreCase("R")) {
				return "mpaa|R|400|";
			}
			else if (certValue.equalsIgnoreCase("NC-17")) {
				return "mpaa|R|500|";
			}
			else if (certValue.equalsIgnoreCase("TV-Y")) {
				return "us-tv|TV-V|100|";
			}
			else if (certValue.equalsIgnoreCase("TV-Y7")) {
				return "us-tv|TV-V7|200|";
			}
			else if (certValue.equalsIgnoreCase("TV-G")) {
				return "us-tv|TV-G|300|";
			}
			else if (certValue.equalsIgnoreCase("TV-PG")) {
				return "us-tv|TV-PG|400|";
			}
			else if (certValue.equalsIgnoreCase("TV-14")) {
				return "us-tv|TV-14|500|";
			}
			else if (certValue.equalsIgnoreCase("TV-MA")) {
				return "us-tv|TV-MA|600|";
			}
		}
		return null;
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

	private void upgradeMediaFiles(MediaDirectory mediaDir,File file) throws StoreException {
		if (file.isDirectory()) {
			for (File d : file.listFiles()) {
				upgradeMediaFiles(mediaDir,d);
			}
		}
		else {
			if (isItunesExtension(file)) {
				try {
					List<IAtom> atoms = mp4Manager.listAtoms(file);
					if (hasAtom(atoms,MP4AtomKey.MEDIA_TYPE)) {
						doUpgrade(atoms,mediaDir,file);
					}
				}
				catch (MP4Exception e) {
					throw new StoreException(MessageFormat.format("Unable to process mp4 atoms of file ''{0}''",file),e);
				}
			}
		}
	}

	private boolean hasAtom(List<IAtom> atoms, MP4AtomKey type) {
		for (IAtom atom : atoms) {
			if (atom.getKey().equals(type)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isItunesExtension(File file) {
		String ext = FileHelper.getExtension(file);
		return (ext.equals("m4v") || ext.equals("mp4"));  //$NON-NLS-1$//$NON-NLS-2$
	}

	private StoreVersion getVersion(File file,List<IAtom> atoms) throws StoreException,MP4Exception {
		StoreVersion version = new StoreVersion(new Version("2.0"),1); //$NON-NLS-1$
		for (IAtom atom : atoms) {
			if (atom.getKey() == MP4AtomKey.MM_VERSION && atom instanceof IAtomString) {
				String value = ((IAtomString)atom).getValue();
				int pos = value.indexOf(" "); //$NON-NLS-1$
				version.setVersion(new Version(value.substring(0,pos)));
				version.setRevision(Integer.parseInt(value.substring(pos+1)));
				return version;
			}
		}
		return version;
	}

	private void doUpgrade(List<IAtom> atoms,MediaDirectory mediaDir,File file) throws StoreException, MP4Exception {
		for (IStore store : mediaDir.getStores()) {
			if (store!=this) {
				store.upgrade(mediaDir);
			}
		}

		StoreVersion currentVersion = getVersion(file,atoms);
		if (currentVersion.getVersion().compareTo(STORE_VERSION.getVersion())<0) {
			upgradeAtoms(mediaDir,file,atoms);
		}
		else {
			if (currentVersion.getRevision()<STORE_VERSION.getRevision()) {
				upgradeAtoms(mediaDir,file,atoms);
			}
		}
	}

	private void upgradeAtoms(MediaDirectory mediaDir,File file,List<IAtom> atoms) throws StoreException {
		try {
			if (mediaDir.getMediaDirConfig().getMode()==Mode.TV_SHOW) {
				IEpisode episode = MediaSearcher.getTVEpisode(mediaDir, file,true);
				if (episode!=null) {
					updateEpsiode(controller,mp4Manager, file, episode);
					mediaDir.fileChanged(file);
				}
			}
			else {
				IFilm film;
				try {
					film = MediaSearcher.getFilm(mediaDir, file,true);
					if (film!=null) {
						Integer part = null;
						for (IVideoFile vfile : film.getFiles()) {
							if (vfile.getLocation().equals(file)) {
								part = vfile.getPart();
								break;
							}
						}
						updateFilm(controller,mp4Manager,file, film,part);
						mediaDir.fileChanged(file);
					}
				}
				catch (ActionException e) {
					log.error(MessageFormat.format("Unable to read film details of file ''{0}''", file.getAbsoluteFile()));
				}
			}
			log.info(MessageFormat.format("Updated atoms of file ''{0}''",file));
		}
		catch (StoreException e) {
			throw new StoreException(MessageFormat.format("Unable to find media details for file ''{0}''",file),e);
		} catch (MP4Exception e) {
			throw new StoreException(MessageFormat.format("Unable to find media details for file ''{0}''",file),e);
		} catch (ActionException e) {
			throw new StoreException(MessageFormat.format("Unable to find media details for file ''{0}''",file),e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void upgrade(MediaDirectory mediaDirectory) throws StoreException {
		if (mediaDirectory.getMediaDirConfig().getMode() == Mode.FILM) {
			StoreVersion currentVersion = getCurrentVersion(mediaDirectory);
			if (currentVersion.getVersion().compareTo(STORE_VERSION.getVersion())<0) {
				log.info(MessageFormat.format("Upgrading store {0} at location {1}",storeInfo.getId(),mediaDirectory.getMediaDirConfig().getMediaDir()));
				upgradeMediaFiles(mediaDirectory,mediaDirectory.getMediaDirConfig().getMediaDir());
				saveStoreVersion(mediaDirectory);
				log.info(MessageFormat.format("Upgrade complete",storeInfo.getId(),mediaDirectory.getMediaDirConfig().getMediaDir()));
			}
		}
	}

	protected void saveStoreVersion(MediaDirectory mediaDirectory)
			throws StoreException {
		File configFile;
		try {
			configFile = new File(mediaDirectory.getController().getConfigDir(),CONFIG_FILE_NAME);
		} catch (ConfigException e1) {
			throw new StoreException("Unable to read store version",e1);
		}
		Properties props = readStoreConfig(mediaDirectory, configFile);
		Integer num = findMediaDirNumber(mediaDirectory,props);
		if (num==null) {
			num = Integer.parseInt(props.getProperty(CONFIG_NUM_DIRS,"0"));
			props.setProperty(CONFIG_NUM_DIRS,String.valueOf(num+1));
		}

		props.setProperty(CONFIG_DIRECTORY_PROP+"_"+num, mediaDirectory.getMediaDirConfig().getMediaDir().getAbsolutePath()); //$NON-NLS-1$
		props.setProperty(CONFIG_VERSION_PROP+"_"+num, STORE_VERSION.getVersion().toString()); //$NON-NLS-1$
		props.setProperty(CONFIG_REVISION_PROP+"_"+num, String.valueOf(STORE_VERSION.getRevision())); //$NON-NLS-1$

		OutputStream fs  = null;
		try {
			fs = new FileOutputStream(configFile);
			props.store(fs, ""); //$NON-NLS-1$

		}
		catch (IOException e) {
			throw new StoreException(MessageFormat.format("Unable to wtite configuration file ''{0}''",configFile),e);
		}
		finally  {
			if (fs!=null) {
				try {
					fs.close();
				} catch (IOException e) {
					log.error(MessageFormat.format("Unable to close file: {0}",configFile),e);
				}
			}
		}
	}

	protected Properties readStoreConfig(MediaDirectory mediaDirectory,
			File configFile) throws StoreException {
		Properties props = new Properties();
		if (configFile.exists()) {
			InputStream is = null;
			try {
				is = new FileInputStream(configFile);
				props.load(is);
			} catch (FileNotFoundException e) {

			} catch (IOException e) {
				throw new StoreException("Unable to read store configuration",e);
			}
			finally {
				if (is!=null) {
					try {
						is.close();
					} catch (IOException e) {
						log.error("Unable to close stream",e);
					}
				}
			}
		}
		return props;
	}

	private Integer findMediaDirNumber(MediaDirectory mediaDirectory,Properties props) {
		int numberStores = Integer.parseInt(props.getProperty(CONFIG_NUM_DIRS,"0"));
		for (int i=0;i<numberStores;i++) {
			String dir = props.getProperty(CONFIG_DIRECTORY_PROP+"_"+i); //$NON-NLS-1$
			if (dir!=null && dir.equals(mediaDirectory.getMediaDirConfig().getMediaDir().getAbsolutePath())) {
				return i;
			}
		}
		return null;
	}

	private StoreVersion getCurrentVersion(MediaDirectory mediaDirectory) throws StoreException {
		try {
			File configFile = new File(mediaDirectory.getController().getConfigDir(),CONFIG_FILE_NAME);
			if (!configFile.exists()) {
				return new StoreVersion(new Version("2.0"),1); //$NON-NLS-1$
			}
			Properties props = readStoreConfig(mediaDirectory, configFile);

			Integer i= findMediaDirNumber(mediaDirectory,props);
			if (i!=null) {
				String dir = props.getProperty(CONFIG_DIRECTORY_PROP+"_"+i); //$NON-NLS-1$
				if (dir.equals(mediaDirectory.getMediaDirConfig().getMediaDir().getAbsolutePath())) {
					int revision =1;
					try {
						revision = Integer.parseInt(props.getProperty(CONFIG_REVISION_PROP+"_"+i,"1")); //$NON-NLS-1$
					}
					catch (NumberFormatException e) {

					}
					return new StoreVersion(new Version(props.getProperty(CONFIG_VERSION_PROP+"_"+i, "2.0")),revision);
				}
			}

			return new StoreVersion(new Version("2.0"),1); //$NON-NLS-1$
		}
		catch (ConfigException e) {
			throw new StoreException("Unable to read store configuration",e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void fileUpdated(MediaDirectory mediaDirectory, File file)
			throws StoreException {

	}
}
