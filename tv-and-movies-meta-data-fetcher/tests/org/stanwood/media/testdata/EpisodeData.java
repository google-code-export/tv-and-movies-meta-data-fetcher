package org.stanwood.media.testdata;

import java.io.File;

import org.stanwood.media.model.Episode;

/**
 * This class is used to store a episode and it's file in a list.
 */
public class EpisodeData {

	private Episode episode;
	private File file;

	EpisodeData(Episode episode, File file) {
		this.episode = episode;
		this.file = file;
	}

	/**
	 * Used to get the episode
	 * @return The episode
	 */
	public Episode getEpisode() {
		return episode;
	}

	/**
	 * Used to get the file
	 * @return The file
	 */
	public File getFile() {
		return file;
	}


}
