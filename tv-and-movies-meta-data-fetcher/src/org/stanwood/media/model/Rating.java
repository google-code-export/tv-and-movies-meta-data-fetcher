package org.stanwood.media.model;

public class Rating {

	private float rating;
	private int numberOfVotes;

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
