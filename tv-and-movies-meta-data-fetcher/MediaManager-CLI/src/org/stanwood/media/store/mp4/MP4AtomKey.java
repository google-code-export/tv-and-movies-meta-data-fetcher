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
package org.stanwood.media.store.mp4;


public enum MP4AtomKey {

	NAME("Name","©nam",MP4AtomKeyType.String), //$NON-NLS-2$
	ARTIST("Artist","©ART",MP4AtomKeyType.String), //$NON-NLS-2$
	ALBUM_ARTIST("Album Artist","aART",MP4AtomKeyType.String), //$NON-NLS-2$
	ALBUM("Album","©alb",MP4AtomKeyType.String), //$NON-NLS-2$
	GROUPING("Grouping","grup",MP4AtomKeyType.String), //$NON-NLS-2$
	COMPOSER("Composer","©wrt",MP4AtomKeyType.String), //$NON-NLS-2$
	COMMENT("Comment","©cmt",MP4AtomKeyType.String), //$NON-NLS-2$
	FLAVOUR("Flavour","flvr",MP4AtomKeyType.String), //$NON-NLS-2$
	GENRE_PRE_DEFINED("Genre, Pre-defined","gnre",MP4AtomKeyType.String), //$NON-NLS-2$
	GENRE_USER_DEFINED("Genre, User defined","©gen",MP4AtomKeyType.String), //$NON-NLS-2$
	RELEASE_DATE("Release Date","©day",MP4AtomKeyType.Date), //$NON-NLS-2$
	TRACK_NUMBER("Track Number","trkn",MP4AtomKeyType.Range), //$NON-NLS-2$
	DISK_NUMBER("Disc Number","disk",MP4AtomKeyType.Range), //$NON-NLS-2$
	TEMPO("Tempo","tmpo",MP4AtomKeyType.Short), //$NON-NLS-2$
	COMPILATION("Compilation","cpil",MP4AtomKeyType.Boolean), //$NON-NLS-2$
	TV_SHOW_NAME("TV Show Name","tvsh",MP4AtomKeyType.String), //$NON-NLS-2$
	TV_EPISODE_ID("TV Episode ID","tven",MP4AtomKeyType.String), //$NON-NLS-2$
	TV_SEASON("TV Season Number","tvsn",MP4AtomKeyType.Integer), //$NON-NLS-2$
	TV_EPISODE("TV Episode Number","tves",MP4AtomKeyType.Integer), //$NON-NLS-2$
	TV_NETWORK("TV Network","tvnn",MP4AtomKeyType.String), //$NON-NLS-2$
	DESCRIPTION("Description","desc",MP4AtomKeyType.String), //$NON-NLS-2$
	DESCRIPTION_STORE("Store Description","sdes",MP4AtomKeyType.String), //$NON-NLS-2$
	DESCRIPTION_LONG("Long description","ldes",MP4AtomKeyType.String), //$NON-NLS-2$
	LYRICS("Lyrics","©lyr",MP4AtomKeyType.String), //$NON-NLS-2$
	SORT_NAME("Sort Name","sonm",MP4AtomKeyType.String), //$NON-NLS-2$
	SORT_ARTIST("Sort Artist","soar",MP4AtomKeyType.String),//$NON-NLS-2$
	SORT_ALBUM_ARTIST("Sort Album Artist","soaa",MP4AtomKeyType.String), //$NON-NLS-2$
	SORT_ALBUM("Sort Album","soal",MP4AtomKeyType.String), //$NON-NLS-2$
	SORT_COMPOSER("Sort Composer","soco",MP4AtomKeyType.String), //$NON-NLS-2$
	SORT_SHOW("Sort Show","sosn",MP4AtomKeyType.String), //$NON-NLS-2$
	ARTWORK("Cover Artwork","covr",MP4AtomKeyType.Artwork), //$NON-NLS-2$
	COPYRIGHT("Copyright","cprt",MP4AtomKeyType.String), //$NON-NLS-2$
	ENCODING_TOOL("Encoding Tool","©too",MP4AtomKeyType.String), //$NON-NLS-2$
	ENCODED_BY("Encoded By","©enc",MP4AtomKeyType.String), //$NON-NLS-2$
	PURCHASED_DATE("Purchase Date","purd",MP4AtomKeyType.Date), //$NON-NLS-2$
	PODCAST("Podcast","pcst",MP4AtomKeyType.Boolean), //$NON-NLS-2$
	PODCAST_URL("Podcast URL","purl",MP4AtomKeyType.URL), //$NON-NLS-2$
	KEYWORDS("Keywords","keyw",MP4AtomKeyType.String), //$NON-NLS-2$
	CATEGORY("Category","catg",MP4AtomKeyType.String), //$NON-NLS-2$
	HD("HD Video","hdvd",MP4AtomKeyType.Byte), //$NON-NLS-2$
	MEDIA_TYPE("Media Type","stik",MP4AtomKeyType.Enum), //$NON-NLS-2$
	RATING("Content Rating","rtng",MP4AtomKeyType.Byte), //$NON-NLS-2$
	GAPLESS_PLAYBACK("Gapless Playback","pgap",MP4AtomKeyType.Boolean), //$NON-NLS-2$
	PURCHASE_ACCOUNT("Purchase Account","apID",MP4AtomKeyType.String), //$NON-NLS-2$
	ACCOUNT_TYPE("Account Type","akID",MP4AtomKeyType.Byte), //$NON-NLS-2$
	CATALOG_ID("Catalog ID","cnID"	,MP4AtomKeyType.Integer), //$NON-NLS-2$
	COUNTRY_CODE("Country Code","sfID",MP4AtomKeyType.Integer), //$NON-NLS-2$
	CERTIFICATION("Certification","----",MP4AtomKeyType.String,"iTunEXTC","com.apple.iTunes"), //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	INFO("Movie/Show Information","----",MP4AtomKeyType.String,"iTunMOVI","com.apple.iTunes"), //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	MM_VERSION("MediaManager Version","----",MP4AtomKeyType.String,"mmVer","com.google.code"); //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private String id;
	private String displayName;
	private String dnsName;
	private String dnsDomain;
	private MP4AtomKeyType type;

	private MP4AtomKey(String displayName,String id,MP4AtomKeyType type) {
		this.id = id;
		this.displayName = displayName;
		this.type = type;
	}

	private MP4AtomKey(String displayName,String id,MP4AtomKeyType type,String dnsName,String dnsDomain) {
		this.id = id;
		this.displayName = displayName;
		this.type = type;
		this.dnsName = dnsName;
		this.dnsDomain = dnsDomain;
	}

	public String getId() {
		return id;
	}

	public String getDnsName() {
		return dnsName;
	}

	public String getDnsDomain() {
		return dnsDomain;
	}

	public static MP4AtomKey fromKey(String key) {
		for (MP4AtomKey atom : values()) {
			if (atom.getId().equals(key)) {
				return atom;
			}
		}
		return null;
	}

	public static MP4AtomKey fromRDNS(String name,String domain) {
		for (MP4AtomKey atom : values()) {
			if (atom.getId().equals("----")) {
				if (atom.getDnsName().equals(name) && atom.getDnsDomain().equals(domain)) {
					return atom;
				}
			}
		}
		return null;
	}

	public MP4AtomKeyType getType() {
		return type;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		if (getId().equals("----")) {
			return "----;"+getDnsDomain()+";"+getDnsName();
		}
		else {
			return getId();
		}
	}


}
