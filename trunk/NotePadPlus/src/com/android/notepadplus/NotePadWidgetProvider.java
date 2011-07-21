package com.android.notepadplus;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

public class NotePadWidgetProvider extends AppWidgetProvider {
	   
	   /** Action for two buttons on desktop */
	   public final static String ACTION_SHOW_ALL_NOTE = "ShowAllNotes";
	   public final static String ACTION_ADD_NOTE = "AddOneNote";
	   /** Number of notes to be showed on desktop */
	   public final static int Widget_Show_Portrait_Slot = 5;
	   public final static int Widget_Show_Landscape_Slot = 3;
	   public static int AppProviderId = 0;
	   /** Database helper */
	   private NoteDbAdapter mDbHelper = null;
	   
      
       @Override
       public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
       {
    	      Log.d("log","NotePadWidgetProvider onUpdate, size is "+appWidgetIds.length+ " the first one is "+appWidgetIds[0]);
    	      // Open note database 
   		      mDbHelper = new NoteDbAdapter(context);
		      mDbHelper.open();       
		      
		      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widgetview4x2);
		      ComponentName thisWidget = new ComponentName(context, NotePadWidgetProvider.class);
		      
		      // Show notes, at most show five. order by time 
		      Cursor notesCursor = mDbHelper.GetAllNotes();
		      int[] NoteTitleViews = {R.id.note1, R.id.note2, R.id.note3, R.id.note4, R.id.note5};
		      int count = notesCursor.getCount();
 		      int Slot = 0;
 		      if( HelperFunctions.ScreenOrient(context) == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT )
 		    	  Slot = Widget_Show_Portrait_Slot;
 		      else
 		    	  Slot = Widget_Show_Landscape_Slot;
 		    
		      for( int i = 0; i < Slot; ++i )
		      {    
		    	   if( i < count )
		    	   {
		               notesCursor.moveToPosition(i);
		               String TmpTitle = notesCursor.getString(notesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
		               String Title = Integer.toString(i+1)+". "+ TmpTitle;//HelperFunctions.TitleCheck(TmpTitle);
		               remoteViews.setTextViewText(NoteTitleViews[i], Title);
		    	   } else
		    		   remoteViews.setTextViewText(NoteTitleViews[i], ProjectConst.EmptyStr);
		      }
		      
              // Add pending intent to main activity
		      Intent showAllNotes = new Intent(context, NotePadPlus.class);
		      showAllNotes.setAction(ACTION_SHOW_ALL_NOTE);
			  PendingIntent showAllNotesPendingIntent = PendingIntent.getActivity(context, 0, showAllNotes, 0);
			  remoteViews.setOnClickPendingIntent(R.id.widgetboard, showAllNotesPendingIntent);
			  			
			  // Update widget
		      appWidgetManager.updateAppWidget(thisWidget, remoteViews); 
		      
    	      // App init jobs, if the user uses widget first
		      if( AppProviderId == 0 )
		    	  AppSetting.AppInitJobs(context);
    	      AppProviderId = appWidgetIds[0];
       }
       
       @Override
       public void onDeleted(Context context, int[] appWidgetIds)
       {
    		  if( mDbHelper != null )
        		  mDbHelper.close(); 
       }
       
}
