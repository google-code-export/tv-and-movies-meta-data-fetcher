package org.stanwood.media.util;

import java.util.regex.Pattern;

/**
 * Used to store version information and allow it to be compared to another version
 */
public class Version implements Comparable<Version> {

	private String normalised;
	private String version;

	/**
	 * Used to construct a version object
	 * @param version The version (eg 2.6.37 or 2a.4)
	 */
	public Version(String version) {
		normalised = normalisedVersion(version);
		this.version = version;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Version o) {
		return normalised.compareTo(o.normalised);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return version.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Version) {
			return version.equals(((Version)obj).version);
		}
		else {
			return false;
		}
	}

	private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    private static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

    String getNormalised() {
    	return normalised;
    }

    /**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return version;
	}


}
