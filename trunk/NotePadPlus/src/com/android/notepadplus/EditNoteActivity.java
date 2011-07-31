package com.android.notepadplus;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import com.android.notepadplus.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.LayoutParams;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.app.Dialog;
import android.app.AlertDialog.Builder;

public class EditNoteActivity extends Activity {
	
	// Dialog id
	private static final int Del_Prompt_Dlg = 2;
	private static final int NoteHasLock_Dlg = 3;
	private static final int ShareBy_Dlg = 4;
	/** Request code */
	private static final int ACTIVITY_SET_NOTIFYTIME = 0;
	private static final int ACTIVITY_SET_TAGCLR = 1;
	
	/**Menu id */
	public static final int ITEM0 = Menu.FIRST;
	public static final int ITEM1 = Menu.FIRST + 1;
	public static final int ITEM2 = Menu.FIRST + 2;
	public static final int ITEM3 = Menu.FIRST + 3;
	public static final int ITEM4 = Menu.FIRST + 4;
	public static final int ITEM5 = Menu.FIRST + 5;
	
	/** Source from notepadplus or notifyalarmreceiver's notification */
	public static final String KEY_SOURCE = "source";
	// Views
	private EditText NoteTitleCtrl = null;
	private EditText NoteBodyCtrl = null;
	private TextView NotifyTimeLabel = null;
	private TextView NoteTitleView = null;
	private Button ChgTagClrBtn = null;
	private ImageButton   EditBtn = null;
	
	// Activity parameters
	private OneNote EditOneNote;
	private String Pre_UseNotifyTime;
	private Calendar Pre_NotifyTime;
	
	// Database
	private NoteDbAdapter NotesDb;
	
	// Width&Height
	private int ScreenHeight;
	private int ScreenWidth;
	
	// Note has been deleted
	boolean IsDeleted;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editnote);

		// Initialize & open database
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();
		
		// Get screen resolution
		DisplayMetrics ScreenMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);
		ScreenHeight = ScreenMetrics.heightPixels;
		ScreenWidth = ScreenMetrics.widthPixels;
		// Note has been deleted
		IsDeleted = false;
		// Get control handler
		NoteTitleCtrl = (EditText)findViewById(R.id.title_content);
		NoteTitleView = (TextView)findViewById(R.id.title);
		NoteBodyCtrl = (EditText)findViewById(R.id.body_content);
		NotifyTimeLabel = (TextView)findViewById(R.id.notifytime_text);
		ChgTagClrBtn = (Button)findViewById(R.id.chgnoteclr);
		EditBtn = (ImageButton)findViewById(R.id.editnotebtn);
		LinearLayout EditPanel = (LinearLayout)findViewById(R.id.editnote_panel);

		// Get parameters from intent
		Bundle Parameters = getIntent().getExtras();
		if( Parameters != null ) {
			// It is from notification
			if( Parameters.getString(KEY_SOURCE).equals(Alarms.ALARM_ALERT_ACTION) )
			{
				// From notify's notification
				// Get note's row id
				int RowId = Parameters.getInt(OneNote.KEY_ROWID);
				// We are from notify alarm, so since it has been in edit note activity
				// Stop playing music service
				// Stop notify alarm and set next one
	    	    Intent StopService = new Intent(Alarms.ALARM_NOTIFY_RING);
		    	stopService(StopService);
				Alarms.DeleteOneAlarm(this, RowId);
				// Initialize the note with database 
				Cursor Note = NotesDb.GetOneNote(RowId);
				EditOneNote = new OneNote(Note);
				// Remove notification on bar
				NotificationManager notificationManager = (NotificationManager)getSystemService(android.content.Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(RowId);
				// Read note's content
				if( EditOneNote.NoteFilePath != null ) 
					EditOneNote.NoteBody = HelperFunctions.ReadTextFile(this, EditOneNote.NoteFilePath);
			} else if( Parameters.getString(KEY_SOURCE).equals(NotePad1X1WidgetHelper.EDIT_WIDGET_ACTION) ) {
				// From one note widget
				// Initialize the note with database 
				Cursor Note = NotesDb.GetOneNote(Parameters.getInt(OneNote.KEY_ROWID));
				if( Note.getCount() == 1 )
				{
				    EditOneNote = new OneNote(Note);
				    // Read note's content
				    if( EditOneNote.NoteFilePath != null ) 
					    EditOneNote.NoteBody = HelperFunctions.ReadTextFile(this, EditOneNote.NoteFilePath);
				} else {
					IsDeleted = true;
					EditOneNote = new OneNote();
					EditOneNote.NoteTitle = getString(R.string.note_hasdel_prompt);
					EditBtn.setEnabled(false);
				}
			} else {
			    // Initialize the note with activity parameters
				EditOneNote = new OneNote(Parameters);
				// Read note's content
				if( EditOneNote.NoteFilePath != null ) 
					EditOneNote.NoteBody = HelperFunctions.ReadTextFile(this, EditOneNote.NoteFilePath);
			}
			// Backup the notify time
			Pre_UseNotifyTime = EditOneNote.Use_NotifyTime;
			Pre_NotifyTime = EditOneNote.NotifyTime;

			// Set control's text
			NoteTitleView.setText(EditOneNote.NoteTitle);
			NoteTitleCtrl.setText(EditOneNote.NoteTitle);
			NoteBodyCtrl.setText(EditOneNote.NoteBody);

			if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) && HelperFunctions.CmpDatePrefix2(EditOneNote.NotifyTime, Calendar.getInstance())>0 )
				NotifyTimeLabel.setText(HelperFunctions.FormatCalendar2ReadableStr(EditOneNote.NotifyTime));
		}

		EditPanel.setBackgroundDrawable(HelperFunctions.CreateTitleBarBg(ScreenWidth, ScreenHeight,  NotePadPlus.ItemBgClr[EditOneNote.ItemBgIdx], NotePadPlus.TagClr[EditOneNote.TagImgIdx]));
		
		// Show tag color change activity
		ChgTagClrBtn.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	Intent intent = new Intent();
				intent.setClass(EditNoteActivity.this, SetTagClrActivity.class);
				startActivityForResult(intent, ACTIVITY_SET_TAGCLR);				
    		}
    	});

		// Eidt button
		EditBtn.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			StartEditMode();
    		}
		});
		
	}
	
	 @Override 
	 public void onBackPressed(){
		 // If the note has been deleted, just return
		 if( IsDeleted )
		 {
			 setResult(RESULT_CANCELED);
			 finish();
			 return;
		 }
		 
  		 // Do trim & check
    	 EditOneNote.NoteTitle = NoteTitleCtrl.getText().toString().trim();
    	 EditOneNote.NoteBody = NoteBodyCtrl.getText().toString().trim();
         // Check title
    	 if( EditOneNote.NoteTitle.length() == 0 )
    	 {
    		 // Title is empty, we prompt it to user and then return
    		 showDialog(ProjectConst.Check_NoteTitle_Dlg);
    		 return;
    	 }

    	 // Save it to original file
    	 HelperFunctions.WriteTextFile(EditNoteActivity.this, EditOneNote.NoteBody, EditOneNote.NoteFilePath);
    	 // Update database record
    	 NotesDb.UpdateOneNote(EditOneNote); 
    	 
    	 // Update the sepcific alarm if user has changed the notify time
    	 if( !Pre_UseNotifyTime.equals(EditOneNote.Use_NotifyTime) )
    	 {
        	 // From Y(use notify) to N(don't use notify), User cancels alarm
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.No) )
    		 {
    		     Log.d("log","EditNoteActivity: delete alarm");
    		     Alarms.DeleteOneAlarm(EditNoteActivity.this, EditOneNote.NoteRowId);
    		 }
    		// From N(don't use notify) to Y(use notify), add one alarm
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
    		 {
    			 Log.d("log","EditNoteActivity: add alarm");
    			 NotesDb.UpdateNoteNotifyRingTime(EditOneNote.NoteRowId, EditOneNote.NotifyTime);
        		 Alarms.AddOneAlarm(EditNoteActivity.this);
    		 }
    	 } else {
    		 // User changes the notify time
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) && HelperFunctions.CmpDatePrefix2(Pre_NotifyTime, EditOneNote.NotifyTime) != 0 )
    		 { 
        		 Log.d("log","EditNoteActivity: update alarm");
    		     NotesDb.UpdateNoteNotifyRingTime(EditOneNote.NoteRowId, EditOneNote.NotifyTime);
                 Alarms.UpdateOneAlarm(EditNoteActivity.this, EditOneNote.NoteRowId);
    	     }
    	 }
    	 
    	 // Refresh widget note list
    	 HelperFunctions.RefreshWidgetNoteList(EditNoteActivity.this, NotesDb.GetAllNotes());
    	 // Refresh 1x1 widget
    	 if( EditOneNote.WidgetId != 0 )
    	     HelperFunctions.Refresh1x1Widget(EditNoteActivity.this, EditOneNote.WidgetId, EditOneNote.NoteTitle, EditOneNote.NoteRowId, EditOneNote.ItemBgIdx, EditOneNote.Password.length()>0);
    	 // Show toast to notify user settings have been saved
 	     Toast.makeText(EditNoteActivity.this, getString(R.string.notesavingtip), Toast.LENGTH_SHORT).show();
         // Return to main activity
 	     setResult(RESULT_OK);
    	 finish();
	}
	 
	/** When the activity is destroyed, close database*/
    @Override
    protected void onDestroy(){
    	if( NotesDb != null )
    		NotesDb.close(); 
    	super.onDestroy();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add(Menu.NONE, ITEM0, 1, "放弃").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(Menu.NONE, ITEM1, 2, "提醒").setIcon(R.drawable.ic_menu_reminder).setEnabled(!IsDeleted);
		menu.add(Menu.NONE, ITEM2, 3, "删除").setIcon(android.R.drawable.ic_menu_delete).setEnabled(!IsDeleted);
		menu.add(Menu.NONE, ITEM3, 4, "锁定").setIcon(R.drawable.ic_menu_lock).setEnabled(!IsDeleted);
		menu.add(Menu.NONE, ITEM4, 5, "编辑").setIcon(android.R.drawable.ic_menu_edit).setEnabled(!IsDeleted);
		menu.add(Menu.NONE, ITEM5, 6, "分享").setIcon(android.R.drawable.ic_menu_share).setEnabled(!IsDeleted);
        return true;
	}
	

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {  
           switch(item.getItemId()) 
           {  
              case ITEM0:
            	   setResult(RESULT_CANCELED);
	               finish();
	               break;
              case ITEM1:
            	   StartNotifyActivity();
            	   break;
              case ITEM2:
            	   showDialog(Del_Prompt_Dlg);
            	   break;
              case ITEM3:
      			   if( EditOneNote.Password.length() == 0 )
      			   {
      			       Intent PwdDlgIntent = new Intent(this, PwdDlgActivity.class);
      			       PwdDlgIntent.putExtra(OneNote.KEY_ROWID, EditOneNote.NoteRowId);
      			       startActivity(PwdDlgIntent);
                   } else
      			       showDialog(NoteHasLock_Dlg);
            	   break;
              case ITEM4:
            	   StartEditMode();
            	   break;
              case ITEM5:
            	   showDialog(ShareBy_Dlg);
            	   //SendEmail("", "");
            	   break;
           }
           return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		   case ProjectConst.Check_NoteTitle_Dlg:
			    return HelperFunctions.BuildAltertDialog(EditNoteActivity.this, R.string.prompt_title, R.string.notetitle_empty_tip);
		   case ProjectConst.Set_NotifyDate_Dlg:
			    return HelperFunctions.BuildAltertDialog(EditNoteActivity.this, R.string.prompt_title, R.string.notifydate_expire_tip);
		   case Del_Prompt_Dlg:
				return BuildDelPromptDialog(EditNoteActivity.this, R.string.delnote_title);
		   case NoteHasLock_Dlg:
		        return BuildNoteHasLockDialog(EditNoteActivity.this, R.string.note_lock_dlg_title, R.string.notehaslock_msg);
		   case ShareBy_Dlg:
			    return BuildShareByDlg(this, R.string.shareby_title);
		}
		return null;
	}
	
	private Dialog BuildShareByDlg(Context AppContext, int Title)
	{
        Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(R.drawable.ic_dialog_menu_generic);  
        builder.setTitle(Title);  
        final BaseAdapter adapter = new ListItemAdapter();  
        DialogInterface.OnClickListener listener =   
            new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialogInterface, int which) { 
                	ListItemAdapter Adapter = (ListItemAdapter)adapter;
                	Intent intent = new Intent();
                	
                    intent.setAction(Intent.ACTION_SEND);
                	intent.setComponent(new ComponentName(Adapter.mApps.get(which).activityInfo.packageName, Adapter.mApps.get(which).activityInfo.name));
                    intent.putExtra(Intent.EXTRA_SUBJECT, EditOneNote.NoteTitle);
                    intent.putExtra(Intent.EXTRA_TEXT, EditOneNote.NoteBody);
                    intent.setType("text/plain");
                    
                	startActivity(intent); 
                }  
            };  
        builder.setAdapter(adapter, listener);  
        return builder.create();  
	}
	
	// Go to edit mode
	private void StartEditMode()
	{
	    EditBtn.setVisibility(View.GONE);
	    NoteTitleView.setVisibility(View.GONE);
	    ChgTagClrBtn.setVisibility(View.VISIBLE);
	    NoteTitleCtrl.setVisibility(View.VISIBLE);
	    NoteBodyCtrl.setEnabled(true);
	    NoteBodyCtrl.setFocusable(true);
	    NoteBodyCtrl.setFocusableInTouchMode(true);  
	    NoteBodyCtrl.requestFocus();
	    ChgTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[EditOneNote.TagImgIdx], PorterDuff.Mode.MULTIPLY);
	}
	
	// Turn to set notify time activity
	private void StartNotifyActivity()
	{
		// start notify time activity
    	Intent intent = new Intent();
		intent.setClass(EditNoteActivity.this, NotifyDateActivity.class);
		// Set time
		Bundle Parameters = new Bundle();
		Parameters.putInt(OneNote.KEY_NOTIFYDURA, EditOneNote.NotifyDura);
		Parameters.putInt(OneNote.KEY_NOTIFYMETHOD, EditOneNote.NotifyMethod);
		Parameters.putString(OneNote.KEY_RINGMUSIC, EditOneNote.RingMusic);
		Parameters.putString(OneNote.KEY_USE_NOTIFYTIME, EditOneNote.Use_NotifyTime);
		Parameters.putString(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(EditOneNote.NotifyTime));
        // Pass it to next activity 
		intent.putExtras(Parameters);
		// Go to next activity(set note's notify time activity)
		startActivityForResult(intent, ACTIVITY_SET_NOTIFYTIME);	
	}
	
	// Build a dialog to prompt user we want to delete the assigned note
	private Dialog BuildDelPromptDialog(final Context AppContext, int Title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(AppContext);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle(Title);
		builder.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Delete database record
						NotesDb.DeleteOneNote(EditOneNote.NoteRowId);
						// Delete file on local file system(need full path)
						String path = getString(R.string.notefile_path_prefix) + EditOneNote.NoteFilePath;
						File NoteFile = new File(path);
						NoteFile.delete();
						// Return to main activity
				 	    setResult(RESULT_OK);
						finish();
					}
				});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		return builder.create();
	}
	
	// Note has been locked prompt dialog
	private Dialog BuildNoteHasLockDialog(Context AppContext, int Title, int Msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(AppContext);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle(Title);
		builder.setMessage(Msg);
		builder.setPositiveButton(R.string.changenotepwd_title,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						   Intent ChgPwdDlgIntent = new Intent(EditNoteActivity.this, ChgPwdDlgActivity.class);
						   ChgPwdDlgIntent.putExtra(OneNote.KEY_ROWID, EditOneNote.NoteRowId);
						   startActivity(ChgPwdDlgIntent);
					}
				});
		builder.setCancelable(false);
		builder.setNeutralButton(R.string.clearpwd_tip,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						   Intent ClrPwdDlgIntent = new Intent(EditNoteActivity.this, ClearPwdDlgActivity.class);
						   ClrPwdDlgIntent.putExtra(OneNote.KEY_ROWID, EditOneNote.NoteRowId);
						   startActivity(ClrPwdDlgIntent);
					}
				});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		return builder.create();
	}
	
	
	
	// Handler return code
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == ACTIVITY_SET_NOTIFYTIME ) {
	    	if( resultCode == RESULT_OK ) {
	    		Bundle Result = data.getExtras();
	    		if( Result.isEmpty() )
	    		{
	    			EditOneNote.Use_NotifyTime = ProjectConst.No;
	    			NotifyTimeLabel.setText(R.string.nonotifytime);
	    		} else {
	    			EditOneNote.Use_NotifyTime = ProjectConst.Yes;
	    			EditOneNote.NotifyMethod = Result.getInt(OneNote.KEY_NOTIFYMETHOD);
	    			EditOneNote.NotifyDura = Result.getInt(OneNote.KEY_NOTIFYDURA);
	    			EditOneNote.RingMusic = Result.getString(OneNote.KEY_RINGMUSIC);
	    			EditOneNote.NotifyTime = HelperFunctions.String2Calenar(Result.getString(OneNote.KEY_NOTIFYTIME));
	    			EditOneNote.NotifyTime.set(Calendar.SECOND, 0);
	    			EditOneNote.NotifyTime.set(Calendar.MILLISECOND, 0);
	    		    NotifyTimeLabel.setText(HelperFunctions.FormatCalendar2ReadableStr(EditOneNote.NotifyTime));
	    		}
	    	}
	    } else if ( requestCode == ACTIVITY_SET_TAGCLR ) {
	    	if( resultCode == RESULT_OK ) {
	    	    Bundle SelIdxData = data.getExtras();
	    	    EditOneNote.TagImgIdx = EditOneNote.ItemBgIdx = SelIdxData.getInt(OneNote.KEY_TAGIMG_ID);
	    	    ChgTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[EditOneNote.TagImgIdx], PorterDuff.Mode.MULTIPLY);
	    	    NoteBodyCtrl.getBackground().setColorFilter(NotePadPlus.ItemBgClr[EditOneNote.ItemBgIdx], PorterDuff.Mode.MULTIPLY);
	    	}
	    }
	}
	
	class ListItemAdapter extends BaseAdapter {  
	      
		  public List<ResolveInfo> mApps;
		  ListItemAdapter(){
				Intent IntentType = new Intent(Intent.ACTION_SEND);
				IntentType.setType("text/plain");  
		        mApps = getPackageManager().queryIntentActivities(IntentType, PackageManager.GET_ACTIVITIES);
		  }
	      @Override  
	      public int getCount() {  
	             return mApps.size();  
	      }  
	  
	      @Override  
	      public Object getItem(int position) {  
	            return null;  
	      }  
	  
	      @Override  
	      public long getItemId(int position) {  
	            return position;  
	      }  
	  
	      @Override  
	      public View getView(int position, View contentView, ViewGroup parent) {  
	    	    LinearLayout Item = new LinearLayout(EditNoteActivity.this);
	            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);  
	    	    Item.setLayoutParams(layoutParams);
	            TextView textView = new TextView(EditNoteActivity.this);  

	            ResolveInfo info  = mApps.get(position) ;
	            String text = info.loadLabel(getPackageManager()).toString();
	            textView.setText(text);  

	            //设置字体大小  
	            textView.setTextSize(24);  
	            //设置水平方向上居中  
	            textView.setGravity(android.view.Gravity.CENTER_VERTICAL);  
	            textView.setMinHeight(60);  
	            textView.setPadding(5, 0, 0, 0);
	            //设置文字颜色  
	            textView.setTextColor(Color.BLACK);  
	            
	            // Set app icon
	            ImageView Tag = new ImageView(EditNoteActivity.this);
	            Tag.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
	            Tag.setScaleType(ImageView.ScaleType.FIT_CENTER);
	            Tag.setMinimumHeight(60);
                 
	            // Add icon & name
	            Item.addView(Tag);
	            Item.addView(textView);

	            return Item;  
	      }	          
	}  
}