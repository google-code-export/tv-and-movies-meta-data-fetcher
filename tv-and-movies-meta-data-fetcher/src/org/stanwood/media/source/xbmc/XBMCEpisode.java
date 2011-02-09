package org.stanwood.media.source.xbmc;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.Season;

public class XBMCEpisode extends Episode {

	private Integer displaySeason;
	private Integer displayEpisode;
	private boolean special = false;
	private Season season;

	public XBMCEpisode(int episodeNumber, Season season) {
		super(episodeNumber, season);
		setSeason(season);
	}

	public Integer getDisplaySeason() {
		return displaySeason;
	}

	public void setDisplaySeason(Integer displaySeason) {
		this.displaySeason = displaySeason;
	}

	public Integer getDisplayEpisode() {
		return displayEpisode;
	}

	public void setDisplayEpisode(Integer displayEpisode) {
		this.displayEpisode = displayEpisode;
	}

	@Override
	public boolean isSpecial() {
		return special;
	}

	@Override
	public void setSpecial(boolean special) {
		this.special = special;
	}

	public void setSeason(Season season) {
		this.season = season;
	}

	@Override
	public Season getSeason() {
		return season;
	}


}
