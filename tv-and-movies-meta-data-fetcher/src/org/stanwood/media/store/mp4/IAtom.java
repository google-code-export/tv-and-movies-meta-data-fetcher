package org.stanwood.media.store.mp4;

/**
 * Implementing classes are used to store and retrieve information about mp4 iso atom boxes
 */
public interface IAtom {

	/**
	 * Used to get the name of the atom
	 * @return The name of the atom
	 */
	public String getName();

	/**
	 * Used to set the name of the atom
	 * @param name The name of the atom
	 */
	public void setName(String name);

	/**
	 * Used to get the value of the atom
	 * @return The value of the atom
	 */
	public String getValue();

	/**
	 * Used to set the value of the atom
	 * @param value The value of the atom
	 */
	public void setValue(String value);

	/**
	 * Gets the display name
	 * @return the display name
	 */
	public String getDisplayName();

	/**
	 * Used to set the display name
	 * @param displayName The display name
	 */
	public void setDisplayName(String displayName);

}
