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
import java.util.SortedSet;

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

	private URL url;

	/** The id of the show as found on the source site */
	private String episodeId;

	private List<Actor> actors;
	private List<String>directors;
	private List<String>writers;

	private Rating rating;

	private URL imageURL;

	private SortedSet<VideoFile>videoFiles = new VideoFileSet();

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
	@Override
	public String getSummary() {
		return summary;
	}

	/**
	 * Sets the summary of the episode
	 * @param summary The summary of the episode
	 */
	@Override
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Sets the title of the episode
	 * @param title The title of the episode
	 */
	@Override
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
	@Override
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
	 * Gets the first air date of the episode
	 * @return The first air date of the episode
	 */
	public Date getDate() {
		if (airDate==null) {
			return null;
		}
		return new Date(airDate.getTime());
	}


	/**
	 * Used to set the URL used to get a summary of the show
	 * @param url The summary URL
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * Used to get the URL used to get a summary of the show
	 * @return The summary URL
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Used to get the numeric unique episode id used by the source
	 * @return the numeric unique episode id used by the source
	 */
	public String getEpisodeId() {
		return episodeId;
	}

	/**
	 * Used to set the numeric unique episode id used by the source
	 * @param episodeId The numeric unique episode id used by the source
	 */
	public void setEpisodeId(String episodeId) {
		this.episodeId = episodeId;
	}

	/**
	 * Used to get a list of actors in the episode
	 * @return a list of actors in the episode
	 */
	@Override
	public List<Actor> getActors() {
		return actors;
	}

	/**
	 * Used to set a list of actors in the episode
	 * @param actors A list of actors in the episode
	 */
	@Override
	public void setActors(List<Actor> actors) {
		this.actors = actors;
	}

	/**
	 * Used to get a list of directors for the episode
	 * @return A list of directors for the episode
	 */
	@Override
	public List<String> getDirectors() {
		return directors;
	}

	/**
	 * Used to set a list of directors for the episode
	 * @param directors The list of directors for the episode
	 */
	@Override
	public void setDirectors(List<String> directors) {
		this.directors = directors;
	}

	/**
	 * Used to get a list of writers for the episode
	 * @return Get a list of writers for the episode
	 */
	@Override
	public List<String> getWriters() {
		return writers;
	}

	/**
	 * Used to set a list of writers for the episode
	 * @param writers The list of writers
	 */
	@Override
	public void setWriters(List<String> writers) {
		this.writers = writers;
	}

	/**
	 * Used to get the episode rating
	 * @return the episode rating
	 */
	@Override
	public Rating getRating() {
		return rating;
	}

	/**
	 * Used to set the episode rating
	 * @param rating The episode rating
	 */
	@Override
	public void setRating(Rating rating) {
		this.rating = rating;
	}

	/**
	 * Used to get a URL which points to a image of the episode
	 * @return A URL which points too a image of the episode
	 */
	public URL getImageURL() {
		return imageURL;
	}

	/**
	 * Used to set a URL which points too a image of the episode
	 * @param imageURL A URL which points too a image of the episode
	 */
	public void setImageURL(URL imageURL) {
		this.imageURL = imageURL;
	}

	/**
	 * Returns a string summary of this classes contents. Mainly intended for use when debugging.
	 * @return string summary of this classes contents
	 */
	@Override
	public String toString() {
		return "Episode - ID:" + getEpisodeId() +" Num: " + getEpisodeNumber()+" Title: " + getTitle();
	}

	/** {@inheritDoc} */
	@Override
	public SortedSet<VideoFile> getFiles() {
		return videoFiles;
	}

	/** {@inheritDoc} */
	@Override
	public void setFiles(SortedSet<VideoFile> videoFiles) {
		this.videoFiles = videoFiles;
	}


}
