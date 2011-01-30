package org.stanwood.media.util;

import java.util.Iterator;

import org.w3c.dom.Node;

public class IterableNodeList implements org.w3c.dom.NodeList, Iterable<Node> {

	private org.w3c.dom.NodeList list;
	private int pos = 0;

	public IterableNodeList(org.w3c.dom.NodeList list) {
		this.list = list;
	}

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

	@Override
	public Node item(int index) {
		return list.item(index);
	}

	@Override
	public int getLength() {
		return list.getLength();
	}

}
