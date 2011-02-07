package org.stanwood.media.model;

/**
 * Used to store information on video ratings
 */
public class Rating {

	private float rating;
	private int numberOfVotes;

	/**
	 * The constructor used to create a instance of the rating class
	 * @param rating The value of the rating
	 * @param numberOfVotes The number of votes the rating got
	 */
	public Rating(float rating, int numberOfVotes) {
		super();
		this.rating = rating;
		this.numberOfVotes = numberOfVotes;
	}

	public float getRating() {
		return rating;
	}

	public int getNumberOfVotes() {
		return numberOfVotes;
	}



}
