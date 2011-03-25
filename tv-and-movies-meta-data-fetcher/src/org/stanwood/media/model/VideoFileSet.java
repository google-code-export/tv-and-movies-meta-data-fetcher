package org.stanwood.media.model;

import java.util.Comparator;
import java.util.TreeSet;

public class VideoFileSet extends TreeSet<VideoFile> {

	public VideoFileSet() {
		super(new Comparator<VideoFile>() {
			@Override
			public int compare(VideoFile o1, VideoFile o2) {
				return o1.getLocation().compareTo(o2.getLocation());
			}
		});
	}

}
