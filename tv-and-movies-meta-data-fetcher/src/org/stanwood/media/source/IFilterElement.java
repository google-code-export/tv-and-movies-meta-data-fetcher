package org.stanwood.media.source;

import au.id.jericho.lib.html.Element;


/**
 * This is used to filter elements from HTML parsing search results
 */
public interface IFilterElement {

	/**
	 * This should return true if the element is to be accepted in the results
	 * @param element The element to check
	 * @return True to accept the element, or false to reject it
	 */
	public boolean accept(Element element);
}
