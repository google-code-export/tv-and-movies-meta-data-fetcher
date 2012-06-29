package org.stanwood.media.actions.seendb;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

/**
 * Used to store information on files that have been seen
 */
@Entity
@Table(name = "seen_files")
public class SeenEntry implements Serializable {

	private static final long serialVersionUID = 1687449172780260088L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	@Column(name = "filename",nullable= false,unique=true)	
	private String fileName;
	@Column(name = "lastModified",nullable= false)
	private long lastModified;

	/**
	 * Used to get the id of the Seen Entry, can be null if the seen database does not
	 * support it.
	 * @return The id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Used to set the if the Seen Entry, can be null if the seen database does not support it.
	 * @param id The id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Used to get the filename the seen file
	 * @return the filename the seen file
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Used to set the filename the seen file
	 * @param fileName the filename the seen file
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Used to get the date the file was last modified
	 * @return the date the file was last modified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * Used to set the date the file was last modified
	 * @param lastModified the date the file was last modified
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/** {@inhericDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
		return result;
	}

	/** {@inhericDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SeenEntry other = (SeenEntry) obj;
		if (fileName == null) {
			if (other.fileName != null) {
				return false;
			}
		} else if (!fileName.equals(other.fileName)) {
			return false;
		}
		if (lastModified != other.lastModified) {
			return false;
		}
		return true;
	}
}
