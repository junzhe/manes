package org.whispercomm.manes.client.macentity.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BoundedFifoTest {

	private BoundedFifo<Object> fifo;

	@Test
	public void testAdd() {
		int capacity = 3;
		fifo = new BoundedFifo<Object>(capacity);
		for (int i = 0; i < 10; i++) {
			fifo.add(Integer.valueOf(i));
			if (i >= 3) {
				for (int j = 0; j < capacity; j++) {
					assertTrue((Integer) fifo.get(capacity - j - 1) - (i - j) == 0);
				}
			}
		}
	}
}
