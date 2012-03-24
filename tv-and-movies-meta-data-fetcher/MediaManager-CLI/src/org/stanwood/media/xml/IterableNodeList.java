package org.stanwood.media.xml;

import java.util.Iterator;

import org.w3c.dom.Node;

/**
 * This class is used to convert the {@link org.w3c.dom.NodeList} into something
 * that can be iterated
 */
public class IterableNodeList implements org.w3c.dom.NodeList, Iterable<Node> {

	private org.w3c.dom.NodeList list;
	private int pos = 0;

	public IterableNodeList(org.w3c.dom.NodeList list) {
		this.list = list;
	}

	/**
	 * Get a node iterator
	 * @return the node iterator
	 */
	@Override
	public Iterator<Node> iterator() {
		return new Iterator<Node>() {
			@Override
			public boolean hasNext() {
				if (list==null) {
					return false;
				}
				return pos < list.getLength();
			}

			@Override
			public Node next() {
				return list.item(pos++);
			}


			/**
			 * This does nothing
			 */
			@Override
			public void remove() {
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public Node item(int index) {
		return list.item(index);
	}

	/** {@inheritDoc} */
	@Override
	public int getLength() {
		return list.getLength();
	}

}
