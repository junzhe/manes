package org.whispercomm.manes.exp.locationsensor.location.sensor;

import org.whispercomm.manes.exp.locationsensor.data.HumanReadableTime;
import org.whispercomm.manes.exp.locationsensor.data.MagnetField;
import org.whispercomm.manes.exp.locationsensor.data.MagnetFieldFactory;
import org.whispercomm.manes.exp.locationsensor.data.ThreeDimMeasure;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;

public class MagnetFieldSensor extends MotionPositionSensor<MagnetField> {

	public static final String TAG = MagnetFieldSensor.class.getSimpleName();
	public static final long MEASURE_PERIOD = 15000;
	private static final long MEASURE_DURATION = 100;
	private static final int SENSOR_TYPE = Sensor.TYPE_MAGNETIC_FIELD;
	private static final SensorSignal OPERATOR_SIGNAL = SensorSignal.MAGNET_AVAILABLE;

	public MagnetFieldSensor(Context context, Handler handler,
			SensorManager sensorManager, MagnetFieldFactory readingFactory) {
		super(context, handler, sensorManager, readingFactory,
				MEASURE_DURATION, SENSOR_TYPE, OPERATOR_SIGNAL);
	}

	@Override
	synchronized public MagnetField getLatestReading() {
		// TODO is it a right thing to deplete the record here?
		MagnetField reportReadings = new MagnetField();
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
//			Log.i(TAG, "new magnet readings: " + data.toString());
		}
	}

}
