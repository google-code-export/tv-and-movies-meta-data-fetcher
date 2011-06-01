package org.stanwood.media.store.mp4.mp4v2.lib;

import com.sun.jna.Structure;

@SuppressWarnings("all")
public class MP4TagDisk extends Structure {
	public short index;
	public short total;
	public MP4TagDisk() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"index", "total"});
	}
	public MP4TagDisk(short index, short total) {
		super();
		this.index = index;
		this.total = total;
		initFieldOrder();
	}
	public static class ByReference extends MP4TagDisk implements Structure.ByReference {

	};
	public static class ByValue extends MP4TagDisk implements Structure.ByValue {

	};
}
