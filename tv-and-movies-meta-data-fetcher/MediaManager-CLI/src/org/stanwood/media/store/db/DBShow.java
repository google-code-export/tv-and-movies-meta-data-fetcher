package org.stanwood.media.store.db;

import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.Show;

public class DBShow extends Show {

	private DBMediaDirectory mediaDirectory;
	private List<DBSeason> seasons = new ArrayList<DBSeason>();
	private Long id;

	public DBShow() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DBMediaDirectory getMediaDirectory() {
		return mediaDirectory;
	}

	public void setMediaDirectory(DBMediaDirectory mediaDirectory) {
		this.mediaDirectory = mediaDirectory;
	}

	public List<DBSeason>getSeasons() {
		return seasons;
	}

	public void setSeasons(List<DBSeason> seasons) {
		this.seasons = seasons;
	}
}
