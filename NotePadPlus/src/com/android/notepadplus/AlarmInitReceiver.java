package com.android.notepadplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmInitReceiver extends BroadcastReceiver {

	/***
     * Sets alarm on ACTION_BOOT_COMPLETED
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (context.getContentResolver() == null) {
            Log.d("log", "AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
        
        if( intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ) {
        	// App Init jobs(Start screen oriented service)
        	AppSetting.AppInitJobs(context);
        	// Setup note notify alarm
            Alarms.SetNextAlarm(context);  
            // Log
            Log.d("log","AlarmInitReceiver: finish all initialization jobs");
        }
    }

}
