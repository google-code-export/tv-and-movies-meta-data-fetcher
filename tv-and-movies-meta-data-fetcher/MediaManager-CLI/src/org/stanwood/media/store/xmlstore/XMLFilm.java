/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
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
package org.stanwood.media.store.xmlstore;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.stanwood.media.model.Actor;
import org.stanwood.media.model.Certification;
import org.stanwood.media.model.Chapter;
import org.stanwood.media.model.IFilm;
import org.stanwood.media.model.Rating;
import org.stanwood.media.model.VideoFile;
import org.w3c.dom.Element;

public class XMLFilm implements IFilm {

	private Element node;

	private XMLFilm(Element node) {
		this.node = node;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public void setTitle(String title) {
	}

	@Override
	public List<String> getDirectors() {
		return null;
	}

	@Override
	public void setDirectors(List<String> directors) {
	}

	@Override
	public List<String> getWriters() {
		return null;
	}

	@Override
	public void setWriters(List<String> writers) {
	}

	@Override
	public String getSummary() {
		return null;
	}

	@Override
	public void setSummary(String summary) {
		// TODO Auto-generated method stub

	}

	@Override
	public SortedSet<VideoFile> getFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFiles(SortedSet<VideoFile> videoFiles) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Actor> getActors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActors(List<Actor> actors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGenres(List<String> genres) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getGenres() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addGenre(String genre) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPreferredGenre() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPreferredGenre(String preferredGenre) {
		// TODO Auto-generated method stub

	}

	@Override
	public Rating getRating() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRating(Rating rating) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSourceId(String sourceId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFilmUrl(URL url) {
		// TODO Auto-generated method stub

	}

	@Override
	public URL getFilmUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Certification> getCertifications() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCertifications(List<Certification> certifications) {
		// TODO Auto-generated method stub

	}

	@Override
	public Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDate(Date date) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setImageURL(URL imageURL) {
		// TODO Auto-generated method stub

	}

	@Override
	public URL getImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChapter(Chapter chapter) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Chapter> getChapters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setChapters(List<Chapter> chapters) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCountry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCountry(String country) {
		// TODO Auto-generated method stub

	}

}
