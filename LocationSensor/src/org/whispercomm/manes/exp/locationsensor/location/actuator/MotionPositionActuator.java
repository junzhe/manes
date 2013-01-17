package org.whispercomm.manes.exp.locationsensor.location.actuator;

import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.location.sensor.MotionPositionSensor;
import org.whispercomm.manes.exp.locationsensor.util.PeriodicExecutor;
import org.whispercomm.manes.exp.locationsensor.util.TimedExecutor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;

@SuppressWarnings("rawtypes")
public class MotionPositionActuator implements SensorActuator {

	public static final String TAG = MotionPositionActuator.class
			.getSimpleName();

	private final SensorManager sensorManager;
	private Sensor systemSensor;
	private final MotionPositionSensor sensor;
	private boolean isStarted;
	private PeriodicExecutor measureExecutor;
	private TimedExecutor stopExecutor;
	private SensorMeasurer measurer;
	private SensorStopper stopper;
	private SensorOperator operator;
	private final long measureDuration;
	private final SensorSignal operatorSignal;
	private final Handler handler;

	public MotionPositionActuator(Context context, Handler handler, SensorManager sensorManager,
			MotionPositionSensor sensor, long measureDuration, int sensorType,
			SensorSignal operatorSignal) {
		this.handler = handler;
		this.measureDuration = measureDuration;
		this.operatorSignal = operatorSignal;
		this.sensorManager = sensorManager;
		this.systemSensor = sensorManager.getDefaultSensor(sensorType);
		this.sensor = sensor;
		this.isStarted = false;
		this.measurer = new SensorMeasurer();
		this.stopper = new SensorStopper();
		this.measureExecutor = new PeriodicExecutor(context, TAG
				+ ".measureexecutor", measurer);
		this.stopExecutor = new TimedExecutor(context);
	}

	@Override
	synchronized public boolean startPeriodicMeasures(long period) {
		if (isStarted)
			return false;
		measureExecutor.start(period, true);
		isStarted = true;
		return true;
	}

	private class SensorMeasurer implements Runnable {

		@Override
		public void run() {
			sensorManager.registerListener(sensor, systemSensor,
					SensorManager.SENSOR_DELAY_GAME, handler);
			stopExecutor.schedule(System.currentTimeMillis() + measureDuration,
					stopper);
		}

	}

	private class SensorStopper implements Runnable {

		@Override
		public void run() {
			sensorManager.unregisterListener(sensor, systemSensor);
			operator.inform(operatorSignal);
		}

	}

	@Override
	public void shutDown() {
		stopPeriodicMeasures();
	}

	@Override
	public void setOperator(SensorOperator operator) {
		this.operator = operator;
	}

	@Override
	synchronized public boolean stopPeriodicMeasures() {
		if (isStarted == false)
			return false;
		measureExecutor.stop();
		isStarted = false;
		return true;
	}

}
