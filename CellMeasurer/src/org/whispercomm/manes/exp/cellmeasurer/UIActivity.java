package org.whispercomm.manes.exp.cellmeasurer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UIActivity extends Activity {

	public static final String TAG = "org.whispercomm.manes.exp.cellmeasurer.UIActivity";
    /**
     * The height of the debug terminal in line count.
     */
    static final int TEXTVIEW_HEIGHT_IN_LINE_COUNT = 5;
    
    private LinearLayout ll;
    private Button start;
    private Button stop;
    private TextView hint;
	private EditText editText;
    /**
     * The debug terminal.
     */
    private TextView instructionView;
    private UiHandler uiHandler;
    
    Sensor sensor;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGUI();
        this.uiHandler = new UiHandler(instructionView);
        this.sensor = new CellSensor(this, uiHandler);
    }

    private void initializeGUI() {
        ll = new LinearLayout(this);
        ll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, 
                ViewGroup.LayoutParams.FILL_PARENT));
        ll.setOrientation(LinearLayout.VERTICAL);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(ll);
        
        instructionView = new TextView(this);
        instructionView.setHeight(TEXTVIEW_HEIGHT_IN_LINE_COUNT
                * instructionView.getLineHeight());
        instructionView.setMovementMethod(ScrollingMovementMethod.getInstance());
        ll.addView(instructionView);

        hint = new TextView(this);
		hint.append("Please enter cell scan period in seconds:");
		ll.addView(hint);
		
		editText = new EditText(this);
		ll.addView(editText);
        
        start = new Button(this);
        start.setText("Start Measuring.");
        ll.addView(start);
        start.setEnabled(true);

        stop = new Button(this);
        stop.setText("Stop Measuring.");
        ll.addView(stop);
        stop.setEnabled(false);
        
        initializeStart();
        initializeStop();
    }

    private void initializeStart() {
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	String scanPeriodString = editText.getText().toString();
				int scanPeriod = 0;
				try {
					scanPeriod = Integer.parseInt(scanPeriodString);
				} catch (NumberFormatException e) {
					Log.e(TAG, e.getMessage(), e);
				}
				if (scanPeriod > 0) {
					// start measurement
					sensor.start(scanPeriod);
					// enable stop button
					stop.setEnabled(true);
					// disable start button
					start.setEnabled(false);
					// make a toast
					Toast toast = Toast.makeText(getApplicationContext(),
							"Start cell scanning every " + scanPeriodString
									+ " seconds!", Toast.LENGTH_LONG);
					toast.show();
				} else {
					// make a warning toast
					Toast toast = Toast.makeText(getApplicationContext(),
							"Please input a valid scan period",
							Toast.LENGTH_LONG);
					toast.show();
				}
            }
        });

    }

    private void initializeStop() {
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // stop measurement
                sensor.stop();
                // enable start button
                start.setEnabled(true);
                // disable stop button
                stop.setEnabled(false);
            }
        });

    }
}
