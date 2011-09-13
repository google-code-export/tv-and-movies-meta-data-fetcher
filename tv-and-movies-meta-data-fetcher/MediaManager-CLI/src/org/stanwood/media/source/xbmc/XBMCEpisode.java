package org.stanwood.media.source.xbmc;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.ISeason;

/**
 * The episode type been returned by XBMC addons
 */
public class XBMCEpisode extends Episode {

	private Integer displaySeason;
	private Integer displayEpisode;
	private boolean special = false;
	private ISeason season;

	/**
	 * The constructor
	 * @param episodeNumber The episode number
	 * @param season The season the episode belongs to
	 * @param special Is this a special episode
	 */
	public XBMCEpisode(int episodeNumber, ISeason season,boolean special) {
		super(episodeNumber, season,special);
		setSeason(season);
	}

	/**
	 * Used to get the display season
	 * @return the display season
	 */
	public Integer getDisplaySeason() {
		return displaySeason;
	}

	/**
	 * Used to set the display season
	 * @param displaySeason the display season
	 */
	public void setDisplaySeason(Integer displaySeason) {
		this.displaySeason = displaySeason;
	}

	/**
	 * Get the display episode number
	 * @return The display episode number
	 */
	public Integer getDisplayEpisode() {
		return displayEpisode;
	}

	/**
	 * Used to set the display episode number
	 * @param displayEpisode The display episode number
	 */
	public void setDisplayEpisode(Integer displayEpisode) {
		this.displayEpisode = displayEpisode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSpecial() {
		return special;
	}

	/**
	 * Used to set the season of the episode
	 * @param season the season of the episode
	 */
	public void setSeason(ISeason season) {
		this.season = season;
	}

	/** {@inheritDoc} */
	@Override
	public ISeason getSeason() {
		return season;
	}

	/**
	 * Used to mark this episode as a special
	 * @param special if true, then mark this episode as special
	 */
	public void setSpecial(boolean special) {
		this.special = special;
	}


}
