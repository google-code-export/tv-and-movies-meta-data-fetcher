package org.stanwood.media.search;

import java.io.File;

/**
 * Used to store test search details
 */
public class TSearchDetails {

	private File originalFile;
	private String term;
	private String year;
	private Integer part;

	/**
	 * The constructor
	 * @param originalFile The orinal file
	 * @param term The search term
	 * @param year the year or null if not found
	 * @param part The part or null if not found
	 */
	public TSearchDetails(File originalFile,String term,String year,Integer part) {
		this.originalFile = originalFile;
		this.term = term;
		this.year = year;
		this.part = part;
	}

	/**
	 * Used to get the original file
	 * @return the original file
	 */
	public File getOriginalFile() {
		return originalFile;
	}

	/**
	 * Used to get the search term
	 * @return The search term
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * Used to get the year
	 * @return the year or null if not found
	 */
	public String getYear() {
		return year;
	}

	/**
	 * Used to get the part
	 * @return the part or null if not found
	 */
	public Integer getPart() {
		return part;
	}
}