package org.whispercomm.manes.client.macentity.location.sensor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import com.xtremelabs.robolectric.RobolectricTestRunner;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

@RunWith(RobolectricTestRunner.class)
public class GsmSensorTest {

	private GsmSensor sensor;
	
	@Before
	public void setUp(){
		Context context = new Activity();
		sensor = new GsmSensor(context);
	}
	
	@Test
	public void testGetLatestReadingCdma(){
		TelephonyManager manager = mock(TelephonyManager.class);
		when(manager.getCellLocation()).thenReturn(new CdmaCellLocation());
		when(manager.getSimOperator()).thenReturn("asd123");
		Whitebox.setInternalState(sensor, TelephonyManager.class, manager);
		sensor.startSensing();
		assertTrue(sensor.getLatestReading() == null);
		sensor.stopSensing();
	}
	
	@Test
	public void testGetLatestReadingGsm(){
		TelephonyManager manager = mock(TelephonyManager.class);
		when(manager.getCellLocation()).thenReturn(new GsmCellLocation());
		when(manager.getSimOperator()).thenReturn("asd123");
		Whitebox.setInternalState(sensor, TelephonyManager.class, manager);
		sensor.startSensing();
		assertTrue(sensor.getLatestReading() != null);
		sensor.stopSensing();
	}
}
