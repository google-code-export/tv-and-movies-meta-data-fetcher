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
import java.util.List;

/**
 * This class is used to hold film related information
 */
public class Film {

	private long id;
	private String sourceId;
	private String title;
	private String summary;
	private List<Link>guestStars;
	private List<Link>directors;
	private List<Link>writers;
	private List<String> genres = new ArrayList<String>();
	private List<Certification> certifications = new ArrayList<Certification>();
	private URL filmUrl;
	
	/**
	 * Used to create a instance of the film class.
	 * @param id The id of the film used by the source that it was read from.
	 */
	public Film(long id) {
		setId(id);
	}
	
	/**
	 * Used to get the id of the film used by the source that it was read from.
	 * @return The id of the film
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Used to set the id of the film used by the source that it was read from.
	 * @param id The id of the film
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	
	/**
	 * Used to get the source id of the source that was used to retrieve the film information.
	 * @return The source id
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Used to set the source id of the source that was used to retrieve the film information.
	 * @param sourceId The source id
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Used to get the film title.
	 * @return The film title.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Used to set the title of the film
	 * @param title The title of the film
	 */
	public void setTitle(String title) {
		this.title = title;
	}	

	/**
	 * Used to get a list of guest stars with links to web sites about them
	 * @return A list of guest stars
	 */
	public List<Link> getGuestStars() {
		return guestStars;
	}

	/**
	 * Used to set a list of guest stars in the film.
	 * @param guestStars The guest stars
	 */
	public void setGuestStars(List<Link> guestStars) {
		this.guestStars = guestStars;
	}

	/** 
	 * Used to get a list of directors for the film
	 * @return A list of directors for the film
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
	 * Used to get a list of writers for the film
	 * @return Get a list of writers for the film
	 */
	public List<Link> getWriters() {
		return writers;
	}

	/**
	 * Used to set a list of writers for the film
	 * @param writers The list of writers
	 */
	public void setWriters(List<Link> writers) {
		this.writers = writers;
	}

	/**
	 * Used to get a summary of the film
	 * @return The summary of the film
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Used to set the films summary 
	 * @param summary The films summary
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	/**
	 * Used to set the URL used to get a summary of the film
	 * @param url The summary URL
	 */
	public void setFilmUrl(URL url) {
		filmUrl = url;
	}
	
	/**
	 * Used to get the URL used to get a summary of the film
	 * @return The summary URL
	 */
	public URL getFilmUrl() {
		return filmUrl;
	}
	
	/**
	 * Used to set the genres that the film belongs too
	 * 
	 * @param genres The genres that the film belongs too
	 */
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}

	/**
	 * Used to get the genres that the film belongs too
	 * @return The genres the film belongs too
	 */
	public List<String> getGenres() {
		return genres;
	}
	
	/**
	 * Used to add a genre to the film
	 * @param genre the genre to add
	 */
	public void addGenre(String genre) {
		genres.add(genre);
	}

	/**
	 * Used to get a list of the films certifications
	 * @return The films certification list
	 */
	public List<Certification> getCertifications() {
		return certifications;
	}

	/**
	 * Used to set the films certifications
	 * @param certifications The films certifications
	 */
	public void setCertifications(List<Certification> certifications) {
		this.certifications = certifications;
	}
	
	
}
