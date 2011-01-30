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
public class Episode implements IVideo,IVideoActors,IVideoRating {

	private Season season;

	/** The number of the episode within the season */
	private int episodeNumber;

	private String summary;
	private String title;
	private Date airDate;
	private boolean special;

	private URL summaryUrl;

	/** The id of the show as found on the source site */
	private long episodeId;

	/** Episode number counting since the show started */
	private long showEpisodeNumber;

	private List<Actor> actors;
	private List<String>directors;
	private List<String>writers;

	private Rating rating;

	/**
	 * The constructor used to create a episode instance
	 * @param episodeNumber The number of the episode within the season
	 * @param season The season it belongs too
	 */
	public Episode(int episodeNumber, Season season) {
		setEpisodeNumber(episodeNumber);
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
	 * Gets the number of the episode.
	 * @return The number of the episode
	 */
	public int getEpisodeNumber() {
		return episodeNumber;
	}

	/**
	 * Sets the number of the episode.
	 * @param episodeNumner
	 */
	public void setEpisodeNumber(int episodeNumner) {
		this.episodeNumber = episodeNumner;
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
	public void setDate(Date airDate) {
		this.airDate = new Date(airDate.getTime());
	}

	/**
	 * Gets the title of the show
	 * @return The title of the show
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
	 * Used to find out if this is a special
	 * @return True if special, otherwise false
	 */
	public boolean isSpecial() {
		return special;
	}

	/**
	 * Sets the episode count since the show started
	 * @param showEpisodeNumber The episode number since the show started
	 */
	public void setShowEpisodeNumber(long showEpisodeNumber) {
		this.showEpisodeNumber = showEpisodeNumber;
	}

	/**
	 * Gets the episode number since the show started
	 * @return The episode number since the show started
	 */
	public long getShowEpisodeNumber() {
		return this.showEpisodeNumber;
	}

	/**
	 * Gets the first air date of the episode
	 * @return The first air date of the episode
	 */
	public Date getDate() {
		return new Date(airDate.getTime());
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

	@Override
	public List<Actor> getActors() {
		return actors;
	}

	@Override
	public void setActors(List<Actor> actors) {
		this.actors = actors;
	}

	/**
	 * Used to get a list of directors for the episode
	 * @return A list of directors for the episode
	 */
	public List<String> getDirectors() {
		return directors;
	}

	/**
	 * Used to set a list of directors for the episode
	 * @param directors The list of directors for the episode
	 */
	public void setDirectors(List<String> directors) {
		this.directors = directors;
	}

	/**
	 * Used to get a list of writers for the episode
	 * @return Get a list of writers for the episode
	 */
	public List<String> getWriters() {
		return writers;
	}

	/**
	 * Used to set a list of writers for the episode
	 * @param writers The list of writers
	 */
	public void setWriters(List<String> writers) {
		this.writers = writers;
	}

	@Override
	public Rating getRating() {
		return rating;
	}

	@Override
	public void setRating(Rating rating) {
		this.rating = rating;
	}

}
