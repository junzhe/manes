package org.whispercomm.manes.client.macentity.location.sensor;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;
import org.whispercomm.manes.topology.location.GSM;

/**
 * GSM sensor that records the associated GSM cellular tower information.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 */
public class GsmSensor implements LocationSensor<GSM> {

	private static final String TAG = GsmSensor.class.getSimpleName();

	private TelephonyManager telephonyManager;
	private GSM gsmLast;
	private boolean isWorking;

	public GsmSensor(Context context) {
		this.telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		this.isWorking = false;
	}

	public void startSensing() {
		isWorking = true;
	}

	public void stopSensing() {
		isWorking = false;
	}

	public GSM getLatestReading() {
		if (isWorking) {
			// acquire latest sensor measurements
			updateMeasure();
			return gsmLast;
		} else {
			return null;
		}
	}

	/*
	 * Acquire latest sensor measurements.
	 */
	protected void updateMeasure() {
		CellLocation cellLocation = telephonyManager.getCellLocation();
		if (cellLocation instanceof GsmCellLocation) {
			gsmLast = new GSM();
			String mccString = telephonyManager.getSimCountryIso();
			gsmLast.setMcc(mccString);
			gsmLast.setCid(((GsmCellLocation) cellLocation).getCid());
			String mncString = telephonyManager.getSimOperator();
			if (mncString.length() >= 3) {
				try {
					gsmLast.setMnc(Integer.parseInt(mncString.substring(3)));
				} catch (NumberFormatException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			gsmLast.setLac(((GsmCellLocation) cellLocation).getLac());
		} else {
			gsmLast = null;
		}
	}

	public boolean isSensing() {
		return isWorking;
	}

	@Override
	public void setOperator(SensorOperator operator) {
		// We do not have an operator.
	}

	@Override
	public void updateReadings(GSM newReadings) {
		// We do nothing here.
	}

	@Override
	public void startOneMeasureBy(long execTime) {
		// Do nothing here.
		
	}

	@Override
	public void startOneMeasureAt(long eexcTime) {
		// Do nothing here.
		
	}
}
