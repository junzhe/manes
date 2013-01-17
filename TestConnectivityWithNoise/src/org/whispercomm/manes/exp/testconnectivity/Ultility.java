package org.whispercomm.manes.exp.testconnectivity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Ultility {

	static final String TAG = "testconnectivity";

	public static String getWifiIp(Activity activity) {
		WifiManager wifiManager = (WifiManager) activity
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = intToIp(ipAddress);

		Log.e(TAG, " current IP address" + ip);

		return ip;
	}

	// get the ip address of wifi card
	public static String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);

	}

	// copy the content of one file to another file (FileOutPutStream)
	static void copyFromFile(File src, FileOutputStream dst) throws IOException {
		FileInputStream in = new FileInputStream(src);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			dst.write(buf, 0, len);
		}
		dst.write(new String("\n").getBytes());
		in.close();
		dst.flush();
	}

	// open a file and get its output stream
	static FileOutputStream openFileStream(String path, String name)
			throws FileNotFoundException {
		FileOutputStream fos;
		File dir = new File(path);
		if (dir.exists() == false)
			dir.mkdirs();
		fos = new FileOutputStream(new File(dir, name), true);
		return fos;
	}

	// write /proc/net/wireless to /sdcard/connectivity/noiseRecord.dat
	public static void recordProcWireless(long time, String location)
			throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec("su");
		DataOutputStream os = new DataOutputStream(p.getOutputStream());
		os.writeBytes("echo \"time: " + time
				+ "\" >> /sdcard/connectivity/noiseRecord.dat\n");
		os.writeBytes("echo \"location: " + location
				+ "\" >> /sdcard/connectivity/noiseRecord.dat\n");
		os.writeBytes("cat /proc/net/wireless >> /sdcard/connectivity/noiseRecord.dat\n");
		os.writeBytes("exit\n");
		os.flush();
	}

}
