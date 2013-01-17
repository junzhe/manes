package org.whispercomm.manes.exp.locationsensor.data;

import org.joda.time.DateTime;

public class HumanReadableTime {

	public static String getCurrentTime() {
		long time = System.currentTimeMillis();
		DateTime dateTime = new DateTime(time);
		return dateTime.toString("yyyy/MM/dd, HH:mm:ss.SSS");
	}

	public static String epochToHTime(long epochInMilli) {
		DateTime dateTime = new DateTime(epochInMilli);
		return dateTime.toString("yyyy/MM/dd, HH:mm:ss.SSS");
	}
}
