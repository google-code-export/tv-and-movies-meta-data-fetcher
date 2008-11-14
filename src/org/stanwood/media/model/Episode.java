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
import java.util.Date;
import java.util.List;

/**
 * This is used to store episode information
 */
public class Episode {
	
	private Season season;
	private int episodeNumber;
	
	private String summary;
	private String title;
	private Date airDate;
	private int totalNum;
	private String specialName;
	private boolean special;
	private String siteId;
	private String productionCode;
	private URL summaryUrl;
	private long episodeId;
		
	private List<Link>guestStars;
	private List<Link>directors;
	private List<Link>writers;
	private float rating = -1;
	
	public Episode(int episodeNumber, Season season) {		
		this.episodeNumber = episodeNumber;
		this.season = season;
	}

	/**
	 * Get the season the episode belongs too 
	 * @return The season the episode belongs too
	 */
	public Season getSeason() {
		return season;
	}

	/**
	 * Gets the number of the episode. This excludes specials.
	 * @return The number of the episode
	 */
	public int getEpisodeNumber() {
		return episodeNumber;
	}

	/**
	 * Get a summary of the episode
	 * @return The summary of the episode
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Sets the summary of the episode
	 * @param summary The summary of the episode
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Sets the title of the episode
	 * @param title The title of the episode
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets the air date of the show
	 * @param airDate The air date of the show
	 */
	public void setAirDate(Date airDate) {
		this.airDate = airDate;
	}

	/**
	 * Gets the title of the show
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * If this is a special, then true should be passed to this method to flag it as such.
	 * @param special True if the episode is a special, otherwise false.
	 */
	public void setSpecial(boolean special) {
		this.special = special;
	}

	/**
	 * This is the name of the special. Specials have a extra name used to ID them.
	 * @param specialName The name of the special.
	 */
	public void setSpecialName(String specialName) {
		this.specialName = specialName;
	}
	
	public void setTotalNumber(int number) {
		this.totalNum = number;
	}

	public int getTotalNumber() {
		return totalNum;
	}

	/**
	 * Get the name of the special.  Specials have a extra name used to ID them.
	 * @return The name of the special.
	 */
	public String getSpecialName() {
		return specialName;
	}

	/**
	 * Used to find out if this is a special
	 * @return True if special, otherwise false
	 */
	public boolean isSpecial() {
		return special;
	}

	/**
	 * The id of the as it's found on the sources size. Quite often these
	 * don't start from 1 for each season, but carry on counting.
	 * @param episodeSiteId The id of the episode as found on the site
	 */
	public void setSiteId(String episodeSiteId) {
		this.siteId = episodeSiteId;
	}
	
	/**
	 * Gets the episode id as it was on the sources site. Quite often these
	 * don't start from 1 for each season, but carry on counting.
	 * @return The episode site id
	 */
	public String getEpisodeSiteId() {
		return this.siteId;
	}
	
	/**
	 * Gets the first air date of the episode
	 * @return The first air date of the episode
	 */
	public Date getAirDate() {
		return airDate;
	}

	/**
	 * Gets the production code of the episode
	 * @return The production code of the episode
	 */
	public String getProductionCode() {
		return productionCode;
	}

	/**
	 * Used to set the production code of the episode
	 * @param productionCode The production code of the episode
	 */
	public void setProductionCode(String productionCode) {
		this.productionCode = productionCode;
	}

	/**
	 * Used to set the URL used to get a summary of the show
	 * @param url The summary URL
	 */
	public void setSummaryUrl(URL url) {
		summaryUrl = url;
	}
	
	/**
	 * Used to get the URL used to get a summary of the show
	 * @return The summary URL
	 */
	public URL getSummaryUrl() {
		return summaryUrl;
	}

	/**
	 * Used to get the numeric unique episode id used by the source
	 * @return the numeric unique episode id used by the source
	 */
	public long getEpisodeId() {
		return episodeId;
	}

	/**
	 * Used to set the numeric unique episode id used by the source
	 * @param episodeId The numeric unique episode id used by the source
	 */
	public void setEpisodeId(long episodeId) {
		this.episodeId = episodeId;
	}

	/**
	 * Used to get a list of guest stars with links to web sites about them
	 * @return A list of guest stars
	 */
	public List<Link> getGuestStars() {
		return guestStars;
	}

	/**
	 * Used to set a list of guest stars in the episode.
	 * @param guestStars The guest stars
	 */
	public void setGuestStars(List<Link> guestStars) {
		this.guestStars = guestStars;
	}

	/** 
	 * Used to get a list of directors for the episode
	 * @return A list of directors for the episode
	 */
	public List<Link> getDirectors() {
		return directors;
	}

	/**
	 * Used to set a list of directors for the episode
	 * @param directors The list of directors for the episode
	 */
	public void setDirectors(List<Link> directors) {
		this.directors = directors;
	}

	/**
	 * Used to get a list of writers for the episode
	 * @return Get a list of writers for the episode
	 */
	public List<Link> getWriters() {
		return writers;
	}

	/**
	 * Used to set a list of writers for the show
	 * @param writers The list of writers
	 */
	public void setWriters(List<Link> writers) {
		this.writers = writers;
	}

	/**
	 * Used to get the overall rating for the show
	 * @return The rating of the show
	 */
	public float getRating() {
		return rating;
	}

	/**
	 * Used to set the rating of the show 
	 * @param rating The rating of the show
	 */
	public void setRating(float rating) {
		this.rating = rating;
	}
}
