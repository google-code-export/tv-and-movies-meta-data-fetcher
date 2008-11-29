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
import java.io.IOException;
import java.net.MalformedURLException;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.FileNameParser;
import org.stanwood.media.renamer.ParsedFileName;
import org.stanwood.media.renamer.SearchResult;
import org.stanwood.media.store.IStore;
import org.stanwood.media.store.StoreException;

/**
 * <p>
 * This store is used to store and retrieve film/TV show information from .mp4 files used
 * by iTunes. This allows iTuness to use the meta data retrieved by this tool and this tool
 * too use the meta data of iTunes. 
 * </p>
 * <p>
 * Reading and writing too the .mp4 files is done via the application AtomicParsley 
 * {@link http://atomicparsley.sourceforge.net/}. In order for this store to work, the AtomicParsley
 * command line tool must be installed. Use the method <code>setAtomicParsleyPath</code> too set the
 * of the application.  
 * </p>
 */
public class MP4ItunesStore implements IStore {

	private File atomicParsleyPath;
	
	public void cacheEpisode(Episode episode) throws StoreException {
		validate();
		File showDir = episode.getSeason().getShow().getShowDirectory();		
	
		for (File file : showDir.listFiles()) {
			ParsedFileName parsed = FileNameParser.parse(file.getName());
			if (parsed!=null && parsed.getEpisode() == episode.getEpisodeNumber() && parsed.getSeason()==episode.getSeason().getSeasonNumber()) {
				writeEpisode(file,episode);
				break;
			}
		}	
	}

	private void writeEpisode(File file, Episode episode) throws StoreException {
		AtomicParsley ap = new AtomicParsley(atomicParsleyPath);
		try {
			ap.updateEpsiode(file,episode);
		} catch (IOException e) {
			throw new StoreException(e.getMessage(),e);
		} catch (InterruptedException e) {
			throw new StoreException(e.getMessage(),e);
		}
	}

	public void cacheSeason(Season season) throws StoreException {
		validate();
	}

	public void cacheShow(Show show) throws StoreException {
		validate();
	}

	public Episode getEpisode(Season season, int episodeNum) throws StoreException, MalformedURLException, IOException {
		validate();
		return null;
	}

	public Season getSeason(Show show, int seasonNum) throws StoreException, IOException {
		validate();
		return null;
	}

	public Show getShow(File showDirectory, long showId) throws StoreException, MalformedURLException, IOException {
		validate();
		return null;
	}

	public Episode getSpecial(Season season, int specialNumber) throws MalformedURLException, IOException,
			StoreException {		
		return null;
	}

	public SearchResult searchForShowId(File showDirectory) throws StoreException {		
		return null;
	}

	/**
	 * Used to set the store parameter used to find the atomic parsley application
	 * @param atomicParsleyPath The path to the atomic parsley path
	 */
	public void setAtomicParsleyPath(String atomicParsleyPath) {
		this.atomicParsleyPath = new File(atomicParsleyPath);
	}

	/**
	 * Used to get the store parameter used to find the atomic parsley application
	 * @return The path to the atomic parsley path
	 */
	public String getAtomicParsleyPath() {
		return atomicParsleyPath.getAbsolutePath();
	}

	public void validate() throws StoreException {
		if (atomicParsleyPath==null) {
			throw new StoreException("The atomicParsleyPath property must be set before the store can be used");
		}
		if (!atomicParsleyPath.exists()) {
			throw new StoreException("Unable to locate the AtomicParsley application at the path '"+atomicParsleyPath.getAbsolutePath()+"'.");
		}
	}
}
