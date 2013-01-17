package org.whispercomm.manes.exp.locationsensor.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ThreeDimTimeSeries {

	List<ThreeDimMeasure> measures;

	public ThreeDimTimeSeries() {
		super();
		this.measures = new LinkedList<ThreeDimMeasure>();
	}

	public List<ThreeDimMeasure> getMeasures() {
		return measures;
	}

	public void setMeasures(List<ThreeDimMeasure> measures) {
		this.measures = measures;
	}
	
	@Override
	public String toString() {
		String out = "";
		Iterator<ThreeDimMeasure> it = measures.iterator();
		while (it.hasNext()) {
			out += it.next().toString();
		}
		return out;
	}
}
