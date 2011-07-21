package com.android.notepadplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

class AppSetting
{
	// Preferences name
	public static final String PREFS_NAME = "NotePadPlusSetting";
	
	// Preference key name
	public static final String Key_PrefViewStyle = "ViewStyle";
	public static final String Key_PrefAppCount = "PrefAppCount";
    public static final String Key_PrefFullScreen = "FullScreen_Key";
    public static final String Key_PrefFontSize = "FontSize_Key";
    public static final String Key_PrefOrderBy = "OrderBy_Key";
    public static final String Key_PrefBgClr = "BgClr_Key";
    public static final String Key_PrefItemHeight="ItemHeight_Key";
	// Value for keys
	public static final String ViewStyle_List = "List";
	public static final String ViewStyle_Grid = "Grid";
	public static final String[] VieweStyleVal = {ViewStyle_List, ViewStyle_Grid};
	public static final int[] FontSizeArray = {15,25,35};
	public static final float[] ItemHeightFactor={1.0f, 1.4f, 1.8f};
	// Order by
	public static final String OrderByCreatedTime = "0";
	public static final String OrderByUpdatedTime = "1";
	public static final String OrderByTitelAlpha = "2";
	public static final String OrderByColor = "3";
	public static final String OrderByRank = "4";
	public static final int[] OrderByIcon = { R.drawable.ic_menu_created_time, R.drawable.ic_menu_desk_clock,
                                              android.R.drawable.ic_menu_sort_alphabetically, R.drawable.ic_menu_sort_by_color,
                                              R.drawable.ic_menu_sort_by_rank};
	// App setting holder
	public String ViewStyle;
	public int PrefAppCount;
	public boolean IsFullScreen;
	public String FontSize;
	public String OrderBy;
	public String ItemHeight;
	public int BgClr;
	public static int ScreenOrient;
	
	// SharedPreference instantance 
	private SharedPreferences AppSettings;
	private SharedPreferences PrefSettings;
	
	
	public AppSetting(Context AppContext)
	{
		LoadSetting(AppContext);
		// If user just start the program after install it, do init jobs here
		if( PrefAppCount == 0 )
		    AppInitJobs(AppContext);
	}
	
	public void SaveSetting()
	{
		// Save app's setting
		SharedPreferences.Editor PrefEditor = AppSettings.edit();        
		PrefEditor.putString(Key_PrefViewStyle, ViewStyle); 
		PrefEditor.putInt(Key_PrefAppCount,PrefAppCount+1);
		PrefEditor.commit();
	}
	
	public void LoadSetting(Context AppContext)
	{
		// Get my app settings
		AppSettings = AppContext.getSharedPreferences(PREFS_NAME, 0);
		ViewStyle = AppSettings.getString(Key_PrefViewStyle, ViewStyle_List);
		PrefAppCount = AppSettings.getInt(Key_PrefAppCount, 0);
		// Get sys app preference settings
		PrefSettings = PreferenceManager.getDefaultSharedPreferences(AppContext);   
		IsFullScreen = PrefSettings.getBoolean(Key_PrefFullScreen, false);   
		FontSize = PrefSettings.getString(Key_PrefFontSize, "1");   
		OrderBy = PrefSettings.getString(Key_PrefOrderBy, OrderByUpdatedTime);
		BgClr = PrefSettings.getInt(Key_PrefBgClr, Color.WHITE);
		ItemHeight = PrefSettings.getString(Key_PrefItemHeight, "1");
	}
	
	public boolean IsListView()
	{
	    return ViewStyle.compareTo(ViewStyle_List)==0;
	}

	public boolean IsFullScreen()
	{
		return IsFullScreen;		
	}
	
	public void SetOrderBy(String By)
	{
		// Save it into sys app preference settings
		SharedPreferences.Editor PrefEditor = PrefSettings.edit();        
		PrefEditor.putString(Key_PrefOrderBy, By); 
		PrefEditor.commit();
		// Set value
		OrderBy = By;
	}
	// Init works:
	// 1. set up clear notes alarm
	// 2. check screen orient
	// 3. start screen orient service
	public static void AppInitJobs(Context ActivityContext)
	{
		// Log
		Log.d("log", "AppInitJobs: Start screen orient service");
    	// Check screen oriention
    	AppSetting.ScreenOrient = HelperFunctions.ScreenOrient(ActivityContext);
    	// Setup note clear alarm
    	Alarms.SetNoteClearAlarm(ActivityContext);
    	// Start screen orient service
	    Intent ScreenOrient = new Intent(ScreenOrientService.ScreenOrient_Action);
	    ActivityContext.startService(ScreenOrient);
	}
}