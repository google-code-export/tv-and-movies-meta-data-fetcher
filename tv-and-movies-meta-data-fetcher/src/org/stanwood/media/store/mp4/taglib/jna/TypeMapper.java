package org.stanwood.media.store.mp4.taglib.jna;


import com.sun.jna.DefaultTypeMapper;

/**
 * Used to map types in the jna mapping
 */
public class TypeMapper extends DefaultTypeMapper  {

	/**
	 * The constructor
	 */
	public TypeMapper() {
		super();
		addTypeConverter(JnaEnum.class, new EnumConverter());
	}


}
