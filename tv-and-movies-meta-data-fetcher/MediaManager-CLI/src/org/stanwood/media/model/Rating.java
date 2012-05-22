package org.stanwood.media.model;

import java.io.Serializable;

/**
 * Used to store information on video ratings
 */
public class Rating implements Serializable {

	private float rating;
	private int numberOfVotes;

	/**
	 * The constructor used to create a instance of the rating class
	 * @param rating The value of the rating
	 * @param numberOfVotes the number of votes the thing been rated had
	 */
	public Rating(float rating, int numberOfVotes) {
		super();
		this.rating = rating;
		this.numberOfVotes = numberOfVotes;
	}

	/**
	 * Used to get the rating value
	 * @return the rating value
	 */
	public float getRating() {
		return rating;
	}

	/**
	 * Used to get the number of votes the thing been rated had
	 * @return the number of votes the thing been rated had
	 */
	public int getNumberOfVotes() {
		return numberOfVotes;
	}



}
