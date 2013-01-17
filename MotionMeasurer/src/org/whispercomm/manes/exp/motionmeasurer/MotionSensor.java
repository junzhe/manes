package org.whispercomm.manes.exp.motionmeasurer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MotionSensor implements ManesSensor, SensorEventListener {

	public static final String LOG_NAME = "Motion-location.dat";

	private UiHandler uiHandler;
	//private FileLogger locationLogger;
	private SensorManager sensors;
	private EasyWakeLock wakeLock;

	public MotionSensor(Context context, UiHandler uiHandler) {
		this.uiHandler = uiHandler;
		//this.locationLogger = null;
		this.sensors = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		this.wakeLock = new EasyWakeLock(context);
	}

	@Override
	public void start(int interval) {
		wakeLock.acquire();
		Sensor accel = sensors
				.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER
						| SensorManager.SENSOR_ORIENTATION);
		sensors.registerListener(this, accel,
				interval);
		uiHandler.appendToTerminal("Motion measuring started.");
//		try {
//			locationLogger = new FileLogger(LOG_NAME);
//		} catch (IOException ex) {
//			uiHandler.appendToTerminal("!!!Cannot open log file!!!");
//			Log.e("MotionSensor", ex.getMessage(), ex);
//		}
	}

	@Override
	public void stop() {
		sensors.unregisterListener(this);
		wakeLock.release();
		uiHandler.appendToTerminal("Motion measuring stopped.");
//		if (locationLogger != null) {
//			try {
//				locationLogger.close();
//			} catch (IOException ex) {
//				uiHandler.appendToTerminal("!!!Cannot close log file!!!");
//			}
//		}
	}

	@Override
	public void onAccuracyChanged(android.hardware.Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		String TimeCrt = Long.toString(System.currentTimeMillis());

		String line = TimeCrt + "\t" + Float.toString(event.values[0]) + "\t"
				+ Float.toString(event.values[1]) + "\t"
				+ Float.toString(event.values[2]);
		uiHandler.appendToTerminal(line);
//		try {
//			locationLogger.append(line);
//		} catch (IOException e) {
//			uiHandler
//					.appendToTerminal("!!!Failed to log this new Wifi ScanResult!!!");
//		}
	}

}
