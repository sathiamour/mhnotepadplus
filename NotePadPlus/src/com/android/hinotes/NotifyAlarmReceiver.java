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
	
	private int NoteRowId = ProjectConst.NegativeOne;
	
	
	@Override
	public void onReceive(Context context, Intent arg1) {
		Bundle Parameters = arg1.getExtras();
		if( ProjectConst.ALARM_KILL_ACTION.equals(arg1.getAction()) ) 
			return;
		
		if( Parameters != null )
		{   			
			// Get parameters
			NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);

			// Open database
			NoteDbAdapter NotesDb = new NoteDbAdapter(context);
			NotesDb.open();
			Cursor Note = NotesDb.GetOneNote(NoteRowId);
			
			// Get notify parameters
			String Title = Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_TITLE));
			int NotifyMethodIdx = Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYMETHOD));
			String Pwd = Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PWD));	
			int Type = Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_NOTETYPE));
			String RingMusic = Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_RINGMUSIC));

		    // Close dialogs and window shade
	        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	        context.sendBroadcast(closeDialogs);
	        
	        // Acquire wake lock, let play notify ring service to get it
	        // NotifyAlarmWakeLock.acquireCpuWakeLock(context);
	        Log.d(ProjectConst.TAG,"We have acquired the cpu wake lock");
	        
            // Disable this alarm if it does not repeat or set new notify time
          	Alarms.UpdateDbNoteAlarm(context, NoteRowId);
          	
            // Enable the next alert if there is one
            Alarms.SetNextAlarm(context);
            
	        // Play the alarm alert and vibrate the device.
	        Log.d(ProjectConst.TAG,"NotifyAlarmReceiver: Play notify ring of Note NO. "+NoteRowId);
	        Intent PlayAlarm = new Intent(ProjectConst.ALARM_NOTIFY_RING);
	        PlayAlarm.putExtra(OneNote.KEY_ROWID, NoteRowId);
	        PlayAlarm.putExtra(OneNote.KEY_RINGMUSIC, RingMusic);
	        PlayAlarm.putExtra(OneNote.KEY_NOTIFYMETHOD, NotifyMethodIdx);
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
	        
	        // Show notification
	        ShowNotification(context, NoteRowId, Title, Pwd, Type);   
	        
	        // Close database
			NotesDb.close();
		}
	}
	
	
	private void ShowNotification(Context context, int NoteRowId, String Title, String Pwd, int Type) {

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon, context.getText(R.string.app_name), System.currentTimeMillis());
		notification.flags |= Notification.FLAG_SHOW_LIGHTS|Notification.FLAG_ONGOING_EVENT;
		// Led light flashing
		notification.ledARGB = Color.GREEN; 
		notification.ledOffMS = 1000; 
		notification.ledOnMS = 300;  

		// Set parameters
		Intent ActivityIntent = null;
		if( Pwd.length() > 0 )
            ActivityIntent = new Intent(context, NotificationPwdDlgActivity.class);
		else {
			if( Type == OneNote.ListNote )
				ActivityIntent = new Intent(context, EditCheckListNoteActivity.class);
	        else if( Type == OneNote.TextNote )
	        	ActivityIntent = new Intent(context, EditNoteActivity.class);
	        else if( Type == OneNote.MultiMediaNote )
	        	ActivityIntent = new Intent(context, EditMultiMediaNoteActivity.class);
		}

		ActivityIntent.putExtra(OneNote.KEY_ROWID, NoteRowId);
		ActivityIntent.putExtra(ProjectConst.KEY_SOURCE, ProjectConst.ALARM_ALERT_ACTION);

		// Show notification
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, ActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), Title, contentIntent);
		// Cancel the same one
		notificationManager.cancel(NoteRowId);
		notificationManager.notify(NoteRowId, notification);
	}
}