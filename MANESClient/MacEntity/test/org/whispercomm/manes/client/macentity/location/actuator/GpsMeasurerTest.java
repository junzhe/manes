package org.whispercomm.manes.client.macentity.location.actuator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.anyObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.whispercomm.manes.client.macentity.location.actuator.GpsActuator.GpsMeasurer;
import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;
import org.whispercomm.manes.client.macentity.location.sensor.GpsSensor;
import org.whispercomm.manes.client.macentity.util.TimedExecutor;
import org.whispercomm.manes.topology.location.GPS;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;

import com.xtremelabs.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class GpsMeasurerTest {

	private GpsActuator actuator;
	private GpsMeasurer measurer;
	private TimedExecutor executor;
	private SensorOperator operator;
	private Context context;
	private GpsSensor gpsSensor;
	private WakeLock wakeLock;

	@Before
	public void setUp() {
		context = new Activity();
		gpsSensor = mock(GpsSensor.class);
		operator = mock(SensorOperator.class);
		actuator = new GpsActuator(context, gpsSensor, operator);
		measurer = actuator.new GpsMeasurer();
		executor = Whitebox.getInternalState(actuator, TimedExecutor.class);
		wakeLock = mock(WakeLock.class);
		Whitebox.setInternalState(actuator, WakeLock.class, wakeLock);
		AlarmManager alarmManager = mock(AlarmManager.class);
		Whitebox.setInternalState(executor, AlarmManager.class, alarmManager);
	}

	@Test
	public void testMeasure() {
		measurer.measure();
		verify(wakeLock).acquire();
		verify(operator).inform((Bundle) anyObject());
		// stopper is scheduled.
		assertTrue(executor.getNextExecTime() != null);
		Intent intent = new Intent();
		Bundle data = new Bundle();
		Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(-1);
		location.setLongitude(-1);
		data.putParcelable(LocationManager.KEY_LOCATION_CHANGED, location);
		intent.putExtras(data);
		measurer.onReceive(context, intent);
		// the stopper after gps_max_wait_time is canceled.
		verify(gpsSensor).updateReadings((GPS) anyObject());
		verify(wakeLock).release();
		assertTrue(executor.getNextExecTime() == null);
	}
}
