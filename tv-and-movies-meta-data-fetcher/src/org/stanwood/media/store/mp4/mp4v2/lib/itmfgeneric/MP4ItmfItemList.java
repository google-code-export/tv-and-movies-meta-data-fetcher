package org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric;

import com.sun.jna.Structure;

@SuppressWarnings("all")
public class MP4ItmfItemList extends Structure {
	/**
	 * < flat array. NULL when size is zero.<br>
	 * C type : MP4ItmfItem*
	 */
	public MP4ItmfItem.ByReference elements;
	/// < number of elements.
	public int size;
	public MP4ItmfItemList() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"elements", "size"});
	}
	/**
	 * @param elements < flat array. NULL when size is zero.<br>
	 * C type : MP4ItmfItem*<br>
	 * @param size < number of elements.
	 */
	public MP4ItmfItemList(MP4ItmfItem.ByReference elements, int size) {
		super();
		this.elements = elements;
		this.size = size;
		initFieldOrder();
	}
	public static class ByReference extends MP4ItmfItemList implements Structure.ByReference {

	};
	public static class ByValue extends MP4ItmfItemList implements Structure.ByValue {

	};
}
