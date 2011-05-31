package org.stanwood.media.store.mp4.taglib.jna;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class LibraryHelper {

	public static Object loadLibrary(String name, Class interfaceClass) {
		Map<String, Object> options = new HashMap<String, Object>();
    	options.put(Library.OPTION_TYPE_MAPPER, new TypeMapper());
        return Native.loadLibrary(name,interfaceClass);
	}
}
