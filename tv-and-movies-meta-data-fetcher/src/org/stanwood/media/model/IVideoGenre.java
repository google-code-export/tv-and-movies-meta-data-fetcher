package org.stanwood.media.model;

import java.util.List;

/**
 * This should be implemented by videos that support genres
 */
public interface IVideoGenre {


	/**
	 * Used to set the genres that the film belongs too
	 *
	 * @param genres The genres that the film belongs too
	 */
	public void setGenres(List<String> genres);

	/**
	 * Used to get the genres that the film belongs too
	 * @return The genres the film belongs too
	 */
	public List<String> getGenres();

	/**
	 * Used to add a genre to the film
	 * @param genre the genre to add
	 */
	public void addGenre(String genre);

	/**
	 * This is useful if the video belongs to more than one genres. It will returned the
	 * genre that is preferred.
	 * @return The preferred genre or null if not or flagged as preferred.
	 */
	public String getPreferredGenre();

	/**
	 * Used to set the genre that is preferred in the list of genres.
	 * @param preferredGenre The preferred genre
	 */
	public void setPreferredGenre(String preferredGenre);
}
