package org.stanwood.media.store.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.stanwood.media.model.Film;
import org.stanwood.media.store.StoreVersion;
import org.stanwood.media.util.Version;

/**
 * Used to store media directory information within the database
 */
@Entity
@Table(name = "media_dir")
public class DBMediaDirectory implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	@Column(name = "location")
	private String location;
	@Column(name = "version")
	@Type(type= "org.stanwood.media.store.db.VersionType")
	private Version version;
	@Column(name = "revision")
	private int revision;

	@OneToMany(cascade={CascadeType.ALL})
	private List<Film>films = new ArrayList<Film>();
	@OneToMany(cascade={CascadeType.ALL})
	private List<DBShow>shows = new ArrayList<DBShow>();

	/**
	 * Used to get the database ID of the media directory
	 * @return the database ID of the media directory
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Used to set the database ID of the media directory
	 * @param id the database ID of the media directory
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Used to get the media directory location
	 * @return the media directory location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Used to set the media directory location
	 * @param location the media directory location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Used to get a list of films in the media directory
	 * @return a list of films in the media directory
	 */
	public List<Film> getFilms() {
		return films;
	}

	/**
	 * Used to set a list of films in the media directory
	 * @param films a list of films in the media directory
	 */
	public void setFilms(List<Film> films) {
		this.films = films;
	}

	/**
	 * Used to get a list of shows in the media directory
	 * @return a list of shows in the media directory
	 */
	public List<DBShow> getShows() {
		return shows;
	}

	/**
	 * Used to set a list of shows in the media directory
	 * @param shows a list of shows in the media directory
	 */
	public void setShows(List<DBShow> shows) {
		this.shows = shows;
	}

	/**
	 * Used to get the store version for the media directory
	 * @return the store version for the media directory
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Used to set the store version for the media directory
	 * @param version the store version for the media directory
	 */
	public void setVersion(Version version) {
		this.version = version;
	}

	/**
	 * Used to get the store revision for the media directory
	 * @return the store revision for the media directory
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Used to get the store revision for the media directory
	 * @param revision the store revision for the media directory
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}

	/**
	 * Used to get the store version
	 * @return the store version
	 */
	public StoreVersion getStoreVersion() {
		return new StoreVersion(getVersion(),getRevision());
	}
}
