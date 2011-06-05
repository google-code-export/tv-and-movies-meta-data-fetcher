package org.stanwood.media.jna;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.util.FileHelper;

import com.sun.jna.Library;
import com.sun.jna.Native;

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
	 * @throws UnsatisfiedLinkError An error is thrown in the library can't be found.
	 */
	public static Object loadLibraryWithOptions(String name, Class<?> interfaceClass) throws UnsatisfiedLinkError {
		Map<String, Object> options = new HashMap<String, Object>();
    	options.put(Library.OPTION_TYPE_MAPPER, new TypeMapper());
        Object lib = Native.loadLibrary(name,interfaceClass);
        return lib;
	}

	/**
	 * <p>Used to load a native library. This will first try to load it from the system.
     * If this fails, it checks a environment variable MM_NATIVE_DIR for the location of native
     * libraries and load from their. If this fails, it checks for a native folder in the current
     * working directory (This is mainly for tests).</p>
     * @param libName The name of the library
     * @param interfaceClass The name of the class the library will implement
	 * @return The library
	 * @throws UnsatisfiedLinkError An error is thrown in the library can't be found.
	 */
	public static Object loadLibrary(String libName,Class<?>interfaceClass) throws UnsatisfiedLinkError {
		String method = System.getenv("MM_LIB_LOAD_METHOD");
		Error error = null;

		if (method==null || method.length()==0 || method.equals("system")) {
			try {
				// try load from the system
				return loadLibraryWithOptions(libName,interfaceClass);
			}
			catch (Error e) {
				if (log.isDebugEnabled()) {
					log.debug("Unable to find system version of library '"+libName+"'",e);
				}
			}
		}
		String nativePath = getArchPath(libName);
		if (method==null || method.length()==0 || method.equals("installed")) {
			try {
				String nativeDir = System.getenv("MM_NATIVE_DIR");
				if (nativeDir!=null && !nativeDir.equals("")) {
					String path =new File(nativeDir+File.separator+nativePath).getAbsolutePath();
					return loadLibraryWithOptions(path,interfaceClass);
				}
			}
			catch (Error e1) {
				error = e1;
				if (log.isDebugEnabled()) {
					log.debug("Unable to load the version of the library that is shipped with the product '"+libName+"'",e1);
				}
			}
		}
		if (method==null || method.length()==0 || method.equals("project")) {
			try {
				// If this is a test, find within the project
				String path =new File(FileHelper.getWorkingDirectory(),"native"+File.separator+nativePath).getAbsolutePath();
				return loadLibraryWithOptions(path,interfaceClass);
			}
			catch (Error e1) {
				if (log.isDebugEnabled()) {
					log.debug("Unable to load the version of the library within the project '"+libName+"'",e1);
				}
			}
		}
		if (error!=null) {
			log.error(error.getLocalizedMessage(),error);
		}
		throw new UnsatisfiedLinkError("Unable to load native library '"+libName+"'");
	}



	private static String getArchPath(String libName) {
		StringBuilder result = new StringBuilder("libs"+File.separator);
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
			result.append("lib"+libName+".dll");
			break;
		default:
			throw new Error("No native lib " + libName+ " for this platform");
		}
		return result.toString();
	}


}
