package org.whispercomm.manes.client.macentity.location.sensor;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;

import org.whispercomm.manes.client.macentity.location.operator.SensorOperator;
import org.whispercomm.manes.topology.location.CDMA;

/**
 * CDMA sensor that records the associated CDMA cellular tower information.
 * 
 * @author Junzhe Zhang
 * @author Yue Liu
 */
public class CdmaSensor implements LocationSensor<CDMA> {

	private TelephonyManager telephonyManager;
	private CDMA cdmaLast;
	private boolean isWorking;

	public CdmaSensor(Context context) {
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

	public CDMA getLatestReading() {
		if (isWorking) {
			// acquire latest sensor measurements
			updateMeasure();
			return cdmaLast;
		} else {
			return null;
		}
	}

	/*
	 * Acquire latest sensor measurements.
	 */
	protected void updateMeasure() {
		CellLocation cellLocation = telephonyManager.getCellLocation();
		if (cellLocation instanceof CdmaCellLocation) {
			cdmaLast = new CDMA();
			String mccString = telephonyManager.getSimCountryIso();
			cdmaLast.setMcc(mccString);
			cdmaLast.setSid(((CdmaCellLocation) cellLocation).getSystemId());
			cdmaLast.setNid(((CdmaCellLocation) cellLocation).getNetworkId());
			cdmaLast.setBid(((CdmaCellLocation) cellLocation)
					.getBaseStationId());
		} else {
			cdmaLast = null;
		}
	}

	public boolean isSensing() {
		return isWorking;
	}

	@Override
	public void setOperator(SensorOperator operator) {
		// we do not have an operator.
	}

	@Override
	public void updateReadings(CDMA newReadings) {
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
