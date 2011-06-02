package org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric;

import com.sun.jna.Structure;

@SuppressWarnings("all")
public class MP4ItmfDataList extends Structure {
	/**
	 * < flat array. NULL when size is zero.<br>
	 * C type : MP4ItmfData*
	 */
	public MP4ItmfData.ByReference elements;
	/// < number of elements.
	public int size;
	public MP4ItmfDataList() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"elements", "size"});
	}
	/**
	 * @param elements < flat array. NULL when size is zero.<br>
	 * C type : MP4ItmfData*<br>
	 * @param size < number of elements.
	 */
	public MP4ItmfDataList(MP4ItmfData.ByReference elements, int size) {
		super();
		this.elements = elements;
		this.size = size;
		initFieldOrder();
	}
	public static class ByReference extends MP4ItmfDataList implements Structure.ByReference {

	};
	public static class ByValue extends MP4ItmfDataList implements Structure.ByValue {

	};
}
