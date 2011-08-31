package org.stanwood.media.model;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Used to store video files in a sorted set
 */
public class VideoFileSet extends TreeSet<VideoFile> {

	private static final long serialVersionUID = -2055733997633542890L;

	/**
	 * The constructor
	 */
	public VideoFileSet() {
		super(new Comparator<VideoFile>() {
			@Override
			public int compare(VideoFile o1, VideoFile o2) {
				return o1.getLocation().compareTo(o2.getLocation());
			}
		});
	}

}