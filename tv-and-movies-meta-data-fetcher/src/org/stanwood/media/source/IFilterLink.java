package org.stanwood.media.source;

import org.stanwood.media.model.Link;

/**
 * This is used to filter links from HTML parsing search results
 */
public interface IFilterLink {

	/**
	 * This should return true if the link is to be accepted in the results
	 * @param link The link to check
	 * @return True to accept the element, or false to reject it
	 */
	public boolean accept(Link link);
}
