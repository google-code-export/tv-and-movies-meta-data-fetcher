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

import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is used to parse the XML output of the mediainfo command
 */
public class VideoInfoParser extends XMLParser {

	private Document dom;
	private Node videoTrack;
	private Node audioTrack;

	/**
	 * Constructor
	 * @param dom The DOM XML model output from the command mediainfo
	 */
	public VideoInfoParser(Document dom)  {
		this.dom = dom;
	}

	private Node getVideoTrack() throws XMLParserException {
		if (videoTrack==null) {
			for (Node node : selectNodeList(dom, "Mediainfo/File/track[@type='Video']")) { //$NON-NLS-1$
				if (videoTrack == null ||
				    getTrackDuration(node) > getTrackDuration(videoTrack) ||
				    getTrackFrameRate(node) > getTrackFrameRate(node)) {
					videoTrack = node;
				}
			}
		}
		return videoTrack;
	}

	private Node getAudioTrack() throws XMLParserException {
		if (audioTrack==null) {
			for (Node node : selectNodeList(dom, "Mediainfo/File/track[@type='Audio']")) { //$NON-NLS-1$
				audioTrack = node;
			}
		}
		return audioTrack;
	}

	private String getTrackString(Node trackNode,String key) throws XMLParserException {
		if (trackNode==null) {
			return null;
		}
		Element value = getFirstChildElement(trackNode,key);
		if (value==null) {
			return null;
		}
		return value.getTextContent();
	}

	private Float getTrackFrameRate(Node trackNode) throws XMLParserException {
		String str = getTrackString(trackNode,"Frame_rate"); //$NON-NLS-1$
		if (str==null) {
			return null;
		}
		return Float.parseFloat(str);
	}

	private Integer getTrackDuration(Node trackNode) throws XMLParserException {
		String str = getTrackString(trackNode,"Duration"); //$NON-NLS-1$
		if (str==null) {
			return null;
		}
		return Integer.parseInt(str);
	}

	/**
	 * Used to get the height in pixels or null if it can be found
	 * @return the height or null if it can be found
	 * @throws XMLParserException Thrown if their is a parser error
	 */
	public Integer getHeight() throws XMLParserException {
		Node track = getVideoTrack();
		if (track==null) {
			return null;
		}
		String str =getTrackString(track,"Height"); //$NON-NLS-1$
		if (str==null) {
			return null;
		}
		return Integer.parseInt(str);
	}

	/**
	 * Used to get the width in pixels or null if it can be found
	 * @return the width or null if it can be found
	 * @throws XMLParserException Thrown if their is a parser error
	 */
	public Integer getWidth() throws XMLParserException {
		Node track = getVideoTrack();
		if (track==null) {
			return null;
		}
		String str = getTrackString(track,"Width"); //$NON-NLS-1$
		if (str==null) {
			return null;
		}
		return Integer.parseInt(str);
	}

	/**
	 * Used to get the frames per second or null if it can't be found
	 * @return the frames per second or null if it can't be found
	 * @throws XMLParserException Thrown if their is a parser error
	 */
	public Float getFrameRate() throws XMLParserException {
		Node track = getVideoTrack();
		if (track==null) {
			return null;
		}
		return getTrackFrameRate(track);
	}

	/**
	 * Used to get the video duration in milliseconds or null if it can't be found
	 * @return the video duration in milliseconds or null if it can't be found
	 * @throws XMLParserException Thrown if their is a parser error
	 */
	public Integer getDuration() throws XMLParserException {
		Node track = getVideoTrack();
		if (track==null) {
			return null;
		}
		return getTrackDuration(track);
	}

	/**
	 * Used to get the aspect ratio or null if it can't be found
	 * @return the aspect ratio or null if it can't be found
	 * @throws XMLParserException Thrown if their is a parser error
	 */
	public String getAspectRatio() throws XMLParserException {
		Node track = getVideoTrack();
		if (track==null) {
			return null;
		}
		return getLastChildElement(track,"Display_aspect_ratio").getTextContent(); //$NON-NLS-1$
	}

	/**
	 * Used to find out if the scan type is interlaced
	 * @return true if the scan type is interlaced, otherwise false
	 * @throws XMLParserException Thrown if their is a parser error
	 */
	public boolean getInterlaced() throws XMLParserException {
		Node track = getVideoTrack();
		if (track==null) {
			return false;
		}
		String value = getTrackString(track,"Scan_type"); //$NON-NLS-1$
		if (value==null) {
			return false;
		}
		return !value.equalsIgnoreCase("progressive");  //$NON-NLS-1$
	}

	/**
	 * Used to get the audio format profile
	 * @return The audio format profile
	 * @throws XMLParserException Thrown if their is a parser error
	 */
	public String getAudioFormatProfile() throws XMLParserException {
		Node track = getAudioTrack();
		if (track==null) {
			return null;
		}
		String value = getTrackString(track,"Format_profile"); //$NON-NLS-1$
		return value;
	}

	/**
	 * Used to get the audio bit rate (Kbs)
	 * @return The audio bit rate (Kbs)
	 * @throws XMLParserException
	 */
	public Long getAudioBitRate() throws XMLParserException {
		Node track = getAudioTrack();
		if (track==null) {
			return null;
		}
		String value = getTrackString(track,"Bit_rate"); //$NON-NLS-1$
		if (value==null) {
			return null;
		}

		return Math.round(((double)Long.parseLong(value))/1000);
	}

}
