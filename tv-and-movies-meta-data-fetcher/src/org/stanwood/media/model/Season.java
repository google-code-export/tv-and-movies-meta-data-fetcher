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
package org.stanwood.media.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is used to hold information on a season within a show
 */
public class Season {
	
	private int seasonNumber;	
	private Map<Integer,Episode>episodes = new HashMap<Integer,Episode>();
	private List<Episode> specials = new ArrayList<Episode>();
	
	private URL listingUrl;
	private URL detailedUrl;
	private Show show;	
	
	/**
	 * The constructor used to create a instance of the season.
	 * @param show The show the season belongs too
	 * @param seasonNumber The season number
	 */
	public Season(Show show,int seasonNumber) {		
		this.seasonNumber = seasonNumber;
		this.show = show;
	}	
	
	/**
	 * Used to get the episode listing URL for the season
	 * @return The episode listing URL for the season
	 */
	public URL getListingUrl() {
		return listingUrl;
	}

	/**
	 * Sets the episode listing URL for the season
	 * @param seasonListingUrl The episode listing URL for the season
	 */
	public void setListingUrl(URL seasonListingUrl) {
		this.listingUrl = seasonListingUrl;
	}

	/**
	 * Gets the detailed episode listing URL for the season
	 * @return The detailed episode listing URL for the season
	 */
	public URL getDetailedUrl() {
		return detailedUrl;
	}

	/**
	 * Sets the detailed episode listing URL for the season
	 * @param seasonDetailedUrl The detailed episode listing URL for the season
	 */
	public void setDetailedUrl(URL seasonDetailedUrl) {
		this.detailedUrl = seasonDetailedUrl;
	}

	/**
	 * Adds a episode to the season
	 * @param episode The episode to add to the season
	 */
	public void addEpisode(Episode episode) {
		episodes.put(episode.getEpisodeNumber(),episode);
	}
		
	/**
	 * Gets the number of episode's in the season
	 * @return The number of episodes in the season
	 */
	public int getEpisodeCount() {
		return episodes.size();
	}
	
	/**
	 * Used to get a episode with a given episode number
	 * @param episodeNum The episode number of the episode to fetch
	 * @return The episode
	 */
	public Episode getEpisode(int episodeNum) {
		Episode episode =episodes.get(episodeNum);		
		return episode;
	}
	
	/**
	 * Used to get a special episode with a given episode number
	 * @param episodeNum The special episode number of the episode to fetch
	 * @return The special episode
	 */
	public Episode getSpecial(int specialNumber) {		
		return specials.get(specialNumber-1);
	}

	/**
	 * Get all the episodes in the season
	 * @return The episodes in the season
	 */
	public Collection<Episode> getEpisodes() {
		return episodes.values();
	}
		
	/**
	 * Get the number of the season 
	 * @return The season number
	 */
	public int getSeasonNumber() {
		return seasonNumber;
	}
	
	/**
	 * Get the show the season belongs too
	 * @return The show the season belongs too
	 */
	public Show getShow() {
		return show;
	}

	/**
	 * Add a special episode too the season
	 * @param episode The special episode too add
	 */
	public void addSepcial(Episode episode) {		
		specials.add(episode);
	}

	/**
	 * Get all the special episodes in the season
	 * @return The special episodes in the season
	 */
	public List<Episode> getSpecials() {
		return specials;
	}
}
