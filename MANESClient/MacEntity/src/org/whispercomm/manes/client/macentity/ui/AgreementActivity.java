package org.whispercomm.manes.client.macentity.ui;

import org.whispercomm.manes.client.macentity.R;
import org.whispercomm.manes.client.macentity.terms.AgreementManager;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An Activity for a user to accept the Manes license agreement.
 * <p>
 * If the user has already agreed, this activity immediately finishes with a
 * success code.
 * 
 * @author David R. Bild
 * 
 */
public class AgreementActivity extends Activity {

	private TextView txtTos;
	private Checkable chkAge;
	private Checkable chkRead;
	private Button btnAccept;
	private Button btnDecline;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.agreement_activity);

		/*
		 * If already agreed, skip out of here.
		 */
		if (AgreementManager.hasAgreed(this)) {
			returnPositive();
		} else {
			initializeGUI();
		}

	}

	private void initializeGUI() {
		txtTos = (TextView) findViewById(R.id.txtTos);
		chkAge = (Checkable) findViewById(R.id.chkAge);
		chkRead = (Checkable) findViewById(R.id.chkRead);
		btnAccept = (Button) findViewById(R.id.btnAgree);
		btnDecline = (Button) findViewById(R.id.btnDecline);

		// Render text from html string
		txtTos.setText(Html.fromHtml(getResources().getString(R.string.tos)));

		// Setup button handlers
		btnAccept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				accept();
			}
		});

		btnDecline.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				decline();
			}
		});
	}

	private void returnPositive() {
		setResult(RESULT_OK);
		finish();
	}

	private void returnNegative() {
		setResult(RESULT_CANCELED);
		finish();
	}

	private boolean validateAgreement() {
		if (!chkAge.isChecked()) {
			Toast.makeText(this,
					"Please confirm that you are at least 13 years of age.",
					Toast.LENGTH_LONG).show();
			return false;
		}

		if (!chkRead.isChecked()) {
			Toast.makeText(
					this,
					"Please confirm that you understand and accept the agreement.",
					Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	private void accept() {
		if (validateAgreement()) {
			AgreementManager.recordUserAgreement(this);
			returnPositive();
		}
	}

	private void decline() {
		returnNegative();
	}
}
