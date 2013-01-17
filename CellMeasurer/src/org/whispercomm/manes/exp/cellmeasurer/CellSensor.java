package org.whispercomm.manes.exp.cellmeasurer;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class CellSensor implements Sensor {

	public static final String LOG_NAME = "3G-location.dat";
	private TelephonyManager telephonyManager;
	private CellInfo cellInfo;
	private Context context;
	private CellSensorKeepaliveTimer timer;
	private UiHandler uiHandler;
	//private FileLogger locationLogger;
	/**
	 * Scan interval in milliseconds.
	 */
	private long scan_interval;

	public CellSensor(Context context, UiHandler uiHandler) {
		this.context = context;
		this.uiHandler = uiHandler;
		this.timer = new CellSensorKeepaliveTimer(context, this);
		//this.locationLogger = null;
	}

	public void start(int interval) {
		uiHandler.appendToTerminal("3G measuring started.");
		scan_interval = interval * 1000;
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager.getCellLocation() instanceof CdmaCellLocation) {
			cellInfo = new CdmaInfo();
		} else if (telephonyManager.getCellLocation() instanceof GsmCellLocation) {
			cellInfo = new GsmInfo();
		}
//		try {
//			locationLogger = new FileLogger(LOG_NAME);
//		} catch (IOException ex) {
//			uiHandler.appendToTerminal("!!!Cannot open log file!!!");
//		}
		timer.start(this.scan_interval);
	}

	public void stop() {
		uiHandler.appendToTerminal("3G measuring stopped.");
//		try {
//			locationLogger.close();
//		} catch (IOException e) {
//			uiHandler.appendToTerminal("!!!Cannot close log file!!!");
//		}
		timer.stop();
	}

	public void updateInfo() {
		cellInfo.mcc = telephonyManager.getSimCountryIso();
		if (cellInfo instanceof CdmaInfo) {
			CdmaCellLocation cdmacellLocation = (CdmaCellLocation) telephonyManager
					.getCellLocation();
			CdmaInfo cdma = (CdmaInfo) cellInfo;
			cdma.sid = cdmacellLocation.getSystemId();
			cdma.nid = cdmacellLocation.getNetworkId();
			cdma.bid = cdmacellLocation.getBaseStationId();
		} else if (cellInfo instanceof GsmInfo) {
			GsmCellLocation gsmcellLocation = (GsmCellLocation) telephonyManager
					.getCellLocation();
			GsmInfo gsm = (GsmInfo) cellInfo;
			gsm.cid = gsmcellLocation.getCid();
			gsm.mnc = Integer.parseInt(telephonyManager.getSimOperator()
					.substring(3));
			gsm.lac = gsmcellLocation.getLac();
		}
		cellInfo.setIsPrev(false);
	}

	public void getInfo() {
		updateInfo();
		String message = String.valueOf(System.currentTimeMillis()) + "\t"
				+ cellInfo.prepareJSON().toString();
		uiHandler.appendToTerminal("New 3G: " + message);
//		try {
//			locationLogger.append(message);
//		} catch (IOException e) {
//			uiHandler
//					.appendToTerminal("!!!Failed to log this new 3G status!!!");
//		}
	}

}
