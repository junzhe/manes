package org.whispercomm.manes.client.exp.testconnectivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestConnectivity extends Activity {
    //92 95 
	//svn ci -m "final version"
	String TAG = "TestConnectivity!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
	
	static final int CKTIMEOUT = 1 * 60 * 1000; // in milliseconds
	static final int SCANTIMEOUT = 1000; // in milliseconds
	static final int SCANTIME = 10; // Number of scans per "scan" button press
	static final int PERIOD = 1000;
	static final int SAMPLESCALE = 2;
	
	Handler mHandler;
	LinearLayout ll;
	Button scan;
	Button startAsSender;
	Button startAsReceiver;
	TextView textview;
	EditText edittext;
	
	private InetAddress group;   //multicast group address
    private int port;            //port number
    private MulticastSocket ms;  //multicast socket
    private InetAddress deviceAddr;
	boolean receiverRunning = false;
	boolean isSender;
	boolean isReceiver;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        initializeGUI();
        initializeCallbacks();
        initializeNetwork();
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	//TODO 
    	//Close record file
    	//Close socket
    	ms.close();
    	// Close the receiver thread
    	receiverRunning = false;
    }
    
    private void initializeGUI() {
		ll = new LinearLayout(this);
		ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		ll.setOrientation(LinearLayout.VERTICAL);

		startAsSender = new Button(this);
		startAsSender.setText("Start As Sender");
		ll.addView(startAsSender);
		startAsReceiver = new Button(this);
		startAsReceiver.setText("Start As Receiver");
		ll.addView(startAsReceiver);
		scan = new Button(this);
		scan.setText("Scan");
		ll.addView(scan);

		edittext = new EditText(this);
		ll.addView(edittext);
		textview = new TextView(this);
		ll.addView(textview);
		setContentView(ll);
	}
    
    private void initializeCallbacks() {
		
    	startAsSender.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startAsSender.setEnabled(false);
				startAsReceiver.setEnabled(false);
				isSender = true;
				isReceiver = false;
			}
		});
    	
    	startAsReceiver.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startAsSender.setEnabled(false);
				startAsReceiver.setEnabled(false);
				isSender = false;
				isReceiver = true;
				//scan.setEnabled(false);
				//edittext.setEnabled(false);
				//Spawn the listener thread
		        receiverRunning = true;
		        Thread thread = new Thread(new receiver());
				thread.start();
			}
		});
    	
		scan.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//Start one scan
				Thread thread = new Thread(new scan());
				thread.start();
			}
		});
    } 
    
    //open a multi-cast socket and join the corresponding group
    private int initializeNetwork(){
    	
        //get group address
        try{
        		group = InetAddress.getByName("224.0.0.111");
        }
        catch(UnknownHostException e1){
        	Log.e(TAG,"unknown host");
        	return -1;
        }
        
        //get port number
        port = 62637; //"manes" on cellphone keyboard
        
        //bind to socket
        try{
        	deviceAddr = InetAddress.getByName(getWifiIp());
            NetworkInterface iface = NetworkInterface.getByInetAddress(deviceAddr);
            
        	ms = new MulticastSocket(port);
        	ms.setNetworkInterface(iface);
        	ms.setInterface(deviceAddr);
        	ms.joinGroup(group);
        }catch(Exception el){
        	Log.e(TAG,"cannot bind to socket"
        			+ "\nError: " + el.getMessage());
        	return -1;
        }
        
        return 1;
    }
    
    private String getWifiIp(){
    	WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	int ipAddress = wifiInfo.getIpAddress();
    	String ip = intToIp(ipAddress);
    	
    	Log.e(TAG," current IP address"+ip);
    	
    	return ip;
    }
    
    //get the ip address of wifi card
    public String intToIp(int i) {

    	return (i & 0xFF ) + "." +
         ((i >> 8 ) & 0xFF) + "." +
         ((i >> 16 ) & 0xFF) + "." +
         ( (i >> 24 )& 0xFF) ;

 	}
    
    //The receiving thread
    private class receiver implements Runnable{

		@Override
		public void run() {
			
			//Set timeout to allow periodical check of receiverRunning 
			try {
				ms.setSoTimeout(CKTIMEOUT);
				//ms.setSoTimeout(0);
			} catch (SocketException el) {
				Log.e(TAG, "cannot set socket timeout"
						+ "\nError: " + el.getMessage());
				return;
			}
			byte[] recvBuffer = new byte[16];
			DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
			while(receiverRunning){
				try {
					ms.receive(recvPacket);
				}
	        	catch(SocketTimeoutException el){continue;}
	        	catch(IOException el){
	        		Log.e(TAG, "packet receiption error"
	    					+ "\nError: " + el.getMessage());
	        		break;
	        	}
	        	//int len = recvPacket.getLength();
	        	String message = new String(recvBuffer, 0, 5);
	        	//Log.v(TAG, len + ", " +message);
	        	if(message.compareTo("PROBE") == 0){
	        		//Reply with an "ACK"
	        		byte[] sendBuffer = new String("ACK").getBytes();
	        		DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, group, port);
	        		try {
		    			ms.send(sendPacket);
		    		} catch (IOException el) {
		    			Log.e(TAG,"cannot send ACK"
		            			+ "\nError: " + el.getMessage());
		    			break;
		    		}
	        	}
			}
		}
    	
    }
    
    //Get one scan results
    private void getOneScan(long stTime, String location, int scanTime, int probeRslt, FileOutputStream fos) throws IOException{
		
		WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
		boolean scanResult = wm.startScan();
		String APRslt = "";
		String name = null;
		String address = null;
		int level = 0;
		
		if (scanResult)
		{
			List<ScanResult> scan = wm.getScanResults();
			for (ScanResult result : scan)
			{
				name = result.SSID;
				address = result.BSSID;
				level = result.level;
				APRslt = APRslt + name + " " + address + " " + level + "\n";
			}
			long crtTime = System.currentTimeMillis() - stTime;
			APRslt = "time: " + crtTime + "\nlocation: " + location + "\n" 
			+ "scanIndex: " + scanTime + "\n" + "probRslt: " + probeRslt 
			+ "\n" + APRslt;
        	fos.write(APRslt.getBytes());
        	fos.write(new String("\n").getBytes());
        	final String APRsltTemp = APRslt;
        	mHandler.post(new Runnable() {
				@Override
				public void run() {
					textview.setText(APRsltTemp);
				}		            			            	
        	});
		}
    }
    
    //The scan thread
    private class scan implements Runnable{

		@Override
		public void run() {

            try 
            {
            	long stTime = System.currentTimeMillis();
    			String location = edittext.getText().toString();		
                File sdcard = Environment.getExternalStorageDirectory();
                File dir = new File (sdcard.getAbsolutePath() + "/myApp/WifiAP");
                dir.mkdirs();
                File file = new File(dir, "result.txt");    
            	FileOutputStream fos = new FileOutputStream(file, true);
            	if(isSender == true && isReceiver == false){
            		//Sender
					for(int scanTime = 0; scanTime < SCANTIME; scanTime++)
					{
						int probeRslt = probe();
						if (probeRslt == -1) return;
	
						Thread.sleep(PERIOD);
						getOneScan(stTime, location, scanTime, probeRslt, fos);
					}
            	}
            	if(isSender == false && isReceiver == true){
            		//Receiver
            		for(int scanTime = 0; scanTime < SCANTIME*1; scanTime++)
					{	
						Thread.sleep(PERIOD/1);
						getOneScan(stTime, location, scanTime, -1, fos);
					}
            	}
				fos.close();
			} catch (Exception e) {
				Log.e(TAG,e.toString());
			}
		}
    }
    
    private int probe(){
    	
    	//Send an "PROBE"
		byte[] sendBuffer = new String("PROBE").getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, group, port);
		try {
			ms.send(sendPacket);
		} catch (IOException el) {
			Log.e(TAG,"cannot send PROBE"
					+ "\nError: " + el.getMessage());
			return -1;
		}
    	
		//Debug
//		mHandler.post(new Runnable(){
//			@Override
//			public void run() {
//				Toast.makeText(TestConnectivity.this, "Send PROBE!", Toast.LENGTH_SHORT).show();
//			}
//		});
		
		//Set timeout to allow periodical check of receiverRunning 
		try {
			ms.setSoTimeout(SCANTIMEOUT);
			//ms.setSoTimeout(0);
		} catch (SocketException el) {
			Log.e(TAG, "cannot set socket timeout"
					+ "\nError: " + el.getMessage());
			return -1;
		}
		
    	byte[] recvBuffer = new byte[16];
		DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
		
		while(true)
		{
			try {
				ms.receive(recvPacket);
			}
			catch(SocketTimeoutException el){return 0;}
			catch(IOException el){
				Log.e(TAG, "packet receiption error"
						+ "\nError: " + el.getMessage());
				return -1;
			}
			if (recvPacket.getAddress().equals(deviceAddr)) continue;
			String message = new String(recvBuffer, 0, recvPacket.getLength());
			if(message.compareTo("ACK") == 0){
				return 1;
			}else return 0;
		}
		
    }
}