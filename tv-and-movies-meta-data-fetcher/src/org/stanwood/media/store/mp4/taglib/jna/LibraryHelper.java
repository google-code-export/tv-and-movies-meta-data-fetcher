package org.stanwood.media.store.mp4.taglib.jna;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.store.mp4.mp4v2.lib.MP4v2Library;
import org.stanwood.media.util.FileHelper;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * Used to help with the native library mappings
 */
public class LibraryHelper {

	private final static Log log = LogFactory.getLog(LibraryHelper.class);

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

	public static MP4v2Library loadMP4v2Library() {
		String libName = "mp4v2";
		try {
			// try load from the system
			return (MP4v2Library) loadLibrary(libName,MP4v2Library.class);
		}
		catch (Error e) {
			if (log.isDebugEnabled()) {
				log.debug("Unable to find system version of library '"+libName+"'",e);
			}
		}
		String nativePath = getArchPath(libName);
		try {
			String nativeDir = System.getenv("MM_NATIVE_DIR");
			if (nativeDir!=null && !nativeDir.equals("")) {
				String path =new File(nativeDir+File.separator+nativePath).getAbsolutePath();
				return (MP4v2Library) loadLibrary(path,MP4v2Library.class);
			}
		}
		catch (Error e1) {
			if (log.isDebugEnabled()) {
				log.debug("Unable to load the version of the library that is shipped with the product '"+libName+"'",e1);
			}
		}
		try {
			// If this is a test, find within the project
			String path =new File(FileHelper.getWorkingDirectory(),"native"+File.separator+nativePath).getAbsolutePath();
			return (MP4v2Library) loadLibrary(path,MP4v2Library.class);
		}
		catch (Error e1) {
			if (log.isDebugEnabled()) {
				log.debug("Unable to load the version of the library within the project '"+libName+"'",e1);
			}
		}
		throw new Error("Unable to load native library '"+libName+"'");
	}

	private static String getArchPath(String libName) {
		StringBuilder result = new StringBuilder();
		switch (Platform.getOSType()) {
		case Platform.LINUX :
			result.append("linux");
			break;
		case Platform.MAC:
			result.append("mac");
			break;
		case Platform.WINDOWS:
			result.append("windows");
			break;
		default:
			throw new Error("No native lib " + libName+ " for this platform");
		}
		result.append(File.separator);
		result.append("x86");
		result.append(File.separator);
		if (Platform.is64Bit()) {
			result.append("64");
		}
		else {
			result.append("32");
		}
		result.append(File.separator);
		switch (Platform.getOSType()) {
		case Platform.LINUX :
			result.append("lib"+libName+".so");
			break;
		case Platform.MAC:
			result.append("lib"+libName+".dylib");
			break;
		case Platform.WINDOWS:
			result.append("lib"+libName+".so");
			break;
		default:
			throw new Error("No native lib " + libName+ " for this platform");
		}
		return result.toString();
	}
}
