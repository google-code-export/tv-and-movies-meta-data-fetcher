package org.stanwood.media.store.db;

import java.util.ArrayList;
import java.util.List;

import org.stanwood.media.model.Show;

/**
 * Used to store show information in a database
 */
public class DBShow extends Show {

	private DBMediaDirectory mediaDirectory;
	private List<DBSeason> seasons = new ArrayList<DBSeason>();
	private Long id;

	/** The constructor */
	public DBShow() {

	}

	/**
	 * Used to get the database ID of the show
	 * @return the database ID of the show
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Used to set the database ID of the show
	 * @param id the database ID of the show
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Used to get the media directory that the show belongs to
	 * @return the media directory that the show belongs to
	 */
	public DBMediaDirectory getMediaDirectory() {
		return mediaDirectory;
	}

	/**
	 * Used to set the media directory that the show belongs to
	 * @param mediaDirectory the media directory that the show belongs to
	 */
	public void setMediaDirectory(DBMediaDirectory mediaDirectory) {
		this.mediaDirectory = mediaDirectory;
	}

	/**
	 * Used to get a list of seasons within the show
	 * @return a list of seasons within the show
	 */
	public List<DBSeason>getSeasons() {
		return seasons;
	}

	/**
	 * Used to set a list of seasons within the show
	 * @param seasons a list of seasons within the show
	 */
	public void setSeasons(List<DBSeason> seasons) {
		this.seasons = seasons;
	}
}
