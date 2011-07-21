package com.android.notepadplus;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class ScreenOrientService extends Service {

	public static final String ScreenOrient_Action = "com.android.notepadplus.ScreenOrient";
	
	/** Do change if it turns from portrait to landscape, or verse */
	public BroadcastReceiver ScreenOrientReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent arg1) {
		       if( arg1.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED) ) {
					if( AppSetting.ScreenOrient != context.getResources().getConfiguration().orientation )
					{
						// Log
						Log.d("log","ScreenOrientLayout onReceive: AppSetting.ScreenOrient "+AppSetting.ScreenOrient+ " Current "+context.getResources().getConfiguration().orientation);
						AppSetting.ScreenOrient = context.getResources().getConfiguration().orientation;
						// Open note database 
						NoteDbAdapter mDbHelper = new NoteDbAdapter(context);
					    mDbHelper.open();
					    HelperFunctions.RefreshWidgetNoteList(context, mDbHelper.GetAllNotes());
					    mDbHelper.close();  
					}
				}
	    }	
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	 @Override
	 public int onStartCommand(Intent intent, int flags, int startId) 
	 {
		    registerReceiver(ScreenOrientReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
		    Log.d("log","ScreenOrientService onStartCommand");
		    return START_STICKY;
	 }
	 
	 @Override
	 public void onDestroy() 
	 {
	        unregisterReceiver(ScreenOrientReceiver);
	 }
}
