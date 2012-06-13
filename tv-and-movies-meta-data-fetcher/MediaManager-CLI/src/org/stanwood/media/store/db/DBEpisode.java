package org.stanwood.media.store.db;

import org.stanwood.media.model.Episode;

/**
 * Used to store database episode information
 */
public class DBEpisode extends Episode {

	private Long id;

	/** The constructor */
	public DBEpisode() {

	}

	/**
	 * Used to get the database ID for the episode
	 * @return the database ID for the episode
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Used to set the database ID for the episode
	 * @param id the database ID for the episode
	 */
	public void setId(Long id) {
		this.id = id;
	}

}
