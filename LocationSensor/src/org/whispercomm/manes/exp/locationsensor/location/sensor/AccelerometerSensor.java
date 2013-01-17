package org.whispercomm.manes.exp.locationsensor.location.sensor;

import java.util.concurrent.locks.ReentrantLock;

import org.whispercomm.manes.exp.locationsensor.data.HumanReadableTime;
import org.whispercomm.manes.exp.locationsensor.data.ThreeDimMeasure;
import org.whispercomm.manes.exp.locationsensor.data.Accelerometer;
import org.whispercomm.manes.exp.locationsensor.location.actuator.MotionPositionActuator;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;

public class AccelerometerSensor extends MotionPositionSensor<Accelerometer> {

	public static final String TAG = AccelerometerSensor.class.getSimpleName();
	public static final long MEASURE_PERIOD = 20000;
	private static final long MEASURE_DURATION = 200;
	private static final int SENSOR_TYPE = Sensor.TYPE_ACCELEROMETER;
	private static final SensorSignal OPERATOR_SIGNAL = SensorSignal.ACC_AVAILABLE;

	public AccelerometerSensor(Context context, Handler handler,
			SensorManager sensorManager) {
		super(context, handler, sensorManager);
		this.readings = new Accelerometer();
		this.isWorking = false;
		this.isWorkingLock = new ReentrantLock();
		this.actuator = new MotionPositionActuator(context, handler,
				sensorManager, this, MEASURE_DURATION, SENSOR_TYPE,
				OPERATOR_SIGNAL);
	}

	@Override
	synchronized public Accelerometer getLatestReading() {
		Accelerometer reportReadings = new Accelerometer();
		reportReadings.getMeasures().addAll(readings.getMeasures());
		readings.getMeasures().clear();
		return reportReadings;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// do nothing here
	}

	@Override
	synchronized public void onSensorChanged(SensorEvent arg0) {
		if (arg0 != null) {
			ThreeDimMeasure data = new ThreeDimMeasure();
			data.setTime(HumanReadableTime.getCurrentTime());
			data.setX(arg0.values[0]);
			data.setY(arg0.values[1]);
			data.setZ(arg0.values[2]);
			readings.getMeasures().add(data);
			// Log.i(TAG, "new acc readings: " + data.toString());
		}
	}

}
