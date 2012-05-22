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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/**
 * This class is used to hold film related information
 */
public class Film implements IFilm {

	private String id;
	private String sourceId;
	private String title;
	private String summary;
	private List<String>directors;
	private List<String>writers;
	private List<String> genres = new ArrayList<String>();
	private List<Certification> certifications = new ArrayList<Certification>();
	private URL filmUrl;
	private Rating rating;
	private Date date;
	private URL imageURL;
	private List<Chapter> chapters = new ArrayList<Chapter>();
	private String description;
	private String preferredGenre;
	private String country;
	private List<? extends Actor> actors;
	private SortedSet<IVideoFile> videoFiles = new VideoFileSet();
	private String studio;

	/**
	 * Used to create a film
	 */
	public Film() {

	}

	/**
	 * Used to create a instance of the film class.
	 * @param id The id of the film used by the source that it was read from.
	 */
	public Film(String id) {
		setId(id);
	}

	/**
	 * This is useful if the film belongs to more than one genres. It will returned the
	 * genre that is preferred.
	 * @return The preferred genre or null if not or flagged as preferred.
	 */
	@Override
	public String getPreferredGenre() {
		if (preferredGenre==null && genres.size()>0) {
			return genres.get(0);
		}
		return preferredGenre;
	}

	/**
	 * Used to set the genre that is preferred in the list of genres.
	 * @param preferredGenre The preferred genre
	 */
	@Override
	public void setPreferredGenre(String preferredGenre) {
		this.preferredGenre = preferredGenre;
	}

	/**
	 * Used to get the id of the film used by the source that it was read from.
	 * @return The id of the film
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Used to set the id of the film used by the source that it was read from.
	 * @param id The id of the film
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * Used to get the source id of the source that was used to retrieve the film information.
	 * @return The source id
	 */
	@Override
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Used to set the source id of the source that was used to retrieve the film information.
	 * @param sourceId The source id
	 */
	@Override
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Used to get the film title.
	 * @return The film title.
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * Used to set the title of the film
	 * @param title The title of the film
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Used to get a list of actors in the video
	 * @return The list of actors in the video
	 */
	@Override
	public List<? extends Actor> getActors() {
		return actors;
	}

	/**
	 * Used to set the list of actors in the film
	 * @param actors The list of actors in the film
	 */
	@Override
	public void setActors(List<? extends Actor> actors) {
		this.actors = actors;
	}

	/**
	 * Used to get a list of directors for the film
	 * @return A list of directors for the film
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
	 * Used to get a list of writers for the film
	 * @return Get a list of writers for the film
	 */
	@Override
	public List<String> getWriters() {
		return writers;
	}

	/**
	 * Used to set a list of writers for the film
	 * @param writers The list of writers
	 */
	@Override
	public void setWriters(List<String> writers) {
		this.writers = writers;
	}

	/**
	 * Used to get a summary of the film
	 * @return The summary of the film
	 */
	@Override
	public String getSummary() {
		return summary;
	}

	/**
	 * Used to set the films summary
	 * @param summary The films summary
	 */
	@Override
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Used to set the URL used to get a summary of the film
	 * @param url The summary URL
	 */
	@Override
	public void setFilmUrl(URL url) {
		filmUrl = url;
	}

	/**
	 * Used to get the URL used to get a summary of the film
	 * @return The summary URL
	 */
	@Override
	public URL getFilmUrl() {
		return filmUrl;
	}

	/**
	 * Used to set the genres that the film belongs too
	 *
	 * @param genres The genres that the film belongs too
	 */
	@Override
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}

	/**
	 * Used to get the genres that the film belongs too
	 * @return The genres the film belongs too
	 */
	@Override
	public List<String> getGenres() {
		return genres;
	}

	/**
	 * Used to add a genre to the film
	 * @param genre the genre to add
	 */
	@Override
	public void addGenre(String genre) {
		genres.add(genre);
		Collections.sort(genres);
	}

	/**
	 * Used to get a list of the films certifications
	 * @return The films certification list
	 */
	@Override
	public List<Certification> getCertifications() {
		return certifications;
	}

	/**
	 * Used to set the films certifications
	 * @param certifications The films certifications
	 */
	@Override
	public void setCertifications(List<Certification> certifications) {
		this.certifications = certifications;
	}

	/**
	 * Used to get the global user rating of the film
	 * @return The global user rating of the show
	 */
	@Override
	public Rating getRating() {
		return rating;
	}

	/**
	 * Used to set the global user rating of the film
	 * @param rating The global user rating of the show
	 */
	@Override
	public void setRating(Rating rating) {
		this.rating = rating;
	}

	/**
	 * Used to get the release date of the film
	 * @return The release date of the film
	 */
	@Override
	public Date getDate() {
		if (date==null) {
			return null;
		}
		else {
			return new Date(date.getTime());
		}
	}

	/**
	 * Used to set the release date of the film
	 * @param date The release date of the film
	 */
	@Override
	public void setDate(Date date) {
		if (date!=null) {
			this.date = new Date(date.getTime());
		}
		else {
			date = null;
		}
	}

	/**
	 * Used to set the URL of the film poster
	 * @param imageURL The URL of the film poster.
	 */
	@Override
	public void setImageURL(URL imageURL) {
		this.imageURL = imageURL;
	}

	/**
	 * Used to get the URL of the film poster. This will return null if
	 * a poster could not be found.
	 * @return The film poster, or null if it does not have one
	 */
	@Override
	public URL getImageURL() {
		return this.imageURL;
	}

	/**
	 * Used to add a chapter to the film
	 * @param chapter The chapter to add
	 */
	@Override
	public void addChapter(Chapter chapter) {
		Iterator<Chapter> it = chapters.iterator();
		while (it.hasNext()) {
			Chapter chap = it.next();
			if (chap.getNumber() == chapter.getNumber()) {
				it.remove();
			}
		}
		chapters.add(chapter);


		Collections.sort(chapters,new Comparator<Chapter>() {
			@Override
			public int compare(Chapter o1, Chapter o2) {
				return Integer.valueOf(o1.getNumber()).compareTo(o2.getNumber());
			}
		});
	}

	/**
	 * Used to get the chapters of the film
	 * @return The chapters of the film
	 */
	@Override
	public List<Chapter> getChapters() {
		return chapters;
	}

	/**
	 * Used to set the chapter information for the film
	 * @param chapters The chapters of the film
	 */
	@Override
	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

	/**
	 * Used to set the films long description
	 * @param description The films long description
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Used to get the films long description
	 * @return the films long description
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * This will return the country the film was made in, or
	 * null if it's not known.
	 * @return the country the film was made in.
	 */
	@Override
	public String getCountry() {
		return country;
	}

	/**
	 * Used to set the country the film was made in.
	 * @param country the country to set
	 */
	@Override
	public void setCountry(String country) {
		this.country = country;
	}

	/** {@inheritDoc} */
	@Override
	public SortedSet<IVideoFile> getFiles() {
		return videoFiles;
	}

	/** {@inheritDoc} */
	@Override
	public void setFiles(Collection<IVideoFile> videoFiles) {
		this.videoFiles.clear();
		this.videoFiles.addAll(videoFiles);
	}

	/** {@inheritDoc} */
	@Override
	public void setStudio(String studio) {
		this.studio = studio;
	}

	/** {@inheritDoc} */
	@Override
	public String getStudio() {
		return studio;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Film ID: " + getId()+" - Source: " + getSourceId()+" - Title: "+getTitle(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
