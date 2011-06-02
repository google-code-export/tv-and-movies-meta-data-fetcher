package org.stanwood.media.store.mp4.mp4v2.lib.itmfgeneric;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

@SuppressWarnings("all")
public class MP4ItmfData extends Structure {
	/// < always zero.
	public byte typeSetIdentifier;
	/**
	 * @see MP4ItmfBasicType<br>
	 * < iTMF basic type.<br>
	 * C type : MP4ItmfBasicType
	 */
	public int typeCode;
	/// < always zero.
	public int locale;
	/**
	 * < may be NULL.<br>
	 * C type : uint8_t*
	 */
	public Pointer value;
	/// < value size in bytes.
	public int valueSize;
	public MP4ItmfData() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"typeSetIdentifier", "typeCode", "locale", "value", "valueSize"});
	}
	/**
	 * @param typeSetIdentifier < always zero.<br>
	 * @param typeCode @see MP4ItmfBasicType<br>
	 * < iTMF basic type.<br>
	 * C type : MP4ItmfBasicType<br>
	 * @param locale < always zero.<br>
	 * @param value < may be NULL.<br>
	 * C type : uint8_t*<br>
	 * @param valueSize < value size in bytes.
	 */
	public MP4ItmfData(byte typeSetIdentifier, int typeCode, int locale, Pointer value, int valueSize) {
		super();
		this.typeSetIdentifier = typeSetIdentifier;
		this.typeCode = typeCode;
		this.locale = locale;
		this.value = value;
		this.valueSize = valueSize;
		initFieldOrder();
	}
	public static class ByReference extends MP4ItmfData implements Structure.ByReference {

	};
	public static class ByValue extends MP4ItmfData implements Structure.ByValue {

	};
}
