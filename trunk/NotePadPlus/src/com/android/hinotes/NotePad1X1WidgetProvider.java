package com.android.hinotes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class NotePad1X1WidgetProvider extends AppWidgetProvider {
	
	@Override
    public void onUpdate(final Context AppContext, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {       
		   NoteDbAdapter Notes = new NoteDbAdapter(AppContext);
		   Notes.open();
		   Cursor WidgetData = Notes.GetWidgetData();
		   int Count = WidgetData.getCount();
		   for( int i = 0; i < Count && WidgetData.moveToNext(); ++i )
		   {
			    // 1x1 widget data
			    int RowId = WidgetData.getInt(WidgetData.getColumnIndexOrThrow(OneNote.KEY_ROWID));
		        String Title = WidgetData.getString(WidgetData.getColumnIndexOrThrow(OneNote.KEY_TITLE));
		        int ClrId = WidgetData.getInt(WidgetData.getColumnIndexOrThrow(OneNote.KEY_DRAWABLE_ID));
		        boolean IsLocked = (WidgetData.getString(WidgetData.getColumnIndexOrThrow(OneNote.KEY_PWD)).length()>0);
				int WidgetID = WidgetData.getInt(WidgetData.getColumnIndexOrThrow(OneNote.KEY_WIDGETID));
				  
			    // Create remote views
	            RemoteViews remoteViews = new RemoteViews(AppContext.getPackageName(), R.layout.widgetview1x1);
                remoteViews.setTextViewText(android.R.id.text1, Title);
                remoteViews.setImageViewBitmap(android.R.id.background, HelperFunctions.GetAlpha1x1Bg(AppContext, R.drawable.bg_note_1x1, 75, 75, ClrId));
                // Add pending intent for clicking to start edit note activity
                Intent ActivityIntent;
                if( IsLocked ) {
    		    	ActivityIntent = new Intent(AppContext, NotificationPwdDlgActivity.class);
    		    	remoteViews.setViewVisibility(R.id.widget1x1_lock, View.VISIBLE);
    		    } else {
    		    	ActivityIntent = new Intent(AppContext, EditNoteActivity.class);
    		    	remoteViews.setViewVisibility(R.id.widget1x1_lock, View.GONE);
    		    }
                ActivityIntent.putExtra(OneNote.KEY_ROWID, RowId);
                ActivityIntent.putExtra(EditNoteActivity.KEY_SOURCE, ProjectConst.WIDGET1x1_EDIT_ACTION);
 			    PendingIntent EditNotePendingIntent = PendingIntent.getActivity(AppContext, WidgetID, ActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 			    remoteViews.setOnClickPendingIntent(android.R.id.background, EditNotePendingIntent);
                // Update
                appWidgetManager.updateAppWidget(WidgetID, remoteViews);   
		   }
		   Notes.close();
    }
 	
    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
    	   Log.d("log","we want to delete app id length is "+appWidgetIds.length + " the id is "+appWidgetIds[0]);
    	   NotePad1X1WidgetHelper.DelAppWidgetID(appWidgetIds[0], context);
    }
}
