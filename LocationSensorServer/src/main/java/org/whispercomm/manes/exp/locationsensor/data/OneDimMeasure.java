package org.whispercomm.manes.exp.locationsensor.data;

public class OneDimMeasure {

	String time;
	double x;

	public OneDimMeasure() {
		super();
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	@Override
	public String toString() {
		return "time:" + time + ", x:" + x;
	}
}
