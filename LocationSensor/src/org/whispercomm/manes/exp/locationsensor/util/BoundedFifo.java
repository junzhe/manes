package org.whispercomm.manes.exp.locationsensor.util;

import java.util.LinkedList;

/**
 * This class represents a FIFO structure of bounded size.
 * <p>
 * Elements are always added to the tail of the FIFO. When there is not enough
 * space when a new element is added, the head of the FIFO is discarded. The
 * implementation is supported by a doubly-linked list. Operations that index
 * into the list will traverse the list from the beginning or the end, whichever
 * is closer to the specified index. This structure is good for keeping the last
 * X readings of a sensor, for example.
 * 
 * @author Yue Liu
 * 
 * @param <E>
 */
public class BoundedFifo<E> {

	private final int capacity;
	private LinkedList<E> list;

	/**
	 * Constructs a new {@link BoundedFifo}.
	 * 
	 * @param capacity
	 *            the capacity of this {@link BoundedFifo}.
	 */
	public BoundedFifo(int capacity) {
		this.capacity = capacity;
		this.list = new LinkedList<E>();
	}

	/**
	 * Add the given element to the tail of the FIFO. If there is not enough
	 * space, remove the head of the FIFO to make space for the new element.
	 * 
	 * @param element
	 *            the object to be added.
	 */
	public void add(E element) {
		if (list.size() >= capacity)
			list.remove();
		list.add(element);
	}

	/**
	 * Removes all of the elements.
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * Returns true if this list contains the specified element.
	 * 
	 * @param o
	 * @return
	 */
	public boolean contains(Object o) {
		return list.contains(o);
	}

	/**
	 * Returns the element at the specified position in this FIFO.
	 * 
	 * @param index
	 * @return
	 */
	public E get(int index) {
		return list.get(index);
	}

	/**
	 * Retrieves and removes the head (first element) of this FIFO.
	 * 
	 * @return the head of the FIFO.
	 */
	public E remove() {
		return list.remove();
	}

	/**
	 * Replaces the element at the specified position in this FIFO with the
	 * specified element.
	 * 
	 * @param index
	 *            the index to be updated.
	 * @param element
	 *            the replacement element.
	 * @return the old element in the given position.
	 */
	public E set(int index, E element) {
		return list.set(index, element);
	}

	/**
	 * Returns the number of elements in this FIFO.
	 * 
	 * @return
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Return the capacity of this fifo.
	 * 
	 * @return
	 */
	public int getCapacity() {
		return this.capacity;
	}
}
