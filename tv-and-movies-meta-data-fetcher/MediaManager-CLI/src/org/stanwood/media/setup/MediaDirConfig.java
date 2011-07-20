package org.stanwood.media.setup;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.stanwood.media.model.Mode;

/**
 * Used to store media directory configuration information
 */
public class MediaDirConfig {

	private File mediaDir;
	private String pattern;
	private Mode mode;
	private List<StoreConfig>stores;
	private List<SourceConfig>sources;
	private List<ActionConfig>actions;
	private List<String> extensions;
	private List<Pattern> ignorePatterns;
	private boolean ignoreSeen;

	/**
	 * Used to get the media directory location
	 * @return the media directory location
	 */
	public File getMediaDir() {
		return mediaDir;
	}

	/**
	 * Used to set the media directory location
	 * @param mediaDir the media directory location
	 */
	public void setMediaDir(File mediaDir) {
		this.mediaDir = mediaDir;
	}

	/**
	 * Used to get the media directory rename pattern
	 * @return the media directory rename pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Used to set the media directory rename pattern
	 * @param pattern the media directory rename pattern
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Used to get the mode of the media directory
	 * @return the mode of the media directory
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * Used to set the mode of the media directory
	 * @param mode the mode of the media directory
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	/**
	 * Used to get a list of stores used by the media directory
	 * @return a list of stores used by the media directory
	 */
	public List<StoreConfig> getStores() {
		return stores;
	}

	/**
	 * Used to set a list of stores used by the media directory
	 * @param stores a list of stores used by the media directory
	 */
	public void setStores(List<StoreConfig> stores) {
		this.stores = stores;
	}

	/**
	 * Used to get a list of sources used by the media directory
	 * @return a list of sources used by the media directory
	 */
	public List<SourceConfig> getSources() {
		return sources;
	}

	/**
	 * Used to set a list of sources used by the media directory
	 * @param sources a list of sources used by the media directory
	 */
	public void setSources(List<SourceConfig> sources) {
		this.sources = sources;
	}

	/**
	 * Used to get a list of actions to be performed on the media directory
	 * @return a list of actions to be performed on the media directory
	 */
	public List<ActionConfig> getActions() {
		return actions;
	}

	/**
	 * Used to set a list of actions to be performed on the media directory
	 * @param actions a list of actions to be performed on the media directory
	 */
	public void setActions(List<ActionConfig> actions) {
		this.actions = actions;
	}

	/**
	 * Used to set a list of valid extensions within the media directory
	 * @param exts A list of valid extensions within the media directory
	 */
	public void setExtensions(List<String> exts) {
		extensions = exts;
	}

	/**
	 * Used to get a list of valid extensions within the media directory
	 * @return a list of valid extensions within the media directory
	 */
	public List<String>getExtensions() {
		return extensions;
	}

	/**
	 * Used to set a list of patterns that will be tested against files
	 *  in  a media directory to work out if they should be ignored.
	 * @param patterns The list of patterns
	 */
	public void setIgnorePatterns(List<Pattern> patterns) {
		this.ignorePatterns = patterns;
	}

	/**
	 * Used to get a list of patterns that will be tested against files
	 *  in  a media directory to work out if they should be ignored.
	 * @return The list of patterns
	 */
	public List<Pattern> getIgnorePatterns() {
		return ignorePatterns;
	}

	/**
	 * Should seen files be ignored in the media directory
	 * @return true if seen files should be ignored
	 */
	public boolean getIgnoreSeen() {
		return ignoreSeen;
	}

	/**
	 * Used to set if seen files be ignored in the media directory
	 * @param ignoreSeen true if seen files should be ignored
	 */
	public void setIgnoreSeen(boolean ignoreSeen) {
		this.ignoreSeen = ignoreSeen;
	}


}
