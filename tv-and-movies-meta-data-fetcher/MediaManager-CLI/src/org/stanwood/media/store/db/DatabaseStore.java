package org.stanwood.media.store.db;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.database.DBHelper;
import org.stanwood.media.database.DatabaseException;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.VideoFile;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.DBResource;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;
import org.stanwood.media.store.StoreVersion;
import org.stanwood.media.util.Version;

/**
 * This store is used to store the show and film information in a database.
 * The database details are specified via a database resource.
 * <p>This source supports the following parameters:
 * <ul>
 * <li>resourceId - Required option, The ID of the database resource</li>
 * </ul>
 * </p>
 */
public class DatabaseStore implements IStore {

	private final static Log log = LogFactory.getLog(DatabaseStore.class);
	private final static StoreVersion STORE_VERSION = new StoreVersion(new Version("1.0"),1); //$NON-NLS-1$
	private Session session;
	private String resourceId;
	private Transaction currentTransaction = null;

	/** {@inheritDoc} */
	@Override
	public void cacheEpisode(File rootMediaDir, File episodeFile,
			IEpisode episode) throws StoreException {
		beginTransaction();
		cacheEpisodeNoTrans(rootMediaDir, episodeFile, episode);
		commitTransaction();
	}

	protected void cacheEpisodeNoTrans(File rootMediaDir, File episodeFile,
			IEpisode episode) throws StoreException {
		DBEpisode dbEpisode = findEpisode(rootMediaDir, episode);
		if (dbEpisode == null) {
			DBSeason season = findSeason(rootMediaDir, episode.getSeason());
			if (season == null) {
				cacheSeasonNoTrans(rootMediaDir,episodeFile,episode.getSeason());
				season = findSeason(rootMediaDir, episode.getSeason());
			}
			if (season == null) {
				throw new StoreException(
						MessageFormat
								.format(Messages.getString("DatabaseStore.UnableFindEpisode"), //$NON-NLS-1$
										episode.getEpisodeNumber(),
										episode.getSeason().getSeasonNumber(),
										episode.getSeason().getShow().getSourceId(),
										episode.getSeason().getShow().getShowId()));

			}
			dbEpisode = new DBEpisode();
			dbEpisode.setSeason(season);
			updateEpisode(episode, dbEpisode,episodeFile,rootMediaDir);
			season.getEpisodes().add(dbEpisode);
			session.saveOrUpdate(season);
		}
		else {
			updateEpisode(episode, dbEpisode,episodeFile,rootMediaDir);
			session.update(dbEpisode);
		}
	}

	protected void commitTransaction() {
		currentTransaction.commit();
		currentTransaction= null;
	}

	protected void beginTransaction() throws StoreException {
		if (currentTransaction!=null) {
			throw new StoreException(Messages.getString("DatabaseStore.TransactionAlreadyOpen")); //$NON-NLS-1$
		}
		currentTransaction = session.beginTransaction();
	}

	protected void updateEpisode(IEpisode episode, DBEpisode dbEpisode,File episodeFile,File rootMediaDir) {
		dbEpisode.setActors(episode.getActors());
		dbEpisode.setDate(episode.getDate());
		dbEpisode.setDirectors(episode.getDirectors());
		dbEpisode.setEpisodeId(episode.getEpisodeId());
		dbEpisode.setEpisodeNumber(episode.getEpisodeNumber());
		dbEpisode.setEpisodes(episode.getEpisodes());
		List<VideoFile> files = episode.getFiles();
		boolean found = false;
		for (VideoFile vf : files ) {
			if (vf.getLocation().equals(episodeFile)) {
				found = true;
			}
		}
		if (!found) {
			files.add(new VideoFile(episodeFile, episodeFile, null, rootMediaDir));
		}
		dbEpisode.setFiles(files);
		dbEpisode.setImageURL(episode.getImageURL());
		dbEpisode.setRating(episode.getRating());
		dbEpisode.setSummary(episode.getSummary());
		dbEpisode.setTitle(episode.getTitle());
		dbEpisode.setUrl(episode.getUrl());
		dbEpisode.setWriters(episode.getWriters());
		dbEpisode.setSpecial(episode.isSpecial());
	}

