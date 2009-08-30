package org.stanwood.media.source;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.stanwood.media.model.Link;
import org.stanwood.media.search.SearchHelper;

import au.id.jericho.lib.html.Element;
import au.id.jericho.lib.html.HTMLElementName;
import au.id.jericho.lib.html.Segment;

/**
 * This class provides some static functions that help with parseing HTML
 * data.
 */
public class ParseHelper {

	/**
	 * Search the child tags directly below the parent tag for one that
	 * matches the tag name and is accepted by the filter. If the filter is null,
	 * then it's not checked.
	 * @param parent The parent tag
	 * @param tagName The name of the tag we are looking for
	 * @param filter A filter, or null if no filter is to be used.
	 * @return The tag that was found, or null if it could not be found.
	 */
	@SuppressWarnings("unchecked")
	public static Element findFirstChild(Segment parent, String tagName,IFilterElement filter) {
		for (Element child : (List<Element>) parent.getChildElements()) {
			if (child.getName().equals(tagName) && (filter==null || filter.accept(child))) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Search the child tags below the parent tag for one that
	 * matches the tag name and is accepted by the filter. If the filter is null,
	 * then it's not checked.
	 * @param parent The parent tag
	 * @param tagName The name of the tag we are looking for
	 * @param recursive True to check all the children and their children of the parent
	 *                  Recursively.
	 * @param filter A filter, or null if no filter is to be used.
	 *
	 * @return The tag that was found, or null if it could not be found.
	 */
	public static Element findFirstChild(Segment parent, String tagName,boolean recursive,IFilterElement filter) {
		if (recursive) {
			List<Element> tags = findAllElements(parent,tagName,filter);
			if (tags!=null && tags.size()>0) {
				return tags.get(0);
			}
			else
			{
				return null;
			}
		}
		else {
			return findFirstChild(parent, tagName, filter);
		}
	}

	/**
	 * Find all the tags under a parent (recursively check all it's children and their children),
	 * that match the tag name and are accepted by the filter. If the filter is null,
	 * then it's not checked.
	 * @param parent The parent tag
	 * @param tagName The name of the tag we are looking for
	 * @param filter A filter, or null if no filter is to be used.
	 * @return A list of tags that were found
	 */
	@SuppressWarnings("unchecked")
	public static List<Element> findAllElements(Segment parent, String tagName,IFilterElement filter) {
		List<Element> tags = parent.findAllElements(tagName);
		Iterator<Element> it =  tags.iterator();
		while (it.hasNext()) {
			Element child = it.next();
			if (filter!=null && !filter.accept(child)) {
				it.remove();
			}
		}
		return tags;
	}

	/**
	 * Find the first element under a parent (recursively check all it's children and their children),
	 * that match the tag name and are accepted by the filter. If the filter is null,
	 * then it's not checked.
	 * @param parent The parent tag
	 * @param tagName The name of the tag we are looking for
	 * @param filter A filter, or null if no filter is to be used.
	 * @return The element, or null if it could not be found
	 */
	public static Element findFirstElement(Segment parent, String tagName,IFilterElement filter) {
		List<Element> tags =  findAllElements(parent,tagName,filter);
		if (tags!=null && tags.size()>0) {
			return tags.get(0);
		}
		return null;
	}

	/**
	 * This will find all the links under a parent (recursively check all it's children and their children)
	 * that the filter accepts or if the filter is null then all links are returned.
	 * Any links that are found with have the urlPrefix added to the start of them.
	 * @param urlPrefix The string to prefix found links with
	 * @param parent The parent to search under
	 * @param filter The filter, or null if a filter is not to be used
	 * @return The links that were found
	 */
	public static List<Link> getLinks(String urlPrefix,Segment parent, IFilterLink filter) {
		List<Link> links = new ArrayList<Link>();
		for (Element a : findAllElements(parent, HTMLElementName.A, null)) {
			if (a.getAttributeValue("href")!=null) {
				String href = a.getAttributeValue("href");
				String title = a.getTextExtractor().toString();
				Link link = new Link(urlPrefix + href, SearchHelper.decodeHtmlEntities(title));
				if (filter==null || filter.accept(link)) {
					links.add(link);
				}

			}

		}
		return links;
	}
}
