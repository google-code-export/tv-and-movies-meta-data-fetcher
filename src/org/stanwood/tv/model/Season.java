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
package org.stanwood.tv.model;

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
	
	public URL getListingUrl() {
		return listingUrl;
	}

	public void setListingUrl(URL seasonListingUrl) {
		this.listingUrl = seasonListingUrl;
	}

	public URL getDetailedUrl() {
		return detailedUrl;
	}

	public void setDetailedUrl(URL seasonDetailedUrl) {
		this.detailedUrl = seasonDetailedUrl;
	}

	public void addEpisode(Episode episode) {
		episodes.put(episode.getEpisodeNumber(),episode);
	}
		
	public int getEpisodeCount() {
		return episodes.size();
	}
	
	public Episode getEpisode(int episodeNum) {
		Episode episode =episodes.get(episodeNum);		
		return episode;
	}
	
	public Episode getSpecial(int specialNumber) {		
		return specials.get(specialNumber-1);
	}

	public Collection<Episode> getEpisodes() {
		return episodes.values();
	}
		
	public int getSeasonNumber() {
		return seasonNumber;
	}
	
	public Show getShow() {
		return show;
	}

	public void addSepcial(Episode episode) {		
		specials.add(episode);
	}

	public List<Episode> getSpecials() {
		return specials;
	}
}
