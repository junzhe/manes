package org.whispercomm.manes.exp.gpsmeasurer;

import android.os.Handler;
import android.widget.TextView;

/**
 * UI handler that can be passed to threads and services to post message to 
 * UI thread.
 * 
 * @author Yue Liu
 */
public class UiHandler extends Handler {

    private TextView textView;
    
    public UiHandler(TextView textView){
        this.textView = textView;
    }
    
    /**
     * append a text string to the textView
     */
    public void appendToTerminal(String msg) {
        final String msgFinal = msg;

        post(new Runnable() {
            @Override
            public void run() {
                textView.append(msgFinal + "\n");
            }
        });
    }
}
