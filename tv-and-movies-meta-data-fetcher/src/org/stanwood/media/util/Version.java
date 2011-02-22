package org.stanwood.media.util;

import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

	private String normalised;
	private String version;

	public Version(String version) {
		normalised = normalisedVersion(version);
		this.version = version;
	}

	@Override
	public int compareTo(Version o) {
		return normalised.compareTo(o.normalised);
	}

	public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    public static String normalisedVersion(String version, String sep, int maxWidth) {
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

	@Override
	public String toString() {
		return version;
	}


}
