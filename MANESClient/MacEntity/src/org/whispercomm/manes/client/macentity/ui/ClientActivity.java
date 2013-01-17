package org.whispercomm.manes.client.macentity.ui;

import org.whispercomm.manes.client.macentity.R;
import org.whispercomm.manes.client.macentity.http.HttpManager;
import org.whispercomm.manes.client.macentity.network.IdManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ClientActivity extends Activity {

	private HttpManager httpManager;
	private DebugManager debugManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		httpManager = new HttpManager();
		IdManager idManager = new IdManager(getApplicationContext());
		debugManager = new DebugManager(idManager, httpManager);
		TextView tv = (TextView) findViewById(R.id.userIdField);
		tv.setText(debugManager.getUserIdDisplay());
		tv = (TextView) findViewById(R.id.addressField);
		tv.setText(debugManager.getServerAddress());
		tv = (TextView) findViewById(R.id.urlField);
		tv.setText(debugManager.getServerBaseUrl());
	}

	public void onClickRegister(View v) {
		if (debugManager.getRegistrationStatus()) {
			Toast.makeText(getApplicationContext(), "Already registered!",
					Toast.LENGTH_SHORT).show();
		} else {
			boolean status = debugManager.registerDevice();
			if (status) {
				TextView tv = (TextView) findViewById(R.id.userIdField);
				tv.setText(debugManager.getUserIdDisplay());
				Toast.makeText(getApplicationContext(),
						"Registration successful!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Registration unsuccessful...", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	@Override
	public void onDestroy() {
		httpManager.shutdown();
		super.onDestroy();
	}
}
