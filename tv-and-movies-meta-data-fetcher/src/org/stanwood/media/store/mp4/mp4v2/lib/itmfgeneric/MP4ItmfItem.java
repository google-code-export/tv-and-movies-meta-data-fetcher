package org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

@SuppressWarnings("all")
public class MP4ItmfItem extends Structure {
	/// < 0-based index of item in ilst container. -1 if undefined.
	public int index;
	/**
	 * < four-char code identifing atom type. NULL-terminated.<br>
	 * C type : char*
	 */
	public Pointer code;
	/**
	 * < may be NULL. UTF-8 meaning. NULL-terminated.<br>
	 * C type : char*
	 */
	public Pointer mean;
	/**
	 * < may be NULL. UTF-8 name. NULL-terminated.<br>
	 * C type : char*
	 */
	public Pointer name;
	/**
	 * < list of data. size is always >= 1.<br>
	 * C type : MP4ItmfDataList
	 */
	public MP4ItmfDataList dataList;
	public MP4ItmfItem() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"index", "code", "mean", "name", "dataList"});
	}
	/**
	 * @param index < 0-based index of item in ilst container. -1 if undefined.<br>
	 * @param code < four-char code identifing atom type. NULL-terminated.<br>
	 * C type : char*<br>
	 * @param mean < may be NULL. UTF-8 meaning. NULL-terminated.<br>
	 * C type : char*<br>
	 * @param name < may be NULL. UTF-8 name. NULL-terminated.<br>
	 * C type : char*<br>
	 * @param dataList < list of data. size is always >= 1.<br>
	 * C type : MP4ItmfDataList
	 */
	public MP4ItmfItem(int index, Pointer code, Pointer mean, Pointer name, MP4ItmfDataList dataList) {
		super();
		this.index = index;
		this.code = code;
		this.mean = mean;
		this.name = name;
		this.dataList = dataList;
		initFieldOrder();
	}
	public static class ByReference extends MP4ItmfItem implements Structure.ByReference {

	};
	public static class ByValue extends MP4ItmfItem implements Structure.ByValue {

	};
}
