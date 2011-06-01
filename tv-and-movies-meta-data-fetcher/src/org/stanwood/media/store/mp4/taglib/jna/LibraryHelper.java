package org.stanwood.media.store.mp4.taglib.jna;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Used to help with the native library mappings
 */
public class LibraryHelper {

	/**
	 * Used to load the library and setup it's options
	 * @param name The name of the lib
	 * @param interfaceClass The interface it's mapped to
	 * @return The lib
	 */
	public static Object loadLibrary(String name, Class<?> interfaceClass) {
		Map<String, Object> options = new HashMap<String, Object>();
    	options.put(Library.OPTION_TYPE_MAPPER, new TypeMapper());
        return Native.loadLibrary(name,interfaceClass);
	}
}
