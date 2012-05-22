package org.stanwood.media.model;

import java.util.List;

/**
 * This should be implemented by video classes that can store actor information
 */
public interface IVideoActors {

	/**
	 * Used to get a list of actors in the video
	 * @return The list of actors in the video
	 */
	public List<Actor> getActors();

	/**
	 * Used to set the list of actors in the film
	 * @param actors The list of actors in the film
	 */
	public void setActors(List<Actor> actors);
}
