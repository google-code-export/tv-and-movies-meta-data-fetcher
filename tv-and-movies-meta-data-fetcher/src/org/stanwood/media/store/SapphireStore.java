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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Film;
import org.stanwood.media.model.Link;
import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;
import org.stanwood.media.renamer.SearchResult;

/**
 * This is a write only store that is used to store information in a format that can 
 * be used by the sapphire frontrow plugin. {@link "http://appletv.nanopi.net/"}. The details
 * of the XML format can be found here: {@link "http://appletv.nanopi.net/manual/overriding-metadata/"}.
 * 
 * Every time the @sa cacheEpisode(Episode) method is called, a XML file is written next to 
 * the episode's file with a .xml extension. 
 */
public class SapphireStore implements IStore {

	private final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	/** 
	 * This will store the episode and show details in a XML file next too the media file.
	 * The XML file will be in the format found here  {@link "http://appletv.nanopi.net/manual/overriding-metadata/"}.
	 * @param episode The episode to the stored
	 * @param episodeFile the file which the episode is stored in
	 * @throws StoreException Thrown if their is a problem writing to the store
	 */
	@Override
	public void cacheEpisode(File episodeFile,Episode episode) throws StoreException {
		try {					
			writeEpisode(episodeFile,episode);				
		}
		catch (IOException e) {
			throw new StoreException("Error creating spahire store",e);
		}
	}

	private void writeEpisode(File file, Episode episode) throws FileNotFoundException {
		int pos = file.getName().lastIndexOf('.');
		if (pos!=-1) {					
			String name = file.getName().substring(0,pos-1);
			File xmlFile = new File(file.getParent(),name+".xml");
			if (xmlFile.exists()) {
				xmlFile.delete();
			}
			PrintStream ps = null;
			try {
				ps = new PrintStream(new FileOutputStream(xmlFile));
				
				ps.println("<media>");
				ps.println("  <title>"+episode.getTitle()+"</title>");
				ps.println("     <summary>"+episode.getSummary()+"</summary>");
//				ps.println("     <description></description>");
//				ps.println("     <publisher>Publisher</publisher>");
//				ps.println("     <composer>Composer</composer>");
//				ps.println("     <copyright>Copyright</copyright>");
				ps.println("     <userStarRating>"+Math.round((episode.getRating()/10)*5)+"</userStarRating>");
//				ps.println("     <rating>TV-PG</rating>");
				ps.println("     <seriesName>"+episode.getSeason().getShow().getName()+"</seriesName>");
//				ps.println("     <broadcaster>The CW</broadcaster>");
				ps.println("     <episodeNumber>"+episode.getEpisodeSiteId()+"</episodeNumber>");
				ps.println("     <season>"+episode.getSeason().getSeasonNumber()+"</season>");
				ps.println("     <episode>"+episode.getEpisodeNumber()+"</episode>");
				ps.println("     <published>"+df.format(episode.getDate())+"</published>");
				ps.println("     <genres>");
				for (String genre : episode.getSeason().getShow().getGenres()) {
//					ps.println("        <genre primary="true">Mystery</genre>");
					ps.println("        <genre>"+genre+"</genre>");
				}
				ps.println("     </genres>");
				ps.println("     <cast>");
				for (Link cast : episode.getGuestStars()) {
					ps.println("        <name>"+cast+"</name>");
				}
				ps.println("     </cast>");
//				ps.println("     <producers>");				
//				ps.println("        <name>Rob Thomas</name>");
//				ps.println("     </producers>");
				ps.println("     <directors>");
				for (Link director : episode.getDirectors()) {
					ps.println("       <name>"+director+"</name>");
				}
				ps.println("     </directors>");
				ps.println("</media>");
			}
			finally {
				ps.close();
				ps = null;
			}
		}
		else {
			System.err.println("Unable to find extension of media file: " + file.getName()); 
		}
	}

	/**
	 * Does nothing as it is not implemented for this store
	 * @param season The season too store
	 * @param episodeFile the file witch the episode is stored in
	 */
	@Override
	public void cacheSeason(File episodeFile,Season season) {
	}

	/**
	 * Does nothing as it is not implemented for this store
	 * @param show The show too store
	 * @param episodeFile the file witch the episode is stored in
	 */
	@Override
	public void cacheShow(File episodeFile,Show show)  {
	}
	
	/**
	 * Always returns null as it is not implemented for this store.
	 * @param season The season the episode belongs too
	 * @param episodeNum The number of the episode
	 * @param episodeFile the file which the episode is stored in 
	 */
	@Override
	public Episode getEpisode(File episodeFile,Season season, int episodeNum) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * @param show The show the season belongs too
	 * @param seasonNum The number of the season
	 * @param episodeFile the file which the episode is stored in 
	 */
	@Override
	public Season getSeason(File episodeFile,Show show, int seasonNum) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * @param showId The id of the show 
	 * @param episodeFile the file which the episode is stored in
	 */
	@Override
	public Show getShow(File episodeFile, long showId) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * @param season The season the special episode belongs too
	 * @param specialNumber The number of the special episode
	 * @param episodeFile the file which the episode is stored in 
	 */
	@Override
	public Episode getSpecial(File episodeFile,Season season, int specialNumber) {
		return null;
	}

	/**
	 * Always returns null as it is not implemented for this store.
	 * @param episodeFile The file the episode is stored in 
	 */
	@Override
	public SearchResult searchForShowId(File episodeFile) {
		return null;
	}

	/**
	 * This is used to write a film to the store.
	 * @param filmFile The file which the film is stored in
	 * @param film The film to write
	 * @throws StoreException Thrown if their is a problem with the store
	 */
	@Override
	public void cacheFilm(File filmFile, Film film) throws StoreException {
		
	}
	
}
