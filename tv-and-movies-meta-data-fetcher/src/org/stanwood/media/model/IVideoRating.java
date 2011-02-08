package org.stanwood.media.model;

/**
 * This interface should be implemented by video classes that can be rated
 */
public interface IVideoRating {

	/**
	 * Used to get the rating of the video
	 * @return The rating of the video
	 */
	public Rating getRating();

	/**
	 * Used to set the rating of the video
	 * @param rating The rating
	 */
	public void setRating(Rating rating);
}
