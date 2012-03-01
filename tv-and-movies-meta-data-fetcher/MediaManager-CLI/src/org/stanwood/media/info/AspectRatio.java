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

public enum AspectRatio {

	Ratio_1_1(false),
	Ratio_16_9(true),
	Ratio_16_10(true),
	Ratio_14_9(true),
	Ratio_2_35_1(true),
	Ratio_4_3(false),
	Unknown(false);

	private boolean wideScreen;

	private AspectRatio(boolean wideScreen) {
		this.wideScreen = wideScreen;
	}

	public boolean isWideScreen() {
		return wideScreen;
	}

	@SuppressWarnings("nls")
	public static AspectRatio fromString(String ratio) {
		AspectRatio aspectRatio = Unknown;
		if (ratio.equals("16:9")) {
			aspectRatio = Ratio_16_9;
		}
		else if (ratio.equals("4:3")) {
			aspectRatio = Ratio_4_3;
		}
		else if (ratio.equals("16:10")) {
			aspectRatio = Ratio_16_10;
		}
		else if (ratio.equals("14:9")) {
			aspectRatio = Ratio_16_10;
		}
		else if (ratio.equals("2.35:1")) {
			aspectRatio = Ratio_2_35_1;
		}
		else if (ratio.equals("1")) {
			aspectRatio = Ratio_1_1;
		}
		else if (ratio.equals("1:1")) {
			aspectRatio = Ratio_1_1;
		}
		return aspectRatio;
	}

}
