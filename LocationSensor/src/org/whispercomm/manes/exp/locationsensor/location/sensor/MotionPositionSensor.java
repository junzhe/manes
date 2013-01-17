package org.whispercomm.manes.exp.locationsensor.location.sensor;

import java.util.concurrent.locks.ReentrantLock;

import org.whispercomm.manes.exp.locationsensor.data.Accelerometer;
import org.whispercomm.manes.exp.locationsensor.data.TimeSeriesFactory;
import org.whispercomm.manes.exp.locationsensor.location.actuator.MotionPositionActuator;
import org.whispercomm.manes.exp.locationsensor.location.operator.SensorOperator;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public abstract class MotionPositionSensor<T> implements LocationSensor<T>,
		SensorEventListener {

	public static final String TAG = Accelerometer.class.getSimpleName();
	public static final long MEASURE_PERIOD = 10000;

	protected T readings;
	protected MotionPositionActuator actuator;
	protected boolean isWorking;
	protected ReentrantLock isWorkingLock;

	public MotionPositionSensor(Context context, Handler handler,
			SensorManager sensorManager, TimeSeriesFactory<T> readingFactory,
			long measureDuration, int sensorType, SensorSignal operatorSignal) {
		this.readings = readingFactory.getTimeSeries();
		this.isWorking = false;
		this.isWorkingLock = new ReentrantLock();
		this.actuator = new MotionPositionActuator(context, handler,
				sensorManager, this, measureDuration, sensorType,
				operatorSignal);
	}
	
	public MotionPositionSensor(Context context, Handler handler,
			SensorManager sensorManager){
		this.isWorking = false;
		this.isWorkingLock = new ReentrantLock();
	}

	@Override
	public void startSensing() {
		isWorkingLock.lock();
		try {
			if (isWorking == false) {
				isWorking = true;
				Log.i(TAG, "******Starting periodic sensing!");
				startPeriodicMeasures(MEASURE_PERIOD);
			}
		} finally {
			isWorkingLock.unlock();
		}
	}

	@Override
	public void stopSensing() {
		isWorkingLock.lock();
		if (isWorking) {
			isWorking = false;
			isWorkingLock.unlock();
			actuator.shutDown();
		} else {
			isWorkingLock.unlock();
		}
	}

	@Override
	public void startPeriodicMeasures(long peirod) {
		isWorkingLock.lock();
		try {
			if (isWorking) {
				if (actuator.startPeriodicMeasures(peirod) == false) {
					actuator.stopPeriodicMeasures();
					actuator.startPeriodicMeasures(peirod);
				}
				Log.i(TAG, "Periodic motion sensor measure is started!");
			}
		} finally {
			isWorkingLock.unlock();
		}
	}

	@Override
	public boolean isSensing() {
		return isWorking;
	}

	@Override
	public void setOperator(SensorOperator operator) {
		actuator.setOperator(operator);
	}

	@Override
	public void updateReadings(T newReadings) {
		// do nothing here
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// do nothing here
	}

}
