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

import java.util.regex.Pattern;

import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class VideoInfoParser extends XMLParser {

	private final static Pattern PATTERN_FPS = Pattern.compile("(.*) fps");
	private final static Pattern PATTERN_PIXELS = Pattern.compile("(.*) pixels");
	private final static Pattern PATTERN_MS = Pattern.compile("(.*)ms");
	private Document dom;
	private Node videoTrack;

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

	private String getTrackString(Node trackNode,String key) throws XMLParserException {
		return getFirstChildElement(trackNode,key).getTextContent();
	}

	private Float getTrackFrameRate(Node trackNode) throws XMLParserException {
		return Float.parseFloat(getTrackString(trackNode,"Frame_rate"));
	}

	private Integer getTrackDuration(Node trackNode) throws XMLParserException {
		return Integer.parseInt(getTrackString(trackNode,"Duration"));
	}

	public Integer getHeight() throws XMLParserException {
		Node track = getVideoTrack();
		return Integer.parseInt(getTrackString(track,"Height"));
	}

	public Integer getWidth() throws XMLParserException {
		Node track = getVideoTrack();
		return Integer.parseInt(getTrackString(track,"Width"));
	}

	public Float getFrameRate() throws XMLParserException {
		Node track = getVideoTrack();
		return getTrackFrameRate(track);
	}

	public Integer getDuration() throws XMLParserException {
		Node track = getVideoTrack();
		return getTrackDuration(track);
	}

	public String getAspectRatio() throws XMLParserException {
		Node track = getVideoTrack();
		return getLastChildElement(track,"Display_aspect_ratio").getTextContent();
	}
}
