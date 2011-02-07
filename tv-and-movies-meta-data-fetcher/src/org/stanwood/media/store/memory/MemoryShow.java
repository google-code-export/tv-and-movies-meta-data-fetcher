package org.stanwood.media.store.memory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.stanwood.media.model.Season;
import org.stanwood.media.model.Show;

public class MemoryShow extends Show {

	private List<Season> seasons = new ArrayList<Season>();
	private Show show;

	public MemoryShow(Show show) {
		super(show.getShowId());
		this.show = show;
	}

	/**
	 * Used to get a season from the show with the given season number.
	 * If the season can't be found, then it will return null.
	 * @param seasonNum The number of the season too fetch.
	 * @return The season, or null if it can't be found
	 */
	public Season getSeason(int seasonNum) {
		for (Season season : seasons) {
			if (season.getSeasonNumber() == seasonNum) {
				return season;
			}
		}
		return null;
	}

	/**
	 * Used to remove a season with the given season number from the show.
	 * @param seasonNumber The season number of the season to remove
	 */
	public void removeSeason(int seasonNumber) {
		Iterator<Season> it = seasons.iterator();
		while (it.hasNext()) {
			Season foundSeason = it.next();
			if (foundSeason.getSeasonNumber() == seasonNumber) {
				it.remove();
			}
		}
	}

	/**
	 * Used to add a season to the show.
	 * @param season The season to add to the show.
	 */
	public void addSeason(Season season) {
		seasons.add(season);
	}

	@Override
	public void setLongSummary(String longSummary) {
		show.setLongSummary(longSummary);
	}

	@Override
	public void setShortSummary(String shortSummary) {
		show.setShortSummary(shortSummary);
	}

	@Override
	public void setGenres(List<String> genres) {
		show.setGenres(genres);
	}

	@Override
	public void setName(String name) {
		show.setName(name);
	}

	@Override
	public void setShowURL(URL showURL) {
		show.setShowURL(showURL);
	}

	@Override
	public String getLongSummary() {
		return show.getLongSummary();
	}

	@Override
	public String getShortSummary() {
		return show.getShortSummary();
	}

	@Override
	public List<String> getGenres() {
		return show.getGenres();
	}

	@Override
	public String getName() {
		return show.getName();
	}

	@Override
	public String getShowId() {
		return show.getShowId();
	}

	@Override
	public URL getImageURL() {
		return show.getImageURL();
	}

	@Override
	public void setImageURL(URL imageURL) {
		show.setImageURL(imageURL);
	}

	@Override
	public URL getShowURL() {
		return show.getShowURL();
	}

	@Override
	public String getSourceId() {
		return show.getSourceId();
	}

	@Override
	public void setSourceId(String sourceId) {
		show.setSourceId(sourceId);
	}

	@Override
	public void addGenre(String genre) {
		show.addGenre(genre);
	}

	@Override
	public String getPreferredGenre() {
		return show.getPreferredGenre();
	}

	@Override
	public void setPreferredGenre(String preferredGenre) {
		show.setPreferredGenre(preferredGenre);
	}

	@Override
	public Map<String, String> getExtraInfo() {
		return show.getExtraInfo();
	}

	@Override
	public void setExtraInfo(Map<String, String> params) {
		show.setExtraInfo(params);
	}


}
