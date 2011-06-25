package com.android.notepadplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

class AppSetting
{
	// Preferences name
	public static final String PREFS_NAME = "NotePadPlusSetting";
	
	// Preference key name
	public static final String Key_PrefViewStyle = "ViewStyle";
	public static final String Key_PrefAppCount = "PrefAppCount";
    
	// Value for keys
	public static final String ViewStyle_List = "List";
	public static final String ViewStyle_Grid = "Grid";

	// App setting holder
	public String ViewStyle;
	public int PrefAppCount;
	public static int ScreenOrient;
	
	// SharedPreference instantance 
	private SharedPreferences AppSettings;
	
	
	public AppSetting(Context AppContext)
	{
		AppSettings = AppContext.getSharedPreferences(PREFS_NAME, 0);	
		LoadSetting();
		if( PrefAppCount == 0 )
		    AppInitJobs(AppContext);
	}
	
	public void SaveSetting()
	{
		// Save app's setting
		SharedPreferences.Editor PrefEditor = AppSettings.edit();        
		PrefEditor.putString(Key_PrefViewStyle, ViewStyle); 
		PrefEditor.putInt(Key_PrefAppCount,PrefAppCount);
		PrefEditor.commit();
	}
	
	public void LoadSetting()
	{
		ViewStyle = AppSettings.getString(Key_PrefViewStyle, ViewStyle_List);
		PrefAppCount = AppSettings.getInt(Key_PrefAppCount, 0);
		
	}
	
	public boolean IsListView()
	{
	    return ViewStyle.compareTo(ViewStyle_List)==0;
	}

	// Init works:
	// 1. set up clear notes alarm
	// 2. check screen orient
	// 3. start screen orient service
	public static void AppInitJobs(Context ActivityContext)
	{
		// Log
		Log.d("log", "AppInitJobs");
    	// Check screen oriention
    	AppSetting.ScreenOrient = HelperFunctions.ScreenOrient(ActivityContext);
    	// Setup note clear alarm
    	Alarms.SetNoteClearAlarm(ActivityContext);
    	// Start screen orient service
	    Intent ScreenOrient = new Intent(ScreenOrientService.ScreenOrient_Action);
	    ActivityContext.startService(ScreenOrient);
	}
}