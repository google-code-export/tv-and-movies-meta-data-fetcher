package org.stanwood.media.model;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * Used to store video files in a sorted set
 */
public class VideoFileSet extends TreeSet<IVideoFile> {

	private static final long serialVersionUID = -2055733997633542890L;

	/**
	 * The constructor
	 */
	public VideoFileSet() {
		super(new Comparator<IVideoFile>() {
			@Override
			public int compare(IVideoFile o1, IVideoFile o2) {
				return o1.getLocation().compareTo(o2.getLocation());
			}
		});
	}

}
