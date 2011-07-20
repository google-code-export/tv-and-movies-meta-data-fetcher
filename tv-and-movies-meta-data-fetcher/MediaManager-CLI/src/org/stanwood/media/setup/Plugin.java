package org.stanwood.media.setup;

/**
 * Used to store information on plugins
 */
public class Plugin {

	private String jar;
	private String pluginClass;

	/**
	 * The constructor
	 * @param jar The jar that contains the plugin
	 * @param clazz The full class name within the jar
	 */
	public Plugin(String jar, String clazz) {
		this.jar = jar;
		this.pluginClass = clazz;
	}

	/**
	 * Used to get the jar name that holds the plugin
	 * @return The jar that contains the plugin
	 */
	public String getJar() {
		return jar;
	}

	/**
	 * Used to get the full class name within the jar
	 * @return The full class name within the jar
	 */
	public String getPluginClass() {
		return pluginClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return jar+":"+pluginClass; //$NON-NLS-1$
	}


}
