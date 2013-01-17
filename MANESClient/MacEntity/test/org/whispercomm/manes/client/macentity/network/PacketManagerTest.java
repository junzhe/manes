package org.whispercomm.manes.client.macentity.network;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispercomm.manes.client.macentity.http.HttpManager;
import org.whispercomm.manes.client.macentity.http.ManesHttpException;
import org.whispercomm.manes.client.macentity.http.ManesTestUtility;

import android.app.Activity;
import android.content.Context;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PacketManagerTest {
	
	PacketManager packetManager;
	
	HttpManager httpManager;
	IdManager idManager;
	
	Context context;
	
	@Before
	public void setUp() {
		this.context = new Activity();
		this.httpManager = new HttpManager();
		this.idManager = new IdManager(httpManager, context);
		this.packetManager = new PacketManager(httpManager, idManager, context);
		
	}
	
	@After
	public void tearDown() {
		this.context = null;
		this.httpManager = null;
		this.idManager = null;
		this.packetManager = null;
	}
	
	@Test
	public void testHandleSinglePacketAsBytes() {
		byte[] packet = ManesTestUtility.generatePacketAsBytes(16);
		packetManager.addQueueUser(0);
		packetManager.handleSinglePacket(0, packet);
		try {
			byte[] received = packetManager.fetchPacket(0, 10);
			assertArrayEquals(packet, received);
			return;
		} catch (InterruptedException e) {
		} catch (ManesHttpException e) {
		}
		fail("Exception thrown");
	}
	
	@Test
	public void testMultipleUsersSameApp() {
		byte[] packetUserA = ManesTestUtility.generatePacketAsBytes(16);
		byte[] packetUserB = ManesTestUtility.generatePacketAsBytes(16);
		// Put two users with app 12
		packetManager.addQueueUser(12);
		packetManager.addQueueUser(12);
		
		// Add the packets to the queue
		packetManager.handleSinglePacket(12, packetUserA);
		packetManager.handleSinglePacket(12, packetUserB);
		
		try {
			byte[] fetchFirst = packetManager.fetchPacket(12, 0);
			packetManager.removeQueueUser(12);
			byte[] fetchSecond = packetManager.fetchPacket(12, 0);
			assertNotNull(fetchSecond);
			assertArrayEquals(packetUserA, fetchFirst);
			assertArrayEquals(packetUserB, fetchSecond);
			packetManager.handleSinglePacket(12, packetUserA);
			packetManager.removeQueueUser(12);
			byte[] failure = packetManager.fetchPacket(12, 0);
			assertNull(failure);
			return;
		} catch (InterruptedException e) {
		} catch (ManesHttpException e) {
		}
		fail("Exception thrown");
	}

}
