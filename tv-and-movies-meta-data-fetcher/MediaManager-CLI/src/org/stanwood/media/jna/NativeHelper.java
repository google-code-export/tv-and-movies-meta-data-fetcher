package org.stanwood.media.jna;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.util.FileHelper;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Used to help with the native libraries and command line tools
 */
public class NativeHelper {

	private final static Log log = LogFactory.getLog(NativeHelper.class);

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
	 * Used to get the path to a native application.
	 * @param nativeDir The directory that contains the native libs and apps
	 * @param appName The application name
	 * @return The app name to execute
	 */
	public static String getNativeApplication(File nativeDir,String appName) {
		String method = System.getenv("MM_EXE_LOCATE_METHOD"); //$NON-NLS-1$
		if (log.isDebugEnabled()) {
			log.debug("Getting native application, method forced to: " + method); //$NON-NLS-1$
			log.debug("Using native directory: " + nativeDir); //$NON-NLS-1$
		}
		appName = getAppName(appName);
		String nativePath = getAppArchPath(appName);
		if (log.isDebugEnabled()) {
			log.debug("App Native Path: " + nativePath); //$NON-NLS-1$
		}
		if (method==null || method.length()==0 || method.equals("installed")) { //$NON-NLS-1$
			if (nativeDir!=null) {
				File f =new File(nativeDir,nativePath);
				if (log.isDebugEnabled()) {
					log.debug("Checking via install: " + f.getAbsolutePath()); //$NON-NLS-1$
				}
				if (f.exists()) {
					if (log.isDebugEnabled()) {
						log.debug("Found: " + f.getAbsolutePath()); //$NON-NLS-1$
					}
					return f.getAbsolutePath();
				}
			}
		}
		if (method==null || method.length()==0 || method.equals("project")) { //$NON-NLS-1$
			File f =new File(FileHelper.getWorkingDirectory(),".."+File.separator+"native"+File.separator+nativePath); //$NON-NLS-1$ //$NON-NLS-2$
			if (log.isDebugEnabled()) {
				log.debug("Checking via project: " + f.getAbsolutePath()); //$NON-NLS-1$
			}
			if (f.exists()) {
				if (log.isDebugEnabled()) {
					log.debug("Found: " + f.getAbsolutePath()); //$NON-NLS-1$
				}
				return f.getAbsolutePath();
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Did not find so just return it for use on system path: " + appName); //$NON-NLS-1$
		}
		return appName;
	}

	private static String getAppName(String appName) {
		if (Platform.isWindows()) {
			return appName+".exe"; //$NON-NLS-1$
		}
		return appName;
	}

	/**
	 * <p>Used to load a native library. This will first try to load it from the system.
     * If this fails, it checks a environment variable MM_NATIVE_DIR for the location of native
     * libraries and load from their. If this fails, it checks for a native folder in the current
     * working directory (This is mainly for tests).</p>
     * @param nativeDir The directory that contains the native libs and apps
     * @param libName The name of the library
     * @param interfaceClass The name of the class the library will implement4
	 * @return The library
	 * @throws UnsatisfiedLinkError An error is thrown in the library can't be found.
	 */
	public static Object loadLibrary(File nativeDir,String libName,Class<?>interfaceClass) throws UnsatisfiedLinkError {
		if (nativeDir==null) {
			if (System.getProperty("NATIVE_DIR")!=null) { //$NON-NLS-1$
				nativeDir = new File(System.getProperty("NATIVE_DIR")); //$NON-NLS-1$
			}
		}
		String method = System.getenv("MM_LIB_LOAD_METHOD"); //$NON-NLS-1$
		Error error = null;
		String nativePath = getLibArchPath(libName);
		if (method==null || method.length()==0 || method.equals("installed")) { //$NON-NLS-1$
			try {
				if (nativeDir!=null) {
					String path =new File(nativeDir,nativePath).getAbsolutePath();
					return loadLibraryWithOptions(path,interfaceClass);
				}
			}
			catch (Error e1) {
				error = e1;
				if (log.isDebugEnabled()) {
					log.debug("Unable to load the version of the library that is shipped with the product '"+libName+"'",e1); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		if (method==null || method.length()==0 || method.equals("project")) { //$NON-NLS-1$
			try {
				// If this is a test, find within the project
				String path =new File(FileHelper.getWorkingDirectory(),".."+File.separator+"native"+File.separator+nativePath).getAbsolutePath(); //$NON-NLS-1$ //$NON-NLS-2$
				return loadLibraryWithOptions(path,interfaceClass);
			}
			catch (Error e1) {
				if (log.isDebugEnabled()) {
					log.debug("Unable to load the version of the library within the project '"+libName+"'",e1);  //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		if (error!=null) {
			log.error(error.getLocalizedMessage(),error);
		}
		throw new UnsatisfiedLinkError(MessageFormat.format(Messages.getString("NativeHelper.UNABLE_LOAD_LIB"),libName)); //$NON-NLS-1$
	}

	private static String getAppArchPath(String appName) {
		StringBuilder result = new StringBuilder("apps"+File.separator); //$NON-NLS-1$
		switch (Platform.getOSType()) {
		case Platform.LINUX :
			result.append("linux"); //$NON-NLS-1$
			break;
		case Platform.MAC:
			result.append("mac"); //$NON-NLS-1$
			break;
		case Platform.WINDOWS:
			result.append("windows"); //$NON-NLS-1$
			break;
		default:
			throw new Error(MessageFormat.format(Messages.getString("NativeHelper.NO_NATIVE_APP_FOR_PLATFORM"),appName)); //$NON-NLS-1$
		}
		result.append(File.separator);
		result.append("x86"); //$NON-NLS-1$
		result.append(File.separator);
		if (Platform.is64Bit()) {
			result.append("64"); //$NON-NLS-1$
		}
		else {
			result.append("32"); //$NON-NLS-1$
		}
		result.append(File.separator);
		switch (Platform.getOSType()) {
		case Platform.LINUX :
			result.append(appName);
			break;
		case Platform.MAC:
			result.append(appName);
			break;
		case Platform.WINDOWS:
			result.append(appName);
			break;
		default:
			throw new Error(MessageFormat.format(Messages.getString("NativeHelper.NO_NATIVE_APP_FOR_PLATFORM"),appName)); //$NON-NLS-1$
		}
		return result.toString();
	}

	private static String getLibArchPath(String libName) {
		StringBuilder result = new StringBuilder("libs"+File.separator); //$NON-NLS-1$
		switch (Platform.getOSType()) {
		case Platform.LINUX :
			result.append("linux"); //$NON-NLS-1$
			break;
		case Platform.MAC:
			result.append("mac"); //$NON-NLS-1$
			break;
		case Platform.WINDOWS:
			result.append("windows"); //$NON-NLS-1$
			break;
		default:
			throw new Error(MessageFormat.format(Messages.getString("NativeHelper.NO_NATIVE_LIB_FOR_PLATFORM"),libName)); //$NON-NLS-1$
		}
		result.append(File.separator);
		result.append("x86"); //$NON-NLS-1$
		result.append(File.separator);
		if (Platform.is64Bit()) {
			result.append("64"); //$NON-NLS-1$
		}
		else {
			result.append("32"); //$NON-NLS-1$
		}
		result.append(File.separator);
		switch (Platform.getOSType()) {
		case Platform.LINUX :
			result.append("lib"+libName+".so"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case Platform.MAC:
			result.append("lib"+libName+".dylib");  //$NON-NLS-1$//$NON-NLS-2$
			break;
		case Platform.WINDOWS:
			result.append("lib"+libName+".dll"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		default:
			throw new Error(MessageFormat.format(Messages.getString("NativeHelper.NO_NATIVE_LIB_FOR_PLATFORM"),libName)); //$NON-NLS-1$
		}
		return result.toString();
	}


}
