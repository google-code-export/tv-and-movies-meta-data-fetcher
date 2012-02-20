package org.stanwood.media.store.mp4.itunes;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message bundle class
 */
public class Messages {
	private static final String BUNDLE_NAME = "org.stanwood.media.store.itunes.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	/**
	 * Used to get the message
	 * @param key The message key
	 * @return The message text
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