	private DBEpisode findEpisode(File mediaDir, IEpisode episode) {

		if (episode instanceof DBEpisode) {
			return (DBEpisode) episode;
		}

		Query q = session
				.createQuery(" from DBEpisode as episode " + //$NON-NLS-1$
						"where episode.season.show.showURL = :showUrl " + //$NON-NLS-1$
						"  and episode.season.seasonNumber = :seasonNum " + //$NON-NLS-1$
						"  and episode.episodeNumber = :episodeNum" + //$NON-NLS-1$
						"  and episode.season.show.mediaDirectory.location = :mediaDir"); //$NON-NLS-1$
		q.setString(
				"showUrl", episode.getSeason().getShow().getShowURL().toExternalForm()); //$NON-NLS-1$
		q.setInteger("seasonNum", episode.getSeason().getSeasonNumber()); //$NON-NLS-1$
		q.setInteger("episodeNum", episode.getEpisodeNumber()); //$NON-NLS-1$
		q.setString("mediaDir", mediaDir.getAbsolutePath()); //$NON-NLS-1$
		@SuppressWarnings("rawtypes")
		List result = q.list();
		if (result.size() > 0) {
			return (DBEpisode) result.get(0);
		}

		return null;
	}

	private DBShow findShow(File mediaDir, IShow show) {
		if (show instanceof DBShow) {
			return (DBShow) show;
		}

		Query q = session.createQuery("  from DBShow as show" + //$NON-NLS-1$
				" where show.showURL = :showUrl " + //$NON-NLS-1$
				"   and show.mediaDirectory.location = :mediaDir"); //$NON-NLS-1$
		q.setString("showUrl", show.getShowURL().toExternalForm()); //$NON-NLS-1$
		q.setString("mediaDir", mediaDir.getAbsolutePath()); //$NON-NLS-1$
		@SuppressWarnings("rawtypes")
		List result = q.list();
		if (result.size() > 0) {
			return (DBShow) result.get(0);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void cacheSeason(File rootMediaDir, File episodeFile, ISeason season)
			throws StoreException {
		beginTransaction();
		cacheSeasonNoTrans(rootMediaDir, episodeFile, season);
		commitTransaction();
	}

	protected void cacheSeasonNoTrans(File rootMediaDir, File episodeFile,
			ISeason season) throws StoreException {
		DBSeason dbSeason = findSeason(rootMediaDir, season);
		if (dbSeason == null) {
			if (log.isDebugEnabled()) {
				log.debug(MessageFormat.format(Messages.getString("DatabaseStore.CreatingNewSeason"),season.getSeasonNumber(),season.getShow().getShowId(),season.getShow().getSourceId())); //$NON-NLS-1$
			}
			DBShow show = findShow(rootMediaDir, season.getShow());
			if (show == null) {
				cacheShowNoTrans(rootMediaDir,episodeFile,season.getShow());
				show = findShow(rootMediaDir, season.getShow());
			}
			if (show == null) {
				throw new StoreException(
						MessageFormat
								.format(Messages.getString("DatabaseStore.UnableFidShow"), //$NON-NLS-1$
										season.getShow().getSourceId(), season.getShow().getShowId()));
			}
			dbSeason = new DBSeason();
			dbSeason.setShow(show);
			updateSeason(season, dbSeason);
			show.getSeasons().add(dbSeason);
			session.saveOrUpdate(show);
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug(MessageFormat.format(Messages.getString("DatabaseStore.UpdatingExistingSeason"),season.getSeasonNumber(),season.getShow().getShowId(),season.getShow().getSourceId())); //$NON-NLS-1$
			}
			updateSeason(season, dbSeason);
			session.update(dbSeason);
		}
	}

	protected void updateSeason(ISeason season, DBSeason dbSeason) {
		dbSeason.setSeasonNumber(season.getSeasonNumber());
		dbSeason.setURL(season.getURL());
	}

	private DBSeason findSeason(File mediaDir, ISeason season) {
		if (season instanceof DBSeason) {
			return (DBSeason) season;
		}

		Query q = session.createQuery(" from DBSeason as season " + //$NON-NLS-1$
				"where season.show.showURL = :showUrl " + //$NON-NLS-1$
				"  and season.seasonNumber = :seasonNum" + //$NON-NLS-1$
				"  and season.show.mediaDirectory.location = :mediaDir"); //$NON-NLS-1$
		q.setString("showUrl", season.getShow().getShowURL().toExternalForm()); //$NON-NLS-1$
		q.setInteger("seasonNum", season.getSeasonNumber()); //$NON-NLS-1$
		q.setString("mediaDir", mediaDir.getAbsolutePath()); //$NON-NLS-1$
		@SuppressWarnings("rawtypes")
		List result = q.list();
		if (result.size() > 0) {
			return (DBSeason) result.get(0);
		}
		return null;
	}

	@SuppressWarnings("javadoc")
	@Override
	public void cacheShow(File rootMediaDir, File episodeFile, IShow show)
			throws StoreException {
		beginTransaction();
		cacheShowNoTrans(rootMediaDir,episodeFile, show);

		commitTransaction();
	}

	protected void cacheShowNoTrans(File rootMediaDir,File episodeFile, IShow show) {
		DBShow foundShow = findShow(rootMediaDir, show);
		if (foundShow == null) {
			foundShow = new DBShow();
			DBMediaDirectory dir = getMediaDir(rootMediaDir, true);

			foundShow.setMediaDirectory(dir);
			updateShow(show, foundShow);
			dir.getShows().add(foundShow);
			session.saveOrUpdate(dir);
		}
		else {
			updateShow(show, foundShow);
			session.saveOrUpdate(foundShow);
		}
	}

	protected void updateShow(IShow show, DBShow foundShow) {
		foundShow.setShowId(show.getShowId());
		foundShow.setCertifications(show.getCertifications());
		foundShow.setExtraInfo(show.getExtraInfo());
		foundShow.setGenres(show.getGenres());
		foundShow.setImageURL(show.getImageURL());
		foundShow.setLongSummary(show.getLongSummary());
		foundShow.setName(show.getName());
		foundShow.setPreferredGenre(show.getPreferredGenre());
		foundShow.setShortSummary(show.getShortSummary());
		foundShow.setShowURL(show.getShowURL());
		foundShow.setSourceId(show.getSourceId());
		foundShow.setStudio(show.getStudio());
	}

	/** {@inheritDoc} */
	@Override
	public Collection<IFilm> listFilms(MediaDirConfig dirConfig,
			IProgressMonitor monitor) throws StoreException {
		beginTransaction();
		Collection<IFilm> result = listFilmsNoTrans(dirConfig);
		commitTransaction();
		return result;
	}

	@SuppressWarnings("unchecked")
	private Collection<IFilm> listFilmsNoTrans(MediaDirConfig dirConfig) throws StoreException {
		Query q = session
				.createQuery("select films from DBMediaDirectory as dir where dir.location=:loc"); //$NON-NLS-1$
		q.setString("loc", dirConfig.getMediaDir().getAbsolutePath()); //$NON-NLS-1$
		@SuppressWarnings("rawtypes")
		List result = q.list();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public IFilm getFilm(File rootMediaDir, File filmFile, String filmId)
			throws StoreException, MalformedURLException, IOException {
		beginTransaction();
		Film foundFilm = findFilm(filmFile, rootMediaDir);
		commitTransaction();
		return foundFilm;
	}

	/** {@inheritDoc} */
	@Override
	public IFilm getFilm(MediaDirectory dir, File file) throws StoreException {
		beginTransaction();
		Film film = findFilm(file, dir.getMediaDirConfig().getMediaDir());
		commitTransaction();
		return film;
	}

	/** {@inheritDoc} */
	@Override
	public void cacheFilm(File rootMediaDir, File filmFile, IFilm film,
			Integer part) throws StoreException {
		beginTransaction();
		DBMediaDirectory dir = getMediaDir(rootMediaDir, true);
		Film foundFilm = findFilm(film.getId(), film.getSourceId(), dir);
		if (foundFilm == null) {
			foundFilm = new Film();
			dir.getFilms().add(foundFilm);
		}

		foundFilm.setActors(film.getActors());
		foundFilm.setCertifications(film.getCertifications());
		foundFilm.setChapters(film.getChapters());
		foundFilm.setCountry(film.getCountry());
		foundFilm.setDate(film.getDate());
		foundFilm.setDescription(film.getDescription());
		foundFilm.setDirectors(film.getDirectors());
		foundFilm.setFilmUrl(film.getFilmUrl());
		foundFilm.setGenres(film.getGenres());
		foundFilm.setId(film.getId());
		foundFilm.setImageURL(film.getImageURL());
		foundFilm.setPreferredGenre(film.getPreferredGenre());
		foundFilm.setRating(film.getRating());
		foundFilm.setSourceId(film.getSourceId());
		foundFilm.setStudio(film.getStudio());
		foundFilm.setSummary(film.getSummary());
		foundFilm.setTitle(film.getTitle());
		foundFilm.setWriters(film.getWriters());

		List<VideoFile> files = film.getFiles();
		boolean found = false;
		for (VideoFile vf : files ) {
			if (vf.getLocation().equals(filmFile)) {
				found = true;
			}
		}
		if (!found) {
			files.add(new VideoFile(filmFile, filmFile, null, rootMediaDir));
		}
		foundFilm.setFiles(files);

		session.saveOrUpdate(dir);
		commitTransaction();
	}

	private Film findFilm(File file, File mediaDirLocation) {
		Query q = session
				.createQuery("select film from DBMediaDirectory as dir join dir.films as film join film.files as file where dir.location = :dirLoc and file.location = :loc"); //$NON-NLS-1$
		q.setString("dirLoc", mediaDirLocation.getAbsolutePath()); //$NON-NLS-1$
		q.setString("loc", file.getAbsolutePath()); //$NON-NLS-1$
		@SuppressWarnings("rawtypes")
		List result = q.list();
		if (result.size() > 0) {
			return (Film) result.get(0);
		}
		return null;
	}

	private Film findFilm(String id, String source, DBMediaDirectory dir) {
		Film foundFilm = null;
		for (Film f : dir.getFilms()) {
			if (f.getId().equals(id) && f.getSourceId().equals(source)) {
				foundFilm = f;
				break;
			}
		}
		return foundFilm;
	}

	private DBMediaDirectory getMediaDir(File rootMediaDir,
			boolean createIfNotFound) {
		Query q = session
				.createQuery("from DBMediaDirectory where location = :rootMediaDir"); //$NON-NLS-1$
		q.setParameter("rootMediaDir", rootMediaDir.getAbsolutePath()); //$NON-NLS-1$
		DBMediaDirectory dir = (DBMediaDirectory) q.uniqueResult();
		if (dir == null && createIfNotFound) {
			dir = new DBMediaDirectory();
			dir.setRevision(STORE_VERSION.getRevision());
			dir.setVersion(STORE_VERSION.getVersion());
			dir.setLocation(rootMediaDir.getAbsolutePath());
			session.save(dir);
		}
		return dir;
	}

	private DBEpisode findEpisode(File file, File mediaDirLocation) {
		Query q = session
				.createQuery("select episode " + //$NON-NLS-1$
						     "  from DBEpisode as episode join episode.files as file" + //$NON-NLS-1$
						     " where episode.season.show.mediaDirectory.location = :mediaDir" + //$NON-NLS-1$
						     "   and file.location = :loc"); //$NON-NLS-1$
		q.setString("mediaDir", mediaDirLocation.getAbsolutePath()); //$NON-NLS-1$
		q.setString("loc", file.getAbsolutePath()); //$NON-NLS-1$
		@SuppressWarnings("rawtypes")
		List result = q.list();
		if (result.size() > 0) {
			return (DBEpisode) result.get(0);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getEpisode(File rootMediaDir, File episodeFile,
			ISeason season, List<Integer> episodeNums) throws StoreException,
			MalformedURLException, IOException {
		DBEpisode episode = findEpisode(episodeFile,rootMediaDir);
		if (episode==null || episode.isSpecial()) {
			return null;
		}
		return episode;
	}

	/** {@inheritDoc} */
	@Override
	public ISeason getSeason(File rootMediaDir, File episodeFile, IShow show,
			int seasonNum) throws StoreException, IOException {
		DBEpisode episode = findEpisode(episodeFile,rootMediaDir);
		if (episode==null) {
			return null;
		}
		return episode.getSeason();
	}

	/** {@inheritDoc} */
	@Override
	public IShow getShow(File rootMediaDir, File episodeFile, String showId)
			throws StoreException, MalformedURLException, IOException {
		DBEpisode episode = findEpisode(episodeFile,rootMediaDir);
		if (episode==null) {
			return null;
		}
		return episode.getSeason().getShow();
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getSpecial(File rootMediaDir, File episodeFile,
			ISeason season, List<Integer> specialNumbers) throws MalformedURLException,
			IOException, StoreException {
		DBEpisode episode = findEpisode(episodeFile,rootMediaDir);
		if (episode==null ||  !episode.isSpecial()) {
			return null;
		}
		return episode;
	}

	/** {@inheritDoc} */
	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,
			MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		beginTransaction();
		try {
			if (mode == Mode.FILM) {
				Film film = findFilm(mediaFile, dirConfig.getMediaDir());
				if (film==null) {
					return null;
				}
				return new SearchResult(film.getId(), film.getSourceId(), film
						.getFilmUrl().toExternalForm(), part, mode);
			} else {
				DBEpisode result = findEpisode(mediaFile, dirConfig.getMediaDir());
				if (result!=null) {
					IShow show = result.getSeason().getShow();
					SearchResult sresult = new SearchResult(show.getShowId(), show.getSourceId(), show.getShowURL().toExternalForm(), null, mode);
					sresult.setEpisodes(result.getEpisodes());
					sresult.setSeason(result.getSeason().getSeasonNumber());
					return sresult ;
				}
			}
		}
		finally {
			commitTransaction();
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void renamedFile(File rootMediaDir, File oldFile, File newFile)
			throws StoreException {
		beginTransaction();
		Query q = session.createQuery("from VideoFile where location = :loc"); //$NON-NLS-1$
		q.setString("loc", oldFile.getAbsolutePath()); //$NON-NLS-1$
		@SuppressWarnings("rawtypes")
		List result = q.list();
		if (result.size() > 0) {
			VideoFile vf = (VideoFile) result.get(0);
			vf.setLocation(newFile);
			session.update(vf);
		}
		commitTransaction();
	}

	/** {@inheritDoc} */
	@Override
	public void setParameter(String key, String value) throws StoreException {
		if (key.equalsIgnoreCase(DatabaseStoreInfo.PARAM_DATABASE_RESOURCE_ID.getName())) {
			resourceId = value;
		}
		else {
			throw new StoreException(MessageFormat.format(
					Messages.getString("DatabaseStore.UnknownParam"), key)); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(String key) throws StoreException {
		if (key.equalsIgnoreCase(DatabaseStoreInfo.PARAM_DATABASE_RESOURCE_ID.getName())) {
			return resourceId;
		} else {
			throw new StoreException(MessageFormat.format(
					Messages.getString("DatabaseStore.UnknownParam"), key)); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	@Override
	public void performedActions(MediaDirectory dir) throws StoreException {
		beginTransaction();
		log.info(Messages.getString("DatabaseStore.CheckForDeletedFIles")); //$NON-NLS-1$
		Collection<IEpisode> epList = listEpisodesNoTrans(dir.getMediaDirConfig());
		for (IEpisode ep : epList) {
			DBEpisode dbEp = (DBEpisode) ep;
			Iterator<VideoFile> it = dbEp.getFiles().iterator();
			boolean changed = false;
			while (it.hasNext()) {
				VideoFile vf = it.next();
				if (!vf.getLocation().exists()) {
					it.remove();
					changed = true;
				}
			}
			if (changed) {
				session.update(dbEp);
			}
		}

		Collection<IFilm> films = listFilmsNoTrans(dir.getMediaDirConfig());
		for (IFilm film : films) {
			Iterator<VideoFile> it = film.getFiles().iterator();
			boolean changed = false;
			while (it.hasNext()) {
				VideoFile vf = it.next();
				if (!vf.getLocation().exists()) {
					it.remove();
					changed = true;
				}
			}
			if (changed) {
				session.update(film);
			}
		}
		commitTransaction();
		session.flush();
	}

	/** {@inheritDoc} */
	@Override
	public void fileDeleted(MediaDirectory dir, File file)
			throws StoreException {
		beginTransaction();
		if (dir.getMediaDirConfig().getMode() == Mode.TV_SHOW) {
			IEpisode episode = getEpisode(dir, file);
			Iterator<VideoFile> it = episode.getFiles().iterator();
			while (it.hasNext()) {
				VideoFile vf = it.next();
				if (vf.getLocation().equals(file)) {
					it.remove();
				}

			}

			session.update(episode);
		}
		else {
			IFilm film = findFilm(file,dir.getMediaDirConfig().getMediaDir());
			Iterator<VideoFile> it = film.getFiles().iterator();
			while (it.hasNext()) {
				VideoFile vf = it.next();
				if (vf.getLocation().equals(file)) {
					it.remove();
				}

			}

			session.update(film);

		}
		commitTransaction();
	}

	/** {@inheritDoc} */
	@Override
	public IEpisode getEpisode(MediaDirectory dir, File file)
			throws StoreException {
		return findEpisode(file, dir.getMediaDirConfig().getMediaDir());
	}

	/** {@inheritDoc} */
	@Override
	public void init(Controller controller, File nativeDir)
			throws StoreException {
		validateParameters();
		DBResource resource = controller.getDatabaseResources().get(resourceId);
		init(resource);
	}

	protected void init(DBResource resource) throws StoreException {
		currentTransaction = null;
		try {
			session = DBHelper.getInstance().getSession(resource);
		} catch (DatabaseException e) {
			throw new StoreException(Messages.getString("DatabaseStore.UnableTalkDB"),e); //$NON-NLS-1$
		}
	}

	private void validateParameters() throws StoreException {
		if (resourceId == null) {
			throw new StoreException(MessageFormat.format(
					Messages.getString("DatabaseStore.MissingRequiredParam"), //$NON-NLS-1$
					DatabaseStoreInfo.PARAM_DATABASE_RESOURCE_ID.getName()));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Collection<IEpisode> listEpisodesNoTrans(MediaDirConfig dirConfig) throws StoreException {
		Query q = session.createQuery("select episode " + //$NON-NLS-1$
                "  from DBMediaDirectory as dir " + //$NON-NLS-1$
                "  join dir.shows as show"+ //$NON-NLS-1$
                "  join show.seasons as season"+ //$NON-NLS-1$
                "  join season.episodes as episode"+ //$NON-NLS-1$
                " where dir.location=:loc"); //$NON-NLS-1$
		q.setString("loc", dirConfig.getMediaDir().getAbsolutePath()); //$NON-NLS-1$
		List result = q.list();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<IEpisode> listEpisodes(MediaDirConfig dirConfig,
			IProgressMonitor monitor) throws StoreException {
		beginTransaction();
		Collection<IEpisode> result = listEpisodesNoTrans(dirConfig);
		commitTransaction();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void upgrade(MediaDirectory mediaDirectory) throws StoreException {
	}

	/** {@inheritDoc} */
	@Override
	public void fileUpdated(MediaDirectory mediaDirectory, File file)
			throws StoreException {

	}

}
