package org.stanwood.media.store.mp4.taglib.jna;

/**
 * Should be extended by JNA enums to make a typesafe mapping to C enums
 * @param <T> The enum class
 */
public interface JnaEnum<T> {
	/** get the value of the enum
	 * @return the value*/
	public int getIntValue();
	/**
	 * Get the enum from the value
	 * @param i The value
	 * @return The enum
	 */
    public T getForValue(int i);
}
