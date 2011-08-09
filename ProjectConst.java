package com.android.hinotes;

import android.view.Menu;

public class ProjectConst
{
	/** Dialog Identifier */
	public static final int Check_NoteTitle_Dlg = 0;
	public static final int Set_NotifyDate_Dlg = 1;
	public static final int PwdErr_Dlg = 2;
	public static final int OrignalPwdErr_Dlg = 3;
	public static final int PwdEmpty_Prompt_Dlg = 4;
	public static final int DelNote_Prompt_Dlg = 5;
	public static final int NoteHasLock_Dlg = 6;
	public static final int ShareBy_Dlg = 7;
	public static final int SelNote_Prompt_Dlg = 8;
	public static final int Date_Picker_Dlg = 9;
	public static final int Minute_Picker_Dlg = 10;
	public static final int About_Dlg = 11;
	public static final int EidtNote_PwdPrompt_Dlg = 12;
	public static final int DelNote_PwdPrompt_Dlg = 13;
	public static final int ViewStyle_Dlg = 14;
	public static final int SetTag_PwdPrompt_Dlg = 15;
	public static final int OrderBySel_Dlg = 16;

	/** Action id for activity redirection */
	public static final int ACTIVITY_CREATE = 0;
	public static final int ACTIVITY_EDIT = 1;
	public static final int ACTIVITY_SET_TAGCLR = 2;
	public static final int ACTIVITY_SET_PWD = 3;
	public static final int ACTIVITY_CHG_PWD = 4;
	public static final int ACTIVITY_CLR_PWD = 5;
	public static final int ACTIVITY_ENTERPWD_EDIT = 6;
	public static final int ACTIVITY_SETTING = 7;
	public static final int ACTIVITY_FILTER = 8;
	public static final int ACTIVITY_USERDEF = 9;
	public static final int ACTIVITY_SET_NOTIFYTIME = 10;
	public static final int ACTIVITY_SEL_NOTIFYMUSIC = 11;

	/** Bottom Menu id */
	public static final int ITEM0 = Menu.FIRST;
	public static final int ITEM1 = Menu.FIRST + 1;
	public static final int ITEM2 = Menu.FIRST + 2;
	public static final int ITEM3 = Menu.FIRST + 3;
	public static final int ITEM4 = Menu.FIRST + 4;
	public static final int ITEM5 = Menu.FIRST + 5;
	
	/** Actions */
	
    // Alarm action
	public final static String ALARM_ALERT_ACTION = "com.android.hinotes.NotifyALARM_ALERT";
	public final static String ALARM_KILL_ACTION = "com.android.hinotes.NotifyALARM_KILL";
	public final static String ALARM_NOTIFY_RING = "com.android.hinotes.NotifyRing";
    // Widget Action
	public final static String WIDGET1x1_EDIT_ACTION = "com.android.hinotes.Widget_EditNote";
	public final static String WIDGET4x2_SHOWALL_ACTION = "com.android.hinotes.Widget_ShowAllNotes";
	// Activity Action
	public static final String MAIN_EDIT_ATION = "com.android.hinotes.MainEditNote";
	public static final String USERDEF_EDIT_ACTION = "com.android.hinotes.UserDefEdit";
	// Broadcast Action
	public static final String BROADCAST_REFRESHLIST_ACTION = "com.android.hinotes.Refresh_List";
	public static final String BROADCAST_RANKNOTE_ACTION = "com.android.hinotes.RankNote";
	// Service
	public static final String SERVICE_SCREENORIENT_ACTION = "com.android.hinotes.ScreenOrient";
	/** Log identifier */
	public static final String TAG = "log";
    
	/** String const */
	public static final String Yes = "Y";
	public static final String No = "N";
	public static final String EmptyStr = "";
	public static final String True = "true";
	public static final String False = "false";
	public static final String InvalidateDate = "1900-01-01 00:00:01";
	
	/** Integration const */
	public static final int NegativeOne  = -1;
	public static final int Zero = 0;
	public static final int One = 1;
	
	
	/** Note file's ext*/
	public static final String NoteFileExt = ".txt";
	
	
}