package com.android.notepadplus;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

/***
 * Hold a wakelock that can be acquired in the AlarmReceiver and
 * released in the AlarmAlert activity
 */
class NotifyAlarmWakeLock {

	private static String ClassName = "NotifyAlarmWakeLock";
    private static PowerManager.WakeLock sCpuWakeLock;

    static void acquireCpuWakeLock(Context context) {
        Log.d("log", "NotifyAlarmWakeLock: Acquiring cpu wake lock");
        if (sCpuWakeLock != null) {
            return;
        }

        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        sCpuWakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP,
                ClassName);
        sCpuWakeLock.acquire();
    }

    static void releaseCpuLock() {
        Log.d("log", "NotifyAlarmWakeLock: Releasing cpu wake lock");
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}