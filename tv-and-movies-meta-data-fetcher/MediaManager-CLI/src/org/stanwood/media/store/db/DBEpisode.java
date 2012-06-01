package org.stanwood.media.store.db;

import org.stanwood.media.model.Episode;

public class DBEpisode extends Episode {

	private Long id;

	public DBEpisode() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
