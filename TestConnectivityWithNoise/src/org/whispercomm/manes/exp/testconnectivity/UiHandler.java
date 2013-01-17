package org.whispercomm.manes.exp.testconnectivity;

import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

public class UiHandler extends Handler {

	/**append a text string to the textView*/
	public void appendToTextView(TextView textView, String msg){
		
		final TextView textViewFinal = textView;
		final String msgFinal = msg;
		
		post(new Runnable(){

			@Override
			public void run() {
				textViewFinal.append(msgFinal);
				
			}
			
		});
	}
	
	/** set a button enabled/dis-enabled*/
	public void enableButton(Button button, boolean enabled){
		
		final Button buttonFinal = button;
		final boolean enabledFinal = enabled;
		
		post(new Runnable(){

			@Override
			public void run() {
				buttonFinal.setEnabled(enabledFinal);
				
			}
			
		});
	}
	
}
