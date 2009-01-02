package org.stanwood.media.store.mythtv;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Mode;
import org.stanwood.media.model.SearchResult;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;

public class MythTVStore implements IStore {

	@Override
	public void cacheEpisode(File episodeFile, Episode episode) throws StoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cacheFilm(File filmFile, Film film) throws StoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cacheSeason(File episodeFile, Season season) throws StoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cacheShow(File episodeFile, Show show) throws StoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Episode getEpisode(File episodeFile, Season season, int episodeNum) throws StoreException,
			MalformedURLException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Film getFilm(File filmFile, String filmId) throws StoreException, MalformedURLException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Season getSeason(File episodeFile, Show show, int seasonNum) throws StoreException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Show getShow(File episodeFile, String showId) throws StoreException, MalformedURLException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Episode getSpecial(File episodeFile, Season season, int specialNumber) throws MalformedURLException,
			IOException, StoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void renamedFile(File oldFile, File newFile) throws StoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SearchResult searchForVideoId(Mode mode, File episodeFile) throws StoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
