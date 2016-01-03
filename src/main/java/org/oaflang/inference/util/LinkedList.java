package org.oaflang.inference.util;

import java.util.Iterator;

public class LinkedList<T> implements Iterable<T> {

	private static class Node<T> {
		T val;
		Node<T> next;
	}

	private Node<T> head;

	public LinkedList() {
		this.head = null;
	}

	public LinkedList(Iterable<T> vals) {
		this();

		Node<T> prev = null;
		for (T val : vals) {
			Node<T> node = new Node<>();
			node.val = val;

			if (head == null)
				head = node;
			
			if (prev != null)
				prev.next = node;
			prev = node;
		}
	}

	public LinkedList<T> prepend(T val) {
		Node<T> node = new Node<>();
		node.val = val;
		node.next = head;
		LinkedList<T> l = new LinkedList<>();
		l.head = node;
		return l;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			private Node<T> ptr = head;

			@Override
			public boolean hasNext() {
				return ptr != null;
			}

			@Override
			public T next() {
				T val = ptr.val;
				ptr = ptr.next;
				return val;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
