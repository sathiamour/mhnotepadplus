package com.android.hinotes;


import java.util.Calendar;
import java.util.Vector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;


public class Alarms
{
	//private static int RowId = -2;
	private static String NoteTitle = ProjectConst.EmptyStr;
	private static long NextAlertTime = Long.MAX_VALUE;


    // Notes with same notify time
	public static Vector<Integer> Notes = new Vector<Integer>(); 
	
    public static void AddOneAlarm(Context ActivityContext){
 
    	 int CurRowId = CalculateNextAlarm(ActivityContext);
    	 int PreRowId = AppSetting.GetIntParam(ActivityContext, AppSetting.Key_Alarm_RowId, ProjectConst.NegativeOne);
    	 Log.d(ProjectConst.TAG, "Alarms: AddOneAlarm, CurRowId "+CurRowId);
    	 // We find one and current one is not the previous one
    	 if( CurRowId != PreRowId && CurRowId != ProjectConst.NegativeOne ) 
    	 {
    		 // If previous one does exist, disable it
    		 if( PreRowId != ProjectConst.NegativeOne )
    		  	 DisableAlter(ActivityContext);
    		 //RowId = CurRowId;
    		 // Enable the alarm time service
    	     EnableAlert(ActivityContext, NextAlertTime, CurRowId);
    	     // Save row id into preference
    	     AppSetting.PutIntParam(ActivityContext, AppSetting.Key_Alarm_RowId, CurRowId);
    	 }
    }
    
    public static boolean UpdateOneAlarm(Context ActivityContext, int NoteRowId){
    	 AddOneAlarm(ActivityContext);
    	 return true;
    }
    
    public static void DeleteOneAlarm(Context ActivityContext, int NoteRowId){
    	// Remove the notify ring time in database
		DisableDbNoteAlarm(ActivityContext, NoteRowId);
		// If delete one is current one, do disable it and set next one
    	if( NoteRowId != ProjectConst.NegativeOne /*&& NoteRowId == RowId*/ )
    	{
    		Log.d("log", "Alarms: delete one alarm "+NoteRowId);
    		DisableAlter(ActivityContext);
    		SetNextAlarm(ActivityContext);
    	}
    }
    
    public static void SetNextAlarm(Context ActivityContext){
    	 //RowId = CalculateNextAlarm(ActivityContext);
    	 int CurRowId = CalculateNextAlarm(ActivityContext);
     	 // We find one
   	     if( CurRowId != ProjectConst.NegativeOne ) 
   	         EnableAlert(ActivityContext, NextAlertTime, CurRowId);
	     // Save row id into preference, if CurRowId == NegativeOne, means there is no next alarm
	     AppSetting.PutIntParam(ActivityContext, AppSetting.Key_Alarm_RowId, CurRowId);

    }
    
