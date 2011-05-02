package org.stanwood.media.logging;

public enum LogConfig {

	INFO("info.log4j.properties"),
	INFOEX("infoex.log4j.properties"),
	ERROR("error.log4j.properties"),
	ERROREX("errorex.log4j.properties"),
	DEBUG("debug.log4j.properties");

	private String filename;

	private LogConfig(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}
 }
