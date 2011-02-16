package org.stanwood.media.search;

public class SearchDetails {

	private String term;
	private String year;

	public SearchDetails(String term, String year) {
		super();
		this.term = term;
		this.year = year;
	}

	public String getTerm() {
		return term;
	}

	public String getYear() {
		return year;
	}

}
