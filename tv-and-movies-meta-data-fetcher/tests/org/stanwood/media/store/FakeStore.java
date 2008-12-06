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
package org.stanwood.media.store;

import java.io.File;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.SearchResult;

/**
 * This is a fake store that is used for testing of a stores params.
 */
public class FakeStore implements IStore {

	/** This is used to test that a param was test */
	public static String testParam1;
		
	/** 
	 * This does nothing as the store does not support it.
	 * @param episodeFile The file the episode is stored in
	 * @param episode The details of the episode
	 */
	@Override
	public void cacheEpisode(File episodeFile,Episode episode) {
	}

	/** 
	 * This does nothing as the store does not support it.
	 * @param episodeFile The file the episode is stored in
	 * @param season The details of the season
	 */
	@Override
	public void cacheSeason(File episodeFile,Season season) {
	}

	/** 
	 * This does nothing as the store does not support it.
	 * @param episodeFile The file the episode is stored in
	 * @param show The details of the show
	 */
	@Override
	public void cacheShow(File episodeFile,Show show) {
	}

	/**
	 * This always returns null as this store does not support reading episodes.
	 * @param episodeFile The file the episode is stored in
	 * @param season The season the episode belongs to.
	 * @param episodeNum The number of the episode to read
	 */
	@Override
	public Episode getEpisode(File episodeFile,Season season, int episodeNum) {
		return null;
	}

	/**
	 * This always returns null as this store does not support reading episodes.
	 * @param episodeFile The file the episode is stored in
	 * @param show The show the season belongs to.
	 * @param seasonNum The number of the season to read
	 */
	@Override
	public Season getSeason(File episodeFile,Show show, int seasonNum)  {
		return null;
	}

	/**
	 * This always returns null as this store does not support reading episodes.
	 * @param episodeFile The file the episode is stored in
	 * @param showId The id of the show to read
	 */
	@Override
	public Show getShow(File episodeFile, long showId)  {
		return null;
	}

	/**
	 * This always returns null as this store does not support reading episodes.
	 * @param episodeFile The file the episode is stored in
	 * @param season The season the episode belongs to.
	 * @param specialNumber The number of the special episode to read
	 */
	@Override
	public Episode getSpecial(File episodeFile,Season season, int specialNumber) {
		return null;
	}

	/**
	 * This will always return null as this store does not support searching
	 * @param episodeFile The file the episode is located in
	 * @return Always returns null
	 */
	@Override
	public SearchResult searchForShowId(File episodeFile) {
		return null;
	}

	/**
	 * Used to get the value of the test parameter
	 * @return The value of the test parameter
	 */
	public String getTestParam1() {
		return FakeStore.testParam1;
	}

	/**
	 * Used to set the value of the test parameter
	 * @param testParam1 The value of the test parameter
	 */
	public void setTestParam1(String testParam1) {
		FakeStore.testParam1 = testParam1;
	}

	/**
	 * This does nothing as this store does not support writing of films
	 * @param filmFile The file the film is stored in
	 * @param film The film details
	 */
	@Override
	public void cacheFilm(File filmFile, Film film) {
		
	}

	/**
	 * This does nothing as this store does not support writing of films
	 * @param oldFile The old file
	 * @param newFile The new file
	 */
	@Override
	public void renamedFile(File oldFile, File newFile) {
	}

	
	
}
