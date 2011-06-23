package com.android.notepadplus;


import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class ClearNoteAlarmReceiver extends BroadcastReceiver {
	
	public static String Clear_Note_Action = "com.android.notepadplus.ClearNote";
	@Override
	public void onReceive(Context context, Intent arg1) {
		   NoteDbAdapter NoteDbHelper = new NoteDbAdapter(context);
		   NoteDbHelper.open();       

	       // Check expired notes
		   Calendar Now = Calendar.getInstance(Locale.CHINA);
	       Cursor NotesCursor = NoteDbHelper.GetAllNotes();
	       for( int i = 0; i < NotesCursor.getCount(); ++i )
	       {
	    	    NotesCursor.moveToPosition(i);
	    	    Calendar EndTime = HelperFunctions.String2Calenar(NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_ENDTIME)));
	    	    String UseEndDate = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_USE_ENDTIME));
				String DelNoteExp = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_DELNOTE_EXP));
			    
				if( UseEndDate.equals(ProjectConst.Yes) && DelNoteExp.equals(ProjectConst.Yes) )
				{
					if( HelperFunctions.CmpDatePrefix(Now,EndTime) > 0 ) {
						
						String Path = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_PATH));
						int RowId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID));
						// Delete database record
						NoteDbHelper.DeleteOneNote(RowId);
						// Delete file on local file system(need full path)
						Path = context.getString(R.string.notefile_path_prefix) + Path;
						File noteFile = new File(Path);
						noteFile.delete();
						
			    	    Log.d("log","ClearNoteAlarmReceiver: clear "+i+" note");
					}
				} 
	       }
	       
	       // Tell main activity we have done, main UI is responsible to refresh note list
	       Intent MainActivity = new Intent(NotePadPlus.BROADCAST_REFRESHLIST);
	       context.sendBroadcast(MainActivity);		
	       // Refresh widget view on desktop
	       HelperFunctions.RefreshWidgetNoteList(context,NoteDbHelper.GetAllNotes());
	       // Close database
	       NoteDbHelper.close();
	}
}