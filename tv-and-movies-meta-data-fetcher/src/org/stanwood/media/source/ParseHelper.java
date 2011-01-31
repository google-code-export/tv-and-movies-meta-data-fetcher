package org.stanwood.media.source;

import java.util.Iterator;
import java.util.List;

import au.id.jericho.lib.html.Element;
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
	 * This will find all the elements that match a tagName a below a parent element. If recursive is set
	 * to true, then this will search out all children recursively also. If the filter is null, then no
	 * filter will be used, otherwise a the returned elements are filtered with the filter
	 * @param elements The found elements that match the tag name and are accepted by the filter
	 * @param parent The parent to start the search
	 * @param tagName The name of the tag been looked for
	 * @param recursive True to perform a recursive search
	 * @param filter The filter or null if no filter is been used
	 */
	@SuppressWarnings("unchecked")
	public static void findAllElements(List<Element> elements,Element parent, String tagName, boolean recursive, IFilterElement filter) {
		for (Element child : (List<Element>)parent.getChildElements()) {
			if (child.getName().equals(tagName)) {
				if (filter==null || filter.accept(child)) {
					elements.add(child);
				}
			}

			if (recursive) {
				if (!child.getChildElements().isEmpty()) {
					findAllElements(elements,child,tagName,recursive,filter);
				}
			}
		}
	}
}
