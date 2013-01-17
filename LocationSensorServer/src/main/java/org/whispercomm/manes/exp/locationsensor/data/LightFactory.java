package org.whispercomm.manes.exp.locationsensor.data;

public class LightFactory implements TimeSeriesFactory<Light> {

	@Override
	public Light getTimeSeries() {
		return new Light();
	}

}
