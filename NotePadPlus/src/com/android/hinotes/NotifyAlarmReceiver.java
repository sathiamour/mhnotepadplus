package com.android.hinotes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class NotifyAlarmReceiver extends BroadcastReceiver {
	
	private String Title = ProjectConst.EmptyStr;
	private int NoteRowId = ProjectConst.NegativeOne;
	private int NotifyMethodIdx = ProjectConst.NegativeOne;
	
	
	@Override
	public void onReceive(Context context, Intent arg1) {
		Bundle Parameters = arg1.getExtras();
		if( ProjectConst.ALARM_KILL_ACTION.equals(arg1.getAction()) ) 
			return;
		
		if( Parameters != null )
		{   
			// Get parameters
			Title = Parameters.getString(OneNote.KEY_TITLE);
			NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
			NotifyMethodIdx = Parameters.getInt(OneNote.KEY_NOTIFYMETHOD);
				
		    // Close dialogs and window shade
	        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	        context.sendBroadcast(closeDialogs);
	        
	        // Acquire wake lock, let play notify ring service to get it
	        // NotifyAlarmWakeLock.acquireCpuWakeLock(context);
	        Log.d(ProjectConst.TAG,"We have acquired the cpu wake lock");
	        // Play the alarm alert and vibrate the device.
	        Log.d(ProjectConst.TAG,"NotifyAlarmReceiver: Play notify ring of Note NO. "+NoteRowId);
	        Intent PlayAlarm = new Intent(ProjectConst.ALARM_NOTIFY_RING);
	        PlayAlarm.putExtras(Parameters);
	        context.startService(PlayAlarm);
	        
	        // Show Dialog if it is ring method
	        // false, not support it right now
	        if( false && OneNote.IsRing(NotifyMethodIdx) )
	        {
                Intent NotifyDlg = new Intent(context, NotifyActivity.class);
                NotifyDlg.putExtras(Parameters);
                NotifyDlg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                context.startActivity(NotifyDlg);
	        }
            
            // Disable this alarm if it does not repeat or set new notify time
          	Alarms.UpdateDbNoteAlarm(context, NoteRowId);
          	
            // Enable the next alert if there is one
            Alarms.SetNextAlarm(context);
	        
	        // Show notifyication
	        ShowNotification(context, NoteRowId, Title);     
		}
	}
	
	
	private void ShowNotification(Context context, int NoteRowId, String Title) {

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon, context.getText(R.string.app_name), System.currentTimeMillis());
		notification.flags |= Notification.FLAG_SHOW_LIGHTS|Notification.FLAG_ONGOING_EVENT;
		// Led light flashing
		notification.ledARGB = Color.GREEN; 
		notification.ledOffMS = 1000; 
		notification.ledOnMS = 300;  

		// Set parameters
		NoteDbAdapter NotesDb = new NoteDbAdapter(context);
		NotesDb.open();
		Cursor Note = NotesDb.GetOneNote(NoteRowId);
		Intent ActivityIntent = null;
		if( Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PWD)).length() > 0 )
            ActivityIntent = new Intent(context, NotificationPwdDlgActivity.class);
		else {
			int Type = Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_NOTETYPE));
			if( Type == OneNote.ListNote )
				ActivityIntent = new Intent(context, EditCheckListNoteActivity.class);
	        else if( Type == OneNote.TextNote )
	        	ActivityIntent = new Intent(context, EditNoteActivity.class);
		}
		ActivityIntent.putExtra(OneNote.KEY_ROWID, NoteRowId);
		ActivityIntent.putExtra(ProjectConst.KEY_SOURCE, ProjectConst.ALARM_ALERT_ACTION);
		NotesDb.close();
		// Show notification
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, ActivityIntent, 0);
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), Title, contentIntent);
		// Cancel the same one
		notificationManager.cancel(NoteRowId);
		notificationManager.notify(NoteRowId, notification);
	}
}