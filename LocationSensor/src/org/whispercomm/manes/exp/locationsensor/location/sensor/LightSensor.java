package org.whispercomm.manes.exp.locationsensor.location.sensor;

import org.whispercomm.manes.exp.locationsensor.data.HumanReadableTime;
import org.whispercomm.manes.exp.locationsensor.data.Light;
import org.whispercomm.manes.exp.locationsensor.data.LightFactory;
import org.whispercomm.manes.exp.locationsensor.data.OneDimMeasure;
import org.whispercomm.manes.exp.locationsensor.location.operator.GeneralOperator.SensorSignal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;

public class LightSensor extends MotionPositionSensor<Light> {

	public static final String TAG = LightSensor.class.getSimpleName();
	public static final long MEASURE_PERIOD = 20000;
	private static final long MEASURE_DURATION = 200;
	private static final int SENSOR_TYPE = Sensor.TYPE_LIGHT;
	private static final SensorSignal OPERATOR_SIGNAL = SensorSignal.LIGHT_AVAILABLE;

	public LightSensor(Context context, Handler handler,
			SensorManager sensorManager, LightFactory readingFactory) {
		super(context, handler, sensorManager, readingFactory,
				MEASURE_DURATION, SENSOR_TYPE, OPERATOR_SIGNAL);
	}

	@Override
	synchronized public Light getLatestReading() {
		// TODO is it a right thing to deplete the record here?
		Light reportReadings = new Light();
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
			OneDimMeasure data = new OneDimMeasure();
			data.setTime(HumanReadableTime.getCurrentTime());
			data.setX(arg0.values[0]);
			readings.getMeasures().add(data);
//			Log.i(TAG, "new light readings: " + data.toString());
		}
	}
}
