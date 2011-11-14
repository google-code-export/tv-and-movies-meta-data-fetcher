package org.stanwood.media.search;

/**
 * The search details returned by the different {@link ISearchStrategy} classes.
 */
public class SearchDetails {

	private String term;
	private String year;
	private Integer part;
	private Integer season;
	private Integer episode;

	/**
	 * The constructor
	 * @param term The search term
	 * @param year The year, or null if not found
	 * @param part The media part number, or null if not found
	 */
	public SearchDetails(String term, String year,Integer part) {
		super();
		this.term = term;
		this.year = year;
		this.part = part;
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
	 * @return The year, or null if not found
	 */
	public String getYear() {
		return year;
	}

	/**
	 * Used to get the part number
	 * @return The media part number, or null if not found
	 */
	public Integer getPart() {
		return part;
	}

	public Integer getSeason() {
		return season;
	}

	public void setSeason(Integer season) {
		this.season = season;
	}

	public Integer getEpisode() {
		return episode;
	}

	public void setEpisode(Integer episode) {
		this.episode = episode;
	}


}
