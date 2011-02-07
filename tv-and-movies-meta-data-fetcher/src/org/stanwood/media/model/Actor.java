package org.stanwood.media.model;

/**
 * Used to store information on actors
 */
public class Actor {

	private String name;
	private String role;

	/**
	 * The constructor used to create a instance of the class
	 * @param name The name of the actor
	 * @param role The role the actor played
	 */
	public Actor(String name, String role) {
		super();
		this.name = name;
		this.role = role;
	}

	/**
	 * Used to get the name of the actor
	 * @return The name of the actor
	 */
	public String getName() {
		return name;
	}

	/**
	 * Used to get the role the actor played
	 * @return the role the actor played
	 */
	public String getRole() {
		return role;
	}

}
