package org.stanwood.media.test.stores;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.Controller;
import org.stanwood.media.MediaDirectory;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.progress.IProgressMonitor;
import org.stanwood.media.setup.MediaDirConfig;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;

public class TestStore implements IStore {

	private static List<String>events = new ArrayList<String>();

	@Override
	public void cacheEpisode(File rootMediaDir, File episodeFile,
			IEpisode episode) throws StoreException {
		events.add("cacheEpisode()");
	}

	@Override
	public void cacheSeason(File rootMediaDir, File episodeFile, ISeason season)
			throws StoreException {
		events.add("cacheSeason()");
	}

	@Override
	public void cacheShow(File rootMediaDir, File episodeFile, IShow show)
			throws StoreException {
		events.add("cacheShow()");
	}

	@Override
	public void cacheFilm(File rootMediaDir, File filmFile, IFilm film, Integer part) throws StoreException {
		events.add("cacheFilm()");
	}

	

	@Override
	public ISeason getSeason(File rootMediaDir, File episodeFile, IShow show,
			int seasonNum) throws StoreException, IOException {
		events.add("getSeason()");
		return null;
	}

	@Override
	public IShow getShow(File rootMediaDir, File episodeFile, String showId)
			throws StoreException, MalformedURLException, IOException {
		events.add("getShow()");
		return null;
	}

	

	@Override
	public SearchResult searchMedia(String name, Mode mode, Integer part,
			MediaDirConfig dirConfig, File mediaFile) throws StoreException {
		events.add("searchMedia()");
		return null;
	}

	@Override
	public void renamedFile(File rootMediaDir, File oldFile, File newFile)
			throws StoreException {
		events.add("renamedFile()");
	}

	@Override
	public IFilm getFilm(File rootMediaDir, File filmFile, String filmId)
			throws StoreException, MalformedURLException, IOException {
		events.add("getFilm()");
		return null;
	}

	public static List<String>getEvents() {
		return events;
	}

	@Override
	public void setParameter(String key, String value) {
		events.add("setParameter()");
	}

	@Override
	public String getParameter(String key) {
		events.add("getParameter()");
		return null;
	}

	@Override
	public void performedActions(MediaDirectory dir) throws StoreException {
		events.add("performedActions()");
	}

	@Override
	public void fileDeleted(MediaDirectory dir, File file) {
		events.add("fileDeleted()");
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

	@Override
	public void init() throws StoreException {

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

	/** {@inheritDoc} */
	@Override
	public void upgrade(MediaDirectory mediaDirectory) {

	}
	
	/** {@inheritDoc} */
	@Override
	public void fileUpdated(MediaDirectory mediaDirectory, File file)
			throws StoreException {
		
	}

	@Override
	public IEpisode getEpisode(File rootMediaDir, File episodeFile,
			ISeason season, List<Integer> episodeNums) throws StoreException,
			MalformedURLException, IOException {
		events.add("getEpisode()");
		return null;
	}

	@Override
	public IEpisode getSpecial(File rootMediaDir, File episodeFile,
			ISeason season, List<Integer> specialNumbers)
			throws MalformedURLException, IOException, StoreException {
		events.add("getSpecial()");
		return null;
	}

	@Override
	public boolean fileKnownByStore(MediaDirectory mediaDirectory, File file)
			throws StoreException {
		// TODO Auto-generated method stub
		return false;
	}
}
