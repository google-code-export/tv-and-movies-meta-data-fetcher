package org.stanwood.media.util;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to help with the native libraries and command line tools
 */
public class NativeHelper {

	private final static Log log = LogFactory.getLog(NativeHelper.class);


	/**
	 * Used to get the path to a native application.
	 * @param nativeDir The directory that contains the native libs and apps
	 * @param appName The application name
	 * @return The app name to execute
	 */
	public static String getNativeApplication(File nativeDir,String appName) {
		if (nativeDir==null) {
			if (System.getProperty("NATIVE_DIR")!=null) { //$NON-NLS-1$
				nativeDir = new File(System.getProperty("NATIVE_DIR")); //$NON-NLS-1$
			}
		}

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


}
