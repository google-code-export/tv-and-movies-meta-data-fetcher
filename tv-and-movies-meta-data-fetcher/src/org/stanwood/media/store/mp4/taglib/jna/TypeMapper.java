package org.stanwood.media.store.mp4.taglib.jna;


import com.sun.jna.DefaultTypeMapper;

public class TypeMapper extends DefaultTypeMapper  {

	public TypeMapper() {
		super();
		addTypeConverter(JnaEnum.class, new EnumConverter());
	}


}
