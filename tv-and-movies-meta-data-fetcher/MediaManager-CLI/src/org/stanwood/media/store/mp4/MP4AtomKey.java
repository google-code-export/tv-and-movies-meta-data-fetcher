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

/** A Enum used to represent each of the atom keys */
public enum MP4AtomKey {

	/** @nam atom key */
	NAME(Messages.getString("MP4AtomKey.0"),"©nam",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** ©ART atom key */
	ARTIST(Messages.getString("MP4AtomKey.1"),"©ART",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** aART atom key */
	ALBUM_ARTIST(Messages.getString("MP4AtomKey.2"),"aART",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** ©alb atom key */
	ALBUM(Messages.getString("MP4AtomKey.3"),"©alb",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** grup atom key */
	GROUPING(Messages.getString("MP4AtomKey.4"),"grup",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** ©wrt atom key */
	COMPOSER(Messages.getString("MP4AtomKey.5"),"©wrt",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** ©wrt atom key */
	COMMENT(Messages.getString("MP4AtomKey.6"),"©cmt",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** flvr atom key */
	FLAVOUR(Messages.getString("MP4AtomKey.7"),"flvr",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** gnre atom key */
	GENRE_PRE_DEFINED(Messages.getString("MP4AtomKey.8"),"gnre",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** ©gen atom key */
	GENRE_USER_DEFINED(Messages.getString("MP4AtomKey.9"),"©gen",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** ©day atom key */
	RELEASE_DATE(Messages.getString("MP4AtomKey.10"),"©day",MP4AtomKeyType.Date),  //$NON-NLS-1$//$NON-NLS-2$
	/** trkn atom key */
	TRACK_NUMBER(Messages.getString("MP4AtomKey.11"),"trkn",MP4AtomKeyType.Range),  //$NON-NLS-1$//$NON-NLS-2$
	/** disk atom key */
	DISK_NUMBER(Messages.getString("MP4AtomKey.12"),"disk",MP4AtomKeyType.Range),  //$NON-NLS-1$//$NON-NLS-2$
	/** tmpo atom key */
	TEMPO(Messages.getString("MP4AtomKey.13"),"tmpo",MP4AtomKeyType.Short),  //$NON-NLS-1$//$NON-NLS-2$
	/** cpil atom key */
	COMPILATION(Messages.getString("MP4AtomKey.14"),"cpil",MP4AtomKeyType.Boolean),  //$NON-NLS-1$//$NON-NLS-2$
	/** tvsh atom key */
	TV_SHOW_NAME(Messages.getString("MP4AtomKey.15"),"tvsh",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** tven atom key */
	TV_EPISODE_ID(Messages.getString("MP4AtomKey.16"),"tven",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** tvsn atom key */
	TV_SEASON(Messages.getString("MP4AtomKey.17"),"tvsn",MP4AtomKeyType.Integer),  //$NON-NLS-1$//$NON-NLS-2$
	/** tves atom key */
	TV_EPISODE(Messages.getString("MP4AtomKey.18"),"tves",MP4AtomKeyType.Integer),  //$NON-NLS-1$//$NON-NLS-2$
	/** tvnn atom key */
	TV_NETWORK(Messages.getString("MP4AtomKey.19"),"tvnn",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** desc atom key */
	DESCRIPTION(Messages.getString("MP4AtomKey.20"),"desc",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** sdes atom key */
	DESCRIPTION_STORE(Messages.getString("MP4AtomKey.21"),"sdes",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** ldes atom key */
	DESCRIPTION_LONG(Messages.getString("MP4AtomKey.22"),"ldes",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** @lyr atom key */
	LYRICS(Messages.getString("MP4AtomKey.23"),"©lyr",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** sonm atom key */
	SORT_NAME(Messages.getString("MP4AtomKey.24"),"sonm",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** soar atom key */
	SORT_ARTIST(Messages.getString("MP4AtomKey.25"),"soar",MP4AtomKeyType.String), //$NON-NLS-1$//$NON-NLS-2$
	/** soaa atom key */
	SORT_ALBUM_ARTIST(Messages.getString("MP4AtomKey.26"),"soaa",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** soal atom key */
	SORT_ALBUM(Messages.getString("MP4AtomKey.27"),"soal",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** soco atom key */
	SORT_COMPOSER(Messages.getString("MP4AtomKey.28"),"soco",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** sosn atom key */
	SORT_SHOW(Messages.getString("MP4AtomKey.29"),"sosn",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** covr atom key */
	ARTWORK(Messages.getString("MP4AtomKey.30"),"covr",MP4AtomKeyType.Artwork),  //$NON-NLS-1$//$NON-NLS-2$
	/** cprt atom key */
	COPYRIGHT(Messages.getString("MP4AtomKey.31"),"cprt",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** @too atom key */
	ENCODING_TOOL(Messages.getString("MP4AtomKey.32"),"©too",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** @enc atom key */
	ENCODED_BY(Messages.getString("MP4AtomKey.33"),"©enc",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** purd atom key */
	PURCHASED_DATE(Messages.getString("MP4AtomKey.34"),"purd",MP4AtomKeyType.Date),  //$NON-NLS-1$//$NON-NLS-2$
	/** pcst atom key */
	PODCAST(Messages.getString("MP4AtomKey.35"),"pcst",MP4AtomKeyType.Boolean),  //$NON-NLS-1$//$NON-NLS-2$
	/** purl atom key */
	PODCAST_URL(Messages.getString("MP4AtomKey.36"),"purl",MP4AtomKeyType.URL),  //$NON-NLS-1$//$NON-NLS-2$
	/** keyw atom key */
	KEYWORDS(Messages.getString("MP4AtomKey.37"),"keyw",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** catg atom key */
	CATEGORY(Messages.getString("MP4AtomKey.38"),"catg",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** hdvd atom key */
	HD(Messages.getString("MP4AtomKey.39"),"hdvd",MP4AtomKeyType.Byte),  //$NON-NLS-1$//$NON-NLS-2$
	/** stik atom key */
	MEDIA_TYPE(Messages.getString("MP4AtomKey.40"),"stik",MP4AtomKeyType.Enum),  //$NON-NLS-1$//$NON-NLS-2$
	/** rtng atom key */
	RATING(Messages.getString("MP4AtomKey.41"),"rtng",MP4AtomKeyType.Byte),  //$NON-NLS-1$//$NON-NLS-2$
	/** pgap atom key */
	GAPLESS_PLAYBACK(Messages.getString("MP4AtomKey.42"),"pgap",MP4AtomKeyType.Boolean),  //$NON-NLS-1$//$NON-NLS-2$
	/** apID atom key */
	PURCHASE_ACCOUNT(Messages.getString("MP4AtomKey.43"),"apID",MP4AtomKeyType.String),  //$NON-NLS-1$//$NON-NLS-2$
	/** akID atom key */
	ACCOUNT_TYPE(Messages.getString("MP4AtomKey.44"),"akID",MP4AtomKeyType.Byte),  //$NON-NLS-1$//$NON-NLS-2$
	/** cnID atom key */
	CATALOG_ID(Messages.getString("MP4AtomKey.45"),"cnID"	,MP4AtomKeyType.Integer),  //$NON-NLS-1$//$NON-NLS-2$
	/** sfID atom key */
	COUNTRY_CODE(Messages.getString("MP4AtomKey.46"),"sfID",MP4AtomKeyType.Integer),  //$NON-NLS-1$//$NON-NLS-2$
	/** ---- [com.apple.iTunes;iTunEXTC] atom key */
	CERTIFICATION(Messages.getString("MP4AtomKey.47"),"----",MP4AtomKeyType.String,"iTunEXTC","com.apple.iTunes"),  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	/** ---- [com.apple.iTunes;iTunMOVI] atom key */
	INFO(Messages.getString("MP4AtomKey.48"),"----",MP4AtomKeyType.String,"iTunMOVI","com.apple.iTunes"),  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	/** Key for custom atom holding the MediaManager version */
	MM_VERSION(Messages.getString("MP4AtomKey.49"),"----",MP4AtomKeyType.String,"mmVer","com.google.code");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

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

	/**
	 * Used to get the id of the atom
	 * @return The id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Used to get the reverse DNS name of the atom, or null if it does not have one
	 * @return the reverse DNS name of the atom, or null if it does not have one
	 */
	public String getDnsName() {
		return dnsName;
	}

	/**
	 * Used to get the reverse DNS domain of the atom, or null if it does not have one
	 * @return the reverse DNS domain of the atom, or null if it does not have one
	 */
	public String getDnsDomain() {
		return dnsDomain;
	}

	/**
	 * Used to get the key enum value from the text id of the atom
	 * @param key The text id of the atom
	 * @return The key
	 */
	public static MP4AtomKey fromKey(String key) {
		for (MP4AtomKey atom : values()) {
			if (atom.getId().equals(key)) {
				return atom;
			}
		}
		return null;
	}

	/**
	 * Used to get the key enum value from of the atom from it's reverse DNS name and domain
	 * @param name The atom reverse DNS name
	 * @param domain The atom reverse DNS domain
	 * @return The key
	 */
	public static MP4AtomKey fromRDNS(String name,String domain) {
		for (MP4AtomKey atom : values()) {
			if (atom.getId().equals("----")) { //$NON-NLS-1$
				if (atom.getDnsName().equals(name) && atom.getDnsDomain().equals(domain)) {
					return atom;
				}
			}
		}
		return null;
	}

	/**
	 * Used to get the type of the atom
	 * @return The atom type
	 */
	public MP4AtomKeyType getType() {
		return type;
	}

	/**
	 * Used to get a name that can be displayed for the atom
	 * @return The display name of the atom
	 */
	public String getDisplayName() {
		return displayName;
	}

	/** {@inheritDoc} } */
	@Override
	public String toString() {
		if (getId().equals("----")) { //$NON-NLS-1$
			return "----;"+getDnsDomain()+";"+getDnsName(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			return getId();
		}
	}


}