    public static void UpdateDbNoteAlarm(Context ActivityContext, int NoteRowId)
    {
    	NoteDbAdapter NotesDb = new NoteDbAdapter(ActivityContext);
        NotesDb.open();
        Cursor Note = NotesDb.GetOneNote(NoteRowId);
        String NotifyRingTime = Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYTIME));
        String Flag = Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_USE_NOTIFYTIME));
        int NotifyDuraIdx = Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYDURA));
        // If not repeat, stop it
        if( !OneNote.IsRepeat(NotifyDuraIdx) )
    	    NotesDb.StopNoteNotify(NoteRowId);   
        else if( Flag.equals(ProjectConst.Yes) ) {    	
        	     // If it has been stopped, do not turn it on, else set next time
        		 Calendar Time = HelperFunctions.String2Calenar(NotifyRingTime);
        		 Time.add(Calendar.MINUTE, OneNote.GetNotifyDura(NotifyDuraIdx));
    	         NotesDb.UpdateNoteNotifyRingTime(NoteRowId, Time);
    	         Log.d("log","Alarms: Next notify time is "+HelperFunctions.Calendar2String(Time)+" row id is "+NoteRowId);
        }
                
    	NotesDb.close();
    }
    
    public static void DisableDbNoteAlarm(Context ActivityContext, int NoteRowId)
    {
    	 NoteDbAdapter NotesDb = new NoteDbAdapter(ActivityContext);
         NotesDb.open();

    	 NotesDb.StopNoteNotify(NoteRowId);

    	 NotesDb.close();
    }
    
    
    private static void EnableAlert(Context ActivityContext, long AlertTime, int CurRowId)
    {
    	 Intent AlertIntent = new Intent(ProjectConst.ALARM_ALERT_ACTION);
    	 AlertIntent.putExtra(OneNote.KEY_ROWID, CurRowId);
         PendingIntent AlertSender = PendingIntent.getBroadcast(ActivityContext, 0, AlertIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 	     AlarmManager NextAlarmMng = (AlarmManager)ActivityContext.getSystemService(Context.ALARM_SERVICE);
 	     NextAlarmMng.set(AlarmManager.RTC_WAKEUP, AlertTime, AlertSender);
 	     // Log
 	     Log.d("log", "Enable alarms: Next alarm is "+CurRowId+ " and title is "+NoteTitle);
    }
    
    private static void DisableAlter(Context ActivityContext){
    	 Intent AlertIntent = new Intent(ProjectConst.ALARM_ALERT_ACTION);
         PendingIntent AlertSender = PendingIntent.getBroadcast(ActivityContext, 0, AlertIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	     // Cancel the alter
	     AlarmManager NextAlarmMng = (AlarmManager)ActivityContext.getSystemService(Context.ALARM_SERVICE);
	     NextAlarmMng.cancel(AlertSender);
	     Log.d("log", "DisableAlter alarm ");
    }
    
    public static int CalculateNextAlarm(Context ActivityContext){
    	
    	NoteDbAdapter NotesDb = new NoteDbAdapter(ActivityContext);
		NotesDb.open();
		
		// use_notifytime = Y 
		// if use_notifytime = N , means the note does not need notify
		String Condition = OneNote.KEY_USE_NOTIFYTIME + "='" + ProjectConst.Yes + "'";
	    Cursor NotesCursor = NotesDb.GetNotesByConditionByOrder(Condition, OneNote.KEY_NOTIFYTIME);
	    int Count = NotesCursor.getCount();
	    Calendar Now = Calendar.getInstance();
	    Notes.clear();
	    long NowInMillis = Now.getTimeInMillis();
	    int CurRowId = ProjectConst.NegativeOne;
	    NextAlertTime = Long.MAX_VALUE;
	    NoteTitle = ProjectConst.EmptyStr;
        
        Log.d("log", "Calculate alarms: number of alarm " + Count);
        Log.d("log", "Now time is "+ HelperFunctions.FormatCalendar2ReadableStr2(Now));
	    for( int i = 0; i < Count; ++i )
	    {
	    	 NotesCursor.moveToPosition(i);
	         Calendar NotifyTime = HelperFunctions.String2Calenar(NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_NOTIFYTIME)));
	         int TmpRowId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID));
	         NotifyTime.set(Calendar.SECOND, 0);
	         NotifyTime.set(Calendar.MILLISECOND, 0);
	         
	         // If notify time has been expired, disable it
	         if( NotifyTime.getTimeInMillis() < NowInMillis ) {
	        	 Log.d("log","Stop notify cur is "+HelperFunctions.FormatCalendar2ReadableStr2(Now)
	        			    +" notifytime is "+HelperFunctions.FormatCalendar2ReadableStr2(NotifyTime));
	        	 NotesDb.StopNoteNotify(TmpRowId);
	         } else if( NextAlertTime > NotifyTime.getTimeInMillis() ) {
	        	 Log.d("log","notify time is "+HelperFunctions.FormatCalendar2ReadableStr2(NotifyTime)+" millis is "+NotifyTime.getTimeInMillis());
	        	 CurRowId = TmpRowId;
	        	 NextAlertTime = NotifyTime.getTimeInMillis();
	             NoteTitle = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
	         }else if( NextAlertTime == NotifyTime.getTimeInMillis() )
	        	 Notes.addElement(TmpRowId);
	    }
	    
	    NotesDb.close();
	    
	    return CurRowId;
    }

}