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

import org.stanwood.media.model.Film;
import org.stanwood.media.store.StoreVersion;
import org.stanwood.media.util.Version;

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
	private Version version;
	@Column(name = "revision")
	private int revision;

	@OneToMany(cascade={CascadeType.ALL})
	private List<Film>films = new ArrayList<Film>();
	@OneToMany(cascade={CascadeType.ALL})
	private List<DBShow>shows = new ArrayList<DBShow>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<Film> getFilms() {
		return films;
	}

	public void setFilms(List<Film> films) {
		this.films = films;
	}

	public List<DBShow> getShows() {
		return shows;
	}

	public void setShows(List<DBShow> shows) {
		this.shows = shows;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public StoreVersion getStoreVersion() {
		return new StoreVersion(getVersion(),getRevision());
	}
}
