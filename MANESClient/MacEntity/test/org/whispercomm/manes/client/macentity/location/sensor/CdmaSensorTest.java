package org.whispercomm.manes.client.macentity.location.sensor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CdmaSensorTest {

	private CdmaSensor sensor;
	
	@Before
	public void setUp(){
		Context context = new Activity();
		sensor = new CdmaSensor(context);
	}
	
	@Test
	public void testGetLatestReadingCdma(){
		TelephonyManager manager = mock(TelephonyManager.class);
		when(manager.getCellLocation()).thenReturn(new CdmaCellLocation());
		Whitebox.setInternalState(sensor, TelephonyManager.class, manager);
		sensor.startSensing();
		assertTrue(sensor.getLatestReading() != null);
		sensor.stopSensing();
	}
	
	@Test
	public void testGetLatestReadingGsm(){
		TelephonyManager manager = mock(TelephonyManager.class);
		when(manager.getCellLocation()).thenReturn(new GsmCellLocation());
		Whitebox.setInternalState(sensor, TelephonyManager.class, manager);
		sensor.startSensing();
		assertTrue(sensor.getLatestReading() == null);
		sensor.stopSensing();
	}
}
