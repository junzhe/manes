package org.whispercomm.manes.client.macentity.location.sensor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.whispercomm.manes.client.macentity.location.actuator.GpsActuator;
import org.whispercomm.manes.client.macentity.location.operator.GeneralOperator;
import org.whispercomm.manes.topology.location.GPS;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class GpsSensorTest {

	private GpsSensor gpsSensor;

	@Before
	public void setUp() {
		Context context = new Activity();
		gpsSensor = new GpsSensor(context);
	}

	@Test
	public void testStartSensing() {
		GpsActuator actuator = mock(GpsActuator.class);
		Whitebox.setInternalState(gpsSensor, GpsActuator.class, actuator);
		gpsSensor.startSensing();
		verify(actuator).startOneMeasure(anyLong());
	}

	@Test
	public void testStopSensing() {
		GpsActuator actuator = mock(GpsActuator.class);
		Whitebox.setInternalState(gpsSensor, GpsActuator.class, actuator);
		gpsSensor.startSensing();
		gpsSensor.stopSensing();
		verify(actuator).cancelPendingMeasures();
	}

	@Test
	public void testUpdateReadings() {
		GeneralOperator operator = mock(GeneralOperator.class);
		Whitebox.setInternalState(gpsSensor, GeneralOperator.class, operator);
		gpsSensor.startSensing();
		gpsSensor.setIsFixed(true);
		GPS gps = new GPS(0, 0);
		gpsSensor.setOperator(operator);
		gpsSensor.updateReadings(gps);
		assertTrue(gpsSensor.getLatestReading().isDataTheSame(gps));
		verify(operator).inform((Bundle) anyObject());
	}

}
