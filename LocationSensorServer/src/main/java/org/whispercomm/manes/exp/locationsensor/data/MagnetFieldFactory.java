package org.whispercomm.manes.exp.locationsensor.data;

public class MagnetFieldFactory implements TimeSeriesFactory<MagnetField> {

	@Override
	public MagnetField getTimeSeries() {
		return new MagnetField();
	}

}
