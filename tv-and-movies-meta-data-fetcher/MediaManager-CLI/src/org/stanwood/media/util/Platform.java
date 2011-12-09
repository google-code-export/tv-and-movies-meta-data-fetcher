/*
 *  Copyright (C) 2008-2011  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.util;

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
        throw new RuntimeException("Unable to workout bitness of JVM");
//        return Native.POINTER_SIZE == 8;
    }
}
