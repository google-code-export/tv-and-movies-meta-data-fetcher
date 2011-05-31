package org.stanwood.media.store.mp4.taglib;

import com.sun.jna.Structure;

public class MP4TagTrack extends Structure {
	public short index;
	public short total;
	public MP4TagTrack() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"index", "total"});
	}
	public MP4TagTrack(short index, short total) {
		super();
		this.index = index;
		this.total = total;
		initFieldOrder();
	}
	public static class ByReference extends MP4TagTrack implements Structure.ByReference {

	};
	public static class ByValue extends MP4TagTrack implements Structure.ByValue {

	};
}
