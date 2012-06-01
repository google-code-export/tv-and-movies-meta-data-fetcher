package org.stanwood.media.store.db;

import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.Season;

public class DBSeason extends Season {

	private Long id;
	private List<DBEpisode>episodes = new ArrayList<DBEpisode>();

	public DBSeason() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<DBEpisode> getEpisodes() {
		return episodes;
	}

	public void setEpisodes(List<DBEpisode> episodes) {
		this.episodes = episodes;
	}
}
