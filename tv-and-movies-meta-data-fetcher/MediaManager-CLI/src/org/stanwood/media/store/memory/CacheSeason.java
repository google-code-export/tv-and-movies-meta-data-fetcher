package org.stanwood.media.store.memory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.stanwood.media.model.Episode;
import org.stanwood.media.model.IEpisode;
import org.stanwood.media.model.ISeason;
import org.stanwood.media.model.IShow;
import org.stanwood.media.model.Season;

/**
 * A cached version of the season object for storing the details in memory
 */
public class CacheSeason extends Season {

	private Map<Integer,IEpisode>episodes = new HashMap<Integer,IEpisode>();
	private List<IEpisode> specials = new ArrayList<IEpisode>();
	private ISeason season;


	/**
	 * Used to cache season information
	 * @param show The cached version of the show
	 * @param season The season to cache
	 */
	public CacheSeason(CacheShow show,ISeason season) {
		super(show,season.getSeasonNumber());
		this.season = season;
	}

	/**
	 * Used to get a episode with a given episode number
	 * @param episodeNum The episode number of the episode to fetch
	 * @return The episode
	 */
	public IEpisode getEpisode(int episodeNum) {
		IEpisode episode =episodes.get(episodeNum);
		return episode;
	}

	/**
	 * Used to get a special episode with a given episode number
	 * @param specialNumber The special episode number of the episode to fetch
	 * @return The special episode
	 */
	public IEpisode getSpecial(int specialNumber) {
		return specials.get(specialNumber-1);
	}

	/**
	 * Get all the episodes in the season
	 * @return The episodes in the season
	 */
	public Collection<IEpisode> getEpisodes() {
		return episodes.values();
	}

	/**
	 * Add a special episode too the season
	 * @param episode The special episode too add
	 */
	public void addSepcial(Episode episode) {
		specials.add(episode);
	}

	/**
	 * Get all the special episodes in the season
	 * @return The special episodes in the season
	 */
	public List<IEpisode> getSpecials() {
		return specials;
	}


	/**
	 * Adds a episode to the season
	 * @param episode The episode to add to the season
	 */
	public void addEpisode(IEpisode episode) {
		episodes.put(episode.getEpisodeNumber(),episode);
	}

	/**
	 * Gets the number of episode's in the season
	 * @return The number of episodes in the season
	 */
	public int getEpisodeCount() {
		return episodes.size();
	}

	/** {@inheritDoc} */
	@Override
	public URL getURL() {
		return season.getURL();
	}

	/** {@inheritDoc} */
	@Override
	public void setURL(URL url) {
		season.setURL(url);
	}

	/** {@inheritDoc} */
	@Override
	public int getSeasonNumber() {
		return season.getSeasonNumber();
	}

	/** {@inheritDoc} */
	@Override
	public IShow getShow() {
		return season.getShow();
	}


}
