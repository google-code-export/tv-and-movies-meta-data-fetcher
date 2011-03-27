package org.stanwood.media.setup;

public class Plugin {

	private String jar;
	private String pluginClass;

	public Plugin(String jar, String clazz) {
		this.jar = jar;
		this.pluginClass = clazz;
	}

	public String getJar() {
		return jar;
	}

	public String getPluginClass() {
		return pluginClass;
	}


}
