package org.whispercomm.manes.client.macentity.network;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread-safe linked blocking queue that keeps track of users per queue. A
 * PacketQueue has 0 users upon construction.
 * 
 * @author David Adrian
 * 
 * @param <E>
 *            Object in queue
 */
public class PacketQueue<E> extends LinkedBlockingQueue<E> {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5222653884031586171L;

	private int inUseByCount = 0;

	public PacketQueue() {
		super();
	}

	public PacketQueue(Collection<? extends E> c) {
		super(c);
	}

	public PacketQueue(int capacity) {
		super(capacity);
	}

	synchronized public void addUser() {
		this.inUseByCount++;
	}

	synchronized public void removeUser() {
		this.inUseByCount--;
	}

	public int getUserCount() {
		return this.inUseByCount;
	}

	public boolean isUnused() {
		return this.inUseByCount == 0;
	}
}
