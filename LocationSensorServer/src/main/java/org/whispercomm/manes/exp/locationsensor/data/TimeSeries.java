package org.whispercomm.manes.exp.locationsensor.data;

import java.util.LinkedList;
import java.util.List;

public class TimeSeries<T> {

	List<T> measures;

	public TimeSeries() {
		super();
		this.measures = new LinkedList<T>();
	}

	public List<T> getMeasures() {
		return measures;
	}

	public void setMeasures(List<T> measures) {
		this.measures = measures;
	}
}
