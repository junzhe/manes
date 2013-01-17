package org.whispercomm.manes.exp.gpsmeasurer;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

/**
 * Wrapper class for conveniently using WakeLock provided by Android
 *
 * @author Yue Liu
 */
public class EasyWakeLock {

    static final String TAG = "org.whispercomm.manes.exp.gpsmeasurer"
            + ".EasyWakeLock";
    static final int DEFAULT_LEVEL = PowerManager.PARTIAL_WAKE_LOCK;
    
    private PowerManager powerManager;
    private final WakeLock wakeLock;

    public EasyWakeLock(Context context) {
        this.powerManager = (PowerManager) context.getSystemService(
                Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(DEFAULT_LEVEL, TAG);
    }
    
    public EasyWakeLock(Context context, int lockLevel) {
        this.powerManager = (PowerManager) context.getSystemService(
                Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(lockLevel, TAG);
    }

    /**
     * Acquire wake lock.
     */
    public void acquire() {
        wakeLock.acquire();
    }

    /**
     * Release wake lock.
     */
    public void release() {
        wakeLock.release();
    }
}
