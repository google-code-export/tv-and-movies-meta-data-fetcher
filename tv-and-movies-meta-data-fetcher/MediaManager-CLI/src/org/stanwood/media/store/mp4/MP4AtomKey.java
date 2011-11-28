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

	NAME("Name","©nam",MP4AtomKeyType.String),
	ARTIST("Artist","©ART",MP4AtomKeyType.String),
	ALBUM_ARTIST("Album Artist","aART",MP4AtomKeyType.String),
	ALBUM("Album","©alb",MP4AtomKeyType.String),
	GROUPING("Grouping","grup",MP4AtomKeyType.String),
	COMPOSER("Composer","©wrt",MP4AtomKeyType.String),
	COMMENT("Comment","©cmt",MP4AtomKeyType.String),
	GENRE_PRE_DEFINED("Genre, Pre-defined","gnre",MP4AtomKeyType.String),
	GENRE_USER_DEFINED("Genre, User defined","©gen",MP4AtomKeyType.String),
	RELEASE_DATE("Release Date","©day",MP4AtomKeyType.Date),
	TRACK_NUMBER("Track Number","trkn",MP4AtomKeyType.Range),
	DISK_NUMBER("Disc Number","disk",MP4AtomKeyType.Range),
	TEMPO("Tempo","tmpo",MP4AtomKeyType.Short),
	COMPILATION("Compilation","cpil",MP4AtomKeyType.Boolean),
	TV_SHOW_NAME("TV Show Name","tvsh",MP4AtomKeyType.String),
	TV_EPISODE_ID("TV Episode ID","tven",MP4AtomKeyType.String),
	TV_SEASON("TV Season Number","tvsn",MP4AtomKeyType.Integer),
	TV_EPISODE("TV Episode Number","tves",MP4AtomKeyType.Integer),
	TV_NETWORK("TV Network","tvnn",MP4AtomKeyType.String),
	DESCRIPTION_SHORT("Description","desc",MP4AtomKeyType.String),
	DESCRIPTION_LONG("Long description","ldes",MP4AtomKeyType.String),
	LYRICS("Lyrics","©lyr",MP4AtomKeyType.String),
	SORT_NAME("Sort Name","sonm",MP4AtomKeyType.String),
	SORT_ARTIST("Sort Artist","soar",MP4AtomKeyType.String),
	SORT_ALBUM_ARTIST("Sort Album Artist","soaa",MP4AtomKeyType.String),
	SORT_ALBUM("Sort Album","soal",MP4AtomKeyType.String),
	SORT_COMPOSER("Sort Composer","soco",MP4AtomKeyType.String),
	SORT_SHOW("Sort Show","sosn",MP4AtomKeyType.String),
	ARTWORK("Cover Artwork","covr",MP4AtomKeyType.Artwork),
	COPYRIGHT("Copyright","cprt",MP4AtomKeyType.String),
	ENCODING_TOOL("Encoding Tool","©too",MP4AtomKeyType.String),
	ENCODED_BY("Encoded By","©enc",MP4AtomKeyType.String),
	PURCHASED_DATE("Purchase Date","purd",MP4AtomKeyType.Date),
	PODCAST("Podcast","pcst",MP4AtomKeyType.Boolean),
	PODCAST_URL("Podcast URL","purl",MP4AtomKeyType.URL),
	KEYWORDS("Keywords","keyw",MP4AtomKeyType.String),
	CATEGORY("Category","catg",MP4AtomKeyType.String),
	HD("HD Video","hdvd",MP4AtomKeyType.Boolean),
	MEDIA_TYPE("Media Type","stik",MP4AtomKeyType.Enum),
	RATING("Content Rating","rtng",MP4AtomKeyType.Byte),
	GAPLESS_PLAYBACK("Gapless Playback","pgap",MP4AtomKeyType.Boolean),
	PURCHASE_ACCOUNT("Purchase Account","apID",MP4AtomKeyType.String),
	ACCOUNT_TYPE("Account Type","akID",MP4AtomKeyType.Byte),
	CATALOG_ID("Catalog ID","cnID"	,MP4AtomKeyType.Integer),
	COUNTRY_CODE("Country Code","sfID",MP4AtomKeyType.Integer),
	CERTIFICATION("Certification","----",MP4AtomKeyType.String,"iTunEXTC","com.apple.iTunes"),
	INFO("Movie/Show Information","----",MP4AtomKeyType.String,"iTunMOVI","com.apple.iTunes"),
	MM_VERSION("MediaManager Version","----",MP4AtomKeyType.String,"mmVer","com.google.code");

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
