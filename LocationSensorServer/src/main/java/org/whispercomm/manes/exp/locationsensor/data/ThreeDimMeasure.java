package org.whispercomm.manes.exp.locationsensor.data;

public class ThreeDimMeasure {

	String time;
	double x;
	double y;
	double z;

	public ThreeDimMeasure() {
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

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	@Override
	public String toString() {
		return "time:" + time + ", x:" + x + ", y:" + y + ", z:" + z;
	}
}
