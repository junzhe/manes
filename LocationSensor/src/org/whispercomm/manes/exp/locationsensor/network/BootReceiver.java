package org.whispercomm.manes.exp.locationsensor.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives boot event and starts up {@link ManesService}.
 * 
 * @author Yue Liu
 * 
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(
				"org.whispercomm.manes.exp.locationsensor.service"));
	}

}
