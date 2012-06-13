package org.stanwood.media.store.db;

import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.Season;

/**
 * Used to store season information in a database
 */
public class DBSeason extends Season {

	private Long id;
	private List<DBEpisode>episodes = new ArrayList<DBEpisode>();

	/**
	 * The constructor
	 */
	public DBSeason() {

	}

	/**
	 * Used to get the database ID of the season
	 * @return the database ID of the season
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Used to set the database ID of the season
	 * @param id the database ID of the season
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Used to get a list of episodes in the season
	 * @return A list of episodes in the season
	 */
	public List<DBEpisode> getEpisodes() {
		return episodes;
	}

	/**
	 * Used to set the list of episodes in the season
	 * @param episodes the list of episodes in the season
	 */
	public void setEpisodes(List<DBEpisode> episodes) {
		this.episodes = episodes;
	}
}
