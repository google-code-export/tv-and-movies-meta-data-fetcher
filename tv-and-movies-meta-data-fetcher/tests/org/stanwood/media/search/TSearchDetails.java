package org.stanwood.media.search;

import java.io.File;

public class TSearchDetails {

	private File originalFile;
	private String term;
	private String year;

	public TSearchDetails(File originalFile,String term,String year) {
		this.originalFile = originalFile;
		this.term = term;
		this.year = year;
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


}