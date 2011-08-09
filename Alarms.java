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
	// Notes
	private static int RowId = ProjectConst.NegativeOne;
	private static String NoteTitle = ProjectConst.EmptyStr;
	private static long NextAlertTime = Long.MAX_VALUE;
	private static String RingMusic = ProjectConst.EmptyStr;
	private static int NotifyMethodIdx = ProjectConst.NegativeOne;
	

    // Notes with same notify time
	public static Vector<Integer> Notes = new Vector<Integer>(); 
	
    public static void AddOneAlarm(Context ActivityContext){
 
    	 int CurRowId = CalculateNextAlarm(ActivityContext);
    	 Log.d(ProjectConst.TAG, "Alarms: AddOneAlarm, CurRowId "+CurRowId+" RowId  "+RowId);
    	 // We find one and current one is not the previous one
    	 if( CurRowId != RowId && CurRowId != ProjectConst.NegativeOne ) 
    	 {
    		 // If previous one does exist, diable it
    		 if( RowId != ProjectConst.NegativeOne )
    			 DisableAlter(ActivityContext);
    		 RowId = CurRowId;
    	     EnableAlert(ActivityContext, NextAlertTime);
    	 }
    }
    
    public static boolean UpdateOneAlarm(Context ActivityContext, int NoteRowId){
    	 AddOneAlarm(ActivityContext);
    	 return true;
    }
    
    public static void DeleteOneAlarm(Context ActivityContext, int NoteRowId){
    	// Remove the notify ring time in database
		DisableDbNoteAlarm(ActivityContext, NoteRowId);
		// If delete one is current one, do disalbe it and set next one
    	if( NoteRowId != ProjectConst.NegativeOne && NoteRowId == RowId )
    	{
    		Log.d("log", "Alarms: delete one alarm "+NoteRowId);
    		DisableAlter(ActivityContext);
    		SetNextAlarm(ActivityContext);
    	}
    }
    
    public static void SetNextAlarm(Context ActivityContext){
    	 RowId = CalculateNextAlarm(ActivityContext);
   	     if( RowId != ProjectConst.NegativeOne ) // We find one
   	         EnableAlert(ActivityContext, NextAlertTime);
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
    	         NotesDb.UpdateNoteNotifyRingTime(RowId, Time);
    	         Log.d("log","Alarms: Next notify time is "+HelperFunctions.Calendar2String(Time));
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
    
    
    private static void EnableAlert(Context ActivityContext, long AlertTime)
    {
    	 Intent AlertIntent = new Intent(ProjectConst.ALARM_ALERT_ACTION);
    	 AlertIntent.putExtra(OneNote.KEY_ROWID, RowId);
    	 AlertIntent.putExtra(OneNote.KEY_TITLE, NoteTitle);
    	 AlertIntent.putExtra(OneNote.KEY_RINGMUSIC, RingMusic);
    	 AlertIntent.putExtra(OneNote.KEY_NOTIFYMETHOD, NotifyMethodIdx);

         PendingIntent AlertSender = PendingIntent.getBroadcast(ActivityContext, 0, AlertIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 	     // Schedule end date check alarm
 	     AlarmManager NextAlarmMng = (AlarmManager)ActivityContext.getSystemService(Context.ALARM_SERVICE);
 	     NextAlarmMng.set(AlarmManager.RTC_WAKEUP, AlertTime, AlertSender);
 	     // Log
 	     Log.d("log", "Enable alarms: Next alarm is "+RowId+ " and title is "+NoteTitle);
    }
    
    private static void DisableAlter(Context ActivityContext){
    	 Intent AlertIntent = new Intent(ProjectConst.ALARM_ALERT_ACTION);
    	 AlertIntent.putExtra(OneNote.KEY_ROWID, RowId);
    	 AlertIntent.putExtra(OneNote.KEY_TITLE, NoteTitle);
    	 AlertIntent.putExtra(OneNote.KEY_RINGMUSIC, RingMusic);
    	 AlertIntent.putExtra(OneNote.KEY_NOTIFYMETHOD, NotifyMethodIdx);
         PendingIntent AlertSender = PendingIntent.getBroadcast(ActivityContext, 0, AlertIntent, PendingIntent.FLAG_CANCEL_CURRENT);

	     // Cancel the alter
	     AlarmManager NextAlarmMng = (AlarmManager)ActivityContext.getSystemService(Context.ALARM_SERVICE);
	     NextAlarmMng.cancel(AlertSender);
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
        RingMusic = ProjectConst.EmptyStr;
        NotifyMethodIdx = ProjectConst.NegativeOne;
      
        Log.d("log", "Calculate alarms: number of alarm " + Count);
	    for( int i = 0; i < Count; ++i )
	    {
	    	 NotesCursor.moveToPosition(i);
	         Calendar NotifyTime = HelperFunctions.String2Calenar(NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_NOTIFYTIME)));
	         int TmpRowId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID));
	         NotifyTime.set(Calendar.SECOND, 0);
	         NotifyTime.set(Calendar.MILLISECOND, 0);
	         
	         // If notify time has been expired, disable it
	         if( NotifyTime.getTimeInMillis() < NowInMillis )
	        	 NotesDb.StopNoteNotify(TmpRowId);
	         else if( NextAlertTime > NotifyTime.getTimeInMillis() ) {
	        	 CurRowId = TmpRowId;
	        	 NextAlertTime = NotifyTime.getTimeInMillis();
	             NoteTitle = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
	             RingMusic = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_RINGMUSIC));
	             NotifyMethodIdx = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_NOTIFYMETHOD));
	         }else if( NextAlertTime == NotifyTime.getTimeInMillis() )
	        	 Notes.addElement(TmpRowId);
	    }
	    
	    NotesDb.close();
	    
	    return CurRowId;
    }

}