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
package org.stanwood.media.info;

import java.text.DecimalFormat;

/**
 * Used to enumerate known display aspect ratios
 */
public enum AspectRatio {

	/** The aspect ratio 1:1 */
	Ratio_1_1(1,1,false),
	/** The aspect ratio 16:9 */
	Ratio_16_9(16,9,true),
	/** The aspect ratio 16:10 */
	Ratio_16_10(16,10,true),
	/** The aspect ratio 14:9 */
	Ratio_14_9(14,9,true),
	/** The aspect ratio 2.35:1 */
	Ratio_2_35_1(2.35,1,true),
	/** The aspect ratio 4:3 */
	Ratio_4_3(4,3,false),
	/** This means the ratio is not known */
	Unknown(-1,-1,false);

	private boolean wideScreen;
	private String description;
	private double value;


	private AspectRatio(double ratioX,double ratioY,boolean wideScreen) {
		this.wideScreen = wideScreen;
		DecimalFormat twoDForm = new DecimalFormat("#.##"); //$NON-NLS-1$
		this.description = twoDForm.format(ratioX)+":"+twoDForm.format(ratioY); //$NON-NLS-1$
		this.value = ratioX / ratioY;
	}

	/**
	 * Returns true if the aspect ratio is wide screen
	 * @return true if the aspect ratio is wide screen
	 */
	public boolean isWideScreen() {
		return wideScreen;
	}

	/**
	 * Used to get the ratio as a string
	 * @return the ratio as a string
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Used to get the ratio as a decimal value
	 * @return the ratio as a decimal value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Used to find the ratio from it's decimal value
	 * @param ratio The decimal value of the ratio
	 * @return The ratio
	 */
	public static AspectRatio fromRatio(double ratio) {
		for (AspectRatio aspectRatio : values()) {
			if (aspectRatio.getValue() == ratio) {
				return aspectRatio ;
			}
		}
		return Unknown;
	}

	/**
	 * Used to find the ratio from it's string description value
	 * @param ratio The string description value of the ratio
	 * @return The ratio
	 */
	public static AspectRatio fromString(String ratio) {
		if (ratio==null) {
			return Unknown;
		}
		DecimalFormat twoDForm = new DecimalFormat("#.##"); //$NON-NLS-1$
		for (AspectRatio aspectRatio : values()) {
			if (aspectRatio.getDescription().equals(ratio)) {
				return aspectRatio;
			}
			else {
				try {
					double value = Double.parseDouble(ratio);
					if (twoDForm.format(aspectRatio.getValue()).equals(twoDForm.format(value))) {
						return aspectRatio;
					}
				}
				catch (NumberFormatException e) {

				}
			}
		}
		return Unknown;
	}

}
