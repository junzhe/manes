package org.whispercomm.manes.exp.locationsensor.ui;

import org.whispercomm.manes.exp.locationsensor.http.HttpManager;
import org.whispercomm.manes.exp.locationsensor.http.ManesHttpException;
import org.whispercomm.manes.exp.locationsensor.R;
import org.whispercomm.manes.exp.locationsensor.network.IdManager;
import org.whispercomm.manes.exp.locationsensor.terms.AgreementManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity that registers with the MANES server.
 * <p>
 * If already registered, this activity immediately finishes with a success
 * code.
 * 
 * @author David R. Bild
 * 
 */
public class RegistrationActivity extends Activity {
	private static final String TAG = RegistrationActivity.class
			.getSimpleName();

	private HttpManager httpManager;
	private IdManager idManager;

	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		idManager = new IdManager(this);

		if (idManager.isRegistered()) {
			returnPositive();
			return;
		}

		if (AgreementManager.hasAgreed(this)) {
			doRegistration();
			return;
		}

		Intent intent = new Intent(this, AgreementActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onDestroy() {
		if (httpManager != null) {
			httpManager.shutdown();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (AgreementManager.hasAgreed(this)) {
			doRegistration();
		} else {
			returnNegative();
		}
	}

	private void initializeDialog() {
		dialog = new ProgressDialog(this);
		dialog.setIcon(R.drawable.ic_launcher);
		dialog.setTitle("Registering...");
		dialog.setMessage("Registering with the MANES service.");
		dialog.setIndeterminate(true);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.show();
	}

	private void doRegistration() {
		httpManager = new HttpManager();
		initializeDialog();
		new RegistrationTask().execute();
	}

	private void returnPositive() {
		setResult(RESULT_OK);
		finish();
	}

	private void returnNegative() {
		setResult(RESULT_CANCELED);
		finish();
	}

	private class RegistrationTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				idManager.register(httpManager);
				return true;
			} catch (ManesHttpException e) {
				Log.w(TAG, "Registration failed.", e);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result) {
				returnPositive();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						RegistrationActivity.this);
				builder.setIcon(R.drawable.ic_launcher)
						.setTitle("Registration Failed")
						.setMessage(
								"Unable to register.  Check your network connection and try again later.")
						.setCancelable(false)
						.setNeutralButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										returnNegative();
									}
								});
				builder.create().show();
			}
		}

	}

}
