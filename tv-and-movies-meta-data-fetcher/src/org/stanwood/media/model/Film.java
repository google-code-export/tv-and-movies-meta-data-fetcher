package org.stanwood.media.model;

/**
 * This class is used to hold film related information
 */
public class Film {

	private long id;
	private String sourceId;
	private String title;	
	
	/**
	 * Used to get the id of the film used by the source that it was read from.
	 * @return The id of the film
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Used to set the id of the film used by the source that it was read from.
	 * @param id The id of the film
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	
	/**
	 * Used to get the source id of the source that was used to retrieve the film information.
	 * @return The source id
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Used to set the source id of the source that was used to retrieve the film information.
	 * @param sourceId The source id
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Used to get the film title.
	 * @return The film title.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Used to set the title of the film
	 * @param title The title of the film
	 */
	public void setTitle(String title) {
		this.title = title;
	}	
}
