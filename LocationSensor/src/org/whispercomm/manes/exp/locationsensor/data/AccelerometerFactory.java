package org.whispercomm.manes.exp.locationsensor.data;

public class AccelerometerFactory implements TimeSeriesFactory<Accelerometer> {

	@Override
	public Accelerometer getTimeSeries() {
		return new Accelerometer();
	}

}
