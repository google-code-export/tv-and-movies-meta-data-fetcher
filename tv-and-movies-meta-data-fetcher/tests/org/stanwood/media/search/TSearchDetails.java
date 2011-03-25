package org.stanwood.media.search;

import java.io.File;

public class TSearchDetails {

	private File originalFile;
	private String term;
	private String year;
	private Integer part;

	public TSearchDetails(File originalFile,String term,String year,Integer part) {
		this.originalFile = originalFile;
		this.term = term;
		this.year = year;
		this.part = part;
	}

	public File getOriginalFile() {
		return originalFile;
	}

	public String getTerm() {
		return term;
	}

	public String getYear() {
		return year;
	}

	public Integer getPart() {
		return part;
	}
}