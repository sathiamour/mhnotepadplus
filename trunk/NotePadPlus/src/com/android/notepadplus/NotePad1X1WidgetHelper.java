package com.android.notepadplus;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.text.TextUtils;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;



public class NotePad1X1WidgetHelper extends Activity {
	
	// Notes database
	private NoteDbAdapter NotesDb;
	private Cursor NotesCursor;
	// Current app widget id
	private int AppWidgetId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		setContentView(R.layout.widget_1x1_seldlg);
		
		Bundle Extras = getIntent().getExtras();
		if( Extras != null ) 
			AppWidgetId = Extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		Log.d("log","tha app widget id is "+AppWidgetId);
		// Note database
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();
		NotesCursor = NotesDb.GetAllNotes();
 
		// Show note selection dialog
		showDialog(ProjectConst.SelNote_Prompt_Dlg);
	}
	
	/** When the activity is destroyed, close database */
	@Override
	protected void onDestroy() {
		// Close database
	    NotesDb.close();
	    Log.d("log","in helper onDestroy");
		// Destroy
		super.onDestroy();

	}
	

	public static void DelAppWidgetID(int WidgetID, Context AppContext)
	{
		// Clear widget id in database
		NoteDbAdapter NotesDb = new NoteDbAdapter(AppContext);
		NotesDb.open();
		NotesDb.ClearNoteWidgetID(WidgetID);
		NotesDb.close();
	}
	
	private Dialog BuildNoteSelDlg(Context AppContext, int Title)
	{
        Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(R.drawable.ic_dialog_menu_generic);  
        builder.setTitle(Title);  
        BaseAdapter adapter = new ListItemAdapter(NotesCursor.getCount());  
        DialogInterface.OnClickListener listener =   
            new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialogInterface, int which) {
                            NotesCursor.moveToPosition(which);
                	        if( NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_WIDGETID)) != ProjectConst.Zero )
                	        {
                	        	Log.d("log","widget id is not zero");
                	        	NotesDb.close();
                	            finish();
                	            return;
                	        }
                	       
                	        
                	        int RowId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID));
                	        String Title = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
                            int ClrId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_DRAWABLE_ID));
                            boolean IsLocked = (NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).length()>0);
                	        //WidgetNotes.put(AppWidgetId, Data);
                	        
                	        // Update widget
                	        Log.d("log","in NotePad1X1WidgetHelper to update widget");
                	        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(NotePad1X1WidgetHelper.this);  
                	        RemoteViews remoteViews = new RemoteViews(NotePad1X1WidgetHelper.this.getPackageName(), R.layout.widgetview1x1);
                		    remoteViews.setTextViewText(android.R.id.text1, Title);  
        	                remoteViews.setImageViewBitmap(android.R.id.background, HelperFunctions.GetAlpha1x1Bg(NotePad1X1WidgetHelper.this, ClrId));
        	                
                		    // Check wether it is locked
                		    Intent ActivityIntent;
                		    if( IsLocked ) {
                		    	ActivityIntent = new Intent(NotePad1X1WidgetHelper.this, NotificationPwdDlgActivity.class);
                		    	remoteViews.setViewVisibility(R.id.widget1x1_lock, View.VISIBLE);
                		    } else {
                		    	ActivityIntent = new Intent(NotePad1X1WidgetHelper.this, EditNoteActivity.class);
                		    	remoteViews.setViewVisibility(R.id.widget1x1_lock, View.GONE);
                		    }
                		    ActivityIntent.putExtra(OneNote.KEY_ROWID, RowId);
                		    ActivityIntent.putExtra(EditNoteActivity.KEY_SOURCE, ProjectConst.WIDGET1x1_EDIT_ACTION);
              			    PendingIntent EditNotePendingIntent = PendingIntent.getActivity(NotePad1X1WidgetHelper.this, AppWidgetId, ActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
              			    remoteViews.setOnClickPendingIntent(android.R.id.background, EditNotePendingIntent);
              		
                		    appWidgetManager.updateAppWidget(AppWidgetId, remoteViews);  
                		    
                		    // Update widgetid
                		    NotesDb.SetNoteWidgetID(RowId, AppWidgetId);
                		    NotesDb.close();
                		    // Set returned widget id
                	        Intent resultValue = new Intent();
                	        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetId);
                	        setResult(RESULT_OK, resultValue);
                	    
                	        // Close the dialog
                	        finish();
                	
                }  
            };  
        builder.setAdapter(adapter, listener);  
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();					
				}
        });
        return builder.create();  
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ProjectConst.SelNote_Prompt_Dlg:
			 return BuildNoteSelDlg(NotePad1X1WidgetHelper.this, R.string.widget_selnote_title);
		}
		return null;
	}
	
	class ListItemAdapter extends BaseAdapter {  
	      
		  private int Count;
		  ListItemAdapter(int NotesCount){
			     Count = NotesCount;
		  }
	      @Override  
	      public int getCount() {  
	             return Count;  
	      }  
	  
	      @Override  
	      public Object getItem(int position) {  
	            return null;  
	      }  
	  
	      @Override  
	      public long getItemId(int position) {  
	            return 0;  
	      }  
	  
	      @Override  
	      public View getView(int position, View contentView, ViewGroup parent) {
	            TextView NoteTitle = new TextView(NotePad1X1WidgetHelper.this);  
	            NotesCursor.moveToPosition(position);
	            String Title = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
	            int ClrId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_DRAWABLE_ID));
	            // Set Title
	            NoteTitle.setText(Title);  
	            // Set font size
	            NoteTitle.setTextSize(24);  
	            // Set layout
	            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);  
	            NoteTitle.setLayoutParams(layoutParams);  
	            // Set gravity
	            NoteTitle.setGravity(android.view.Gravity.CENTER_VERTICAL);   
	            // Set font color
	            NoteTitle.setTextColor(Color.BLACK);
	            // Set backgroud
	            NoteTitle.setBackgroundColor(NotePadPlus.ItemBgClr[ClrId]);
	            // Set left & right padding
	            NoteTitle.setPadding(15, 0, 15, 0);  
	            // Set single line
	            NoteTitle.setSingleLine();
	            NoteTitle.setEllipsize(TextUtils.TruncateAt.END);
	            NoteTitle.setMinHeight(50); 
	            return NoteTitle;  
	      }	          
	}  
}
