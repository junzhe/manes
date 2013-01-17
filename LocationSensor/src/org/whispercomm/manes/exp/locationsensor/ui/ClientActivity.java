package org.whispercomm.manes.exp.locationsensor.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ClientActivity extends Activity {

	public static final String TAG = ClientActivity.class.getSimpleName();

	private LinearLayout ll;
	private Button start;
	private Button stop;
	private boolean startEnable;
	private boolean stopEnable;
	private SharedPreferences sharedRefs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startEnable = true;
		stopEnable = false;

		ll = new LinearLayout(this);
		ll.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		ll.setOrientation(LinearLayout.VERTICAL);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(ll);

		sharedRefs = getSharedPreferences(TAG, Activity.MODE_PRIVATE);

		start = new Button(this);
		start.setText("Start Measuring.");
		ll.addView(start);

		stop = new Button(this);
		stop.setText("Stop Measuring.");
		ll.addView(stop);

		initializeStart();
		initializeStop();
	}

	private void initializeStart() {
		start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "******Start location-sensor service!");
				// make a toast
				Toast toast = Toast.makeText(getApplicationContext(),
						"Location sensor service is started.",
						Toast.LENGTH_LONG);
				toast.show();
				startService(new Intent(
						"org.whispercomm.manes.exp.locationsensor.service"));
				startEnable = false;
				stopEnable = true;
				updateAppState();
			}
		});

	}

	private void initializeStop() {
		stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "******Stop location-sensor service!");
				// make a toast
				Toast toast = Toast.makeText(getApplicationContext(),
						"Location sensor service is stopped.",
						Toast.LENGTH_LONG);
				toast.show();
				stopService(new Intent(
						"org.whispercomm.manes.exp.locationsensor.service"));
				startEnable = true;
				stopEnable = false;
				updateAppState();
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();

		updateAppState();
	}

	private void updateAppState() {
		SharedPreferences.Editor edit = sharedRefs.edit();
		edit.putBoolean("start", startEnable);
		edit.putBoolean("stop", stopEnable);
		edit.commit();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
