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
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        if (osName.startsWith("Linux")) { //$NON-NLS-1$
            osType = LINUX;
        }
        else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) { //$NON-NLS-1$ //$NON-NLS-2$
            osType = MAC;
        }
        else if (osName.startsWith("Windows CE")) { //$NON-NLS-1$
            osType = WINDOWSCE;
        }
        else if (osName.startsWith("Windows")) { //$NON-NLS-1$
            osType = WINDOWS;
        }
        else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) { //$NON-NLS-1$ //$NON-NLS-2$
            osType = SOLARIS;
        }
        else if (osName.startsWith("FreeBSD")) { //$NON-NLS-1$
            osType = FREEBSD;
        }
        else if (osName.startsWith("OpenBSD")) { //$NON-NLS-1$
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
        if (isWindowsCE() && "J9".equals(System.getProperty("java.vm.name"))) { //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
        return true;
    }
    public static final boolean is64Bit() {
        String model = System.getProperty("sun.arch.data.model"); //$NON-NLS-1$
        if (model != null) {
			return "64".equals(model); //$NON-NLS-1$
		}
        String arch = System.getProperty("os.arch").toLowerCase(); //$NON-NLS-1$
        if ("x86_64".equals(arch) //$NON-NLS-1$
            || "ppc64".equals(arch) //$NON-NLS-1$
            || "sparcv9".equals(arch) //$NON-NLS-1$
            || "amd64".equals(arch)) { //$NON-NLS-1$
            return true;
        }
        return Native.POINTER_SIZE == 8;
    }
}
