package org.stanwood.media.jna;

import com.sun.jna.Native;

/** Provide simplified platform information. */
@SuppressWarnings("all")
public final class Platform {
	//TODO Change this to a enum
	/** Unkown OS */
    public static final int UNSPECIFIED = -1;
    /** Mac OS */
    public static final int MAC = 0;
    /** Linux OS */
    public static final int LINUX = 1;
    /** Windows OS */
    public static final int WINDOWS = 2;
    /** Solaris OS */
    public static final int SOLARIS = 3
    /** FreeBSD OS */;
    public static final int FREEBSD = 4;
    /** OpenBSD OS */
    public static final int OPENBSD = 5;
    /** WindowsCE OS */
    public static final int WINDOWSCE = 6;

    private static final int osType;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            osType = LINUX;
        }
        else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            osType = MAC;
        }
        else if (osName.startsWith("Windows CE")) {
            osType = WINDOWSCE;
        }
        else if (osName.startsWith("Windows")) {
            osType = WINDOWS;
        }
        else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            osType = SOLARIS;
        }
        else if (osName.startsWith("FreeBSD")) {
            osType = FREEBSD;
        }
        else if (osName.startsWith("OpenBSD")) {
            osType = OPENBSD;
        }
        else {
            osType = UNSPECIFIED;
        }
    }
    private Platform() { }

    /**
     * Get the OS Type
     * @return the OS Type
     */
    public static final int getOSType() {
        return osType;
    }
    public static final boolean isMac() {
        return osType == MAC;
    }
    public static final boolean isLinux() {
        return osType == LINUX;
    }
    public static final boolean isWindowsCE() {
        return osType == WINDOWSCE;
    }
    public static final boolean isWindows() {
        return osType == WINDOWS || osType == WINDOWSCE;
    }
    public static final boolean isSolaris() {
        return osType == SOLARIS;
    }
    public static final boolean isFreeBSD() {
        return osType == FREEBSD;
    }
    public static final boolean isOpenBSD() {
        return osType == OPENBSD;
    }
    public static final boolean isX11() {
        // TODO: check filesystem for /usr/X11 or some other X11-specific test
        return !Platform.isWindows() && !Platform.isMac();
    }
    public static final boolean deleteNativeLibraryAfterVMExit() {
        return osType == WINDOWS;
    }
    public static final boolean hasRuntimeExec() {
        if (isWindowsCE() && "J9".equals(System.getProperty("java.vm.name"))) {
			return false;
		}
        return true;
    }
    public static final boolean is64Bit() {
        String model = System.getProperty("sun.arch.data.model");
        if (model != null) {
			return "64".equals(model);
		}
        String arch = System.getProperty("os.arch").toLowerCase();
        if ("x86_64".equals(arch)
            || "ppc64".equals(arch)
            || "sparcv9".equals(arch)
            || "amd64".equals(arch)) {
            return true;
        }
        return Native.POINTER_SIZE == 8;
    }
}
