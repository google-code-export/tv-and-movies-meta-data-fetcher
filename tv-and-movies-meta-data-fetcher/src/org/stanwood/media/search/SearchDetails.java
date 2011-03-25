package org.stanwood.media.search;

public class SearchDetails {

	private String term;
	private String year;
	private Integer part;

	public SearchDetails(String term, String year,Integer part) {
		super();
		this.term = term;
		this.year = year;
		this.part = part;
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
