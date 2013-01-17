package org.whispercomm.manes.exp.locationsensor.data;

public class GyroFactory implements TimeSeriesFactory<Gyro> {

	@Override
	public Gyro getTimeSeries() {
		return new Gyro();
	}

}
