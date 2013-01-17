package org.whispercomm.manes.exp.locationsensor.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OneDimTimeSeries {

	List<OneDimMeasure> measures;

	public OneDimTimeSeries() {
		super();
		this.measures = new LinkedList<OneDimMeasure>();
	}

	public List<OneDimMeasure> getMeasures() {
		return measures;
	}

	public void setMeasures(List<OneDimMeasure> measures) {
		this.measures = measures;
	}

	@Override
	public String toString() {
		String out = "";
		Iterator<OneDimMeasure> it = measures.iterator();
		while (it.hasNext()) {
			out += it.next().toString();
		}
		return out;
	}
}
