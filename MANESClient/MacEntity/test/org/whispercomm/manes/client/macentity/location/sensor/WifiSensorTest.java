package org.whispercomm.manes.client.macentity.location.sensor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.whispercomm.manes.client.macentity.location.actuator.WifiScanner;
import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator;
import org.whispercomm.manes.topology.location.Meas;
import org.whispercomm.manes.topology.location.Wifi;
import org.whispercomm.manes.topology.location.Wifis;

import com.xtremelabs.robolectric.RobolectricTestRunner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

@RunWith(RobolectricTestRunner.class)
public class WifiSensorTest {

	private WifiSensor wifiSensor;

	@Before
	public void setUp() {
		Context context = new Activity();
		wifiSensor = new WifiSensor(context);
	}

	@Test
	public void testStartSensing() {
		WifiScanner actuator = mock(WifiScanner.class);
		Whitebox.setInternalState(wifiSensor, WifiScanner.class, actuator);
		wifiSensor.startSensing();
		verify(actuator).startOneMeasure(anyLong());
	}

	@Test
	public void testStopSensing() {
		WifiScanner actuator = mock(WifiScanner.class);
		Whitebox.setInternalState(wifiSensor, WifiScanner.class, actuator);
		wifiSensor.startSensing();
		wifiSensor.stopSensing();
		verify(actuator).cancelPendingMeasures();
	}

	@Test
	public void testUpdateReadings() {
		GeneralOperator operator = mock(GeneralOperator.class);
		Whitebox.setInternalState(wifiSensor, GeneralOperator.class, operator);
		wifiSensor.startSensing();
		Wifis wifi = new Wifis();
		List<Wifi> wifiList = new LinkedList<Wifi>();
		Wifi wifiInList = new Wifi();
		wifiInList.setAp(0);
		Meas meas = new Meas();
		meas.setFreq(2);
		meas.setRssi(-90);
		wifiInList.setMeas(meas);
		wifiList.add(wifiInList);
		wifi.setWifi(wifiList);
		wifiSensor.setOperator(operator);
		wifiSensor.updateReadings(wifi);
		assertTrue(wifiSensor.getLatestReading().isDataTheSame(wifi));
		verify(operator).inform((Bundle) anyObject());
	}

}
