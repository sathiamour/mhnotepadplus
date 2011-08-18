package com.android.hinotes;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.app.Dialog;

public class EditCheckListNoteActivity extends Activity {
	// Views
	private EditText NoteTitleCtrl = null;
	private ListView CheckList;
	private TextView NotifyTimeLabel = null;
	private TextView NoteTitleView = null;
	private Button ChgTagClrBtn = null;
	private ImageButton   EditBtn = null;
	private LinearLayout EditPanel;
	private View PromptView;
	private CheckListItemAdapter ItemAdapter;
	private OnItemClickListener ItemClickListener;
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
	// Push front or back
	private boolean FrontOrBack; 
	// Current item's index
	private int CurIdx;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editchecklistnote);

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
		// Push front or back
		FrontOrBack = false; 
		// Current item's index
		CurIdx = ProjectConst.NegativeOne;
		// Get control handler
		NoteTitleCtrl = (EditText)findViewById(R.id.title_content);
		NoteTitleView = (TextView)findViewById(R.id.title);
		NotifyTimeLabel = (TextView)findViewById(R.id.notifytime_text);
		CheckList = (ListView)findViewById(R.id.checklist);
		ChgTagClrBtn = (Button)findViewById(R.id.chgnoteclr);
		EditBtn = (ImageButton)findViewById(R.id.editnotebtn);
		EditPanel = (LinearLayout)findViewById(R.id.editnote_panel);
		LayoutInflater factory = LayoutInflater.from(this);
		PromptView  = factory.inflate(R.layout.checklistitem_input, null);
		// Get parameters from intent to initialize the note
		EditOneNote = new OneNote(OneNote.ListNote);
		Bundle Parameters = getIntent().getExtras();
		if( Parameters != null ) {
			String Source = Parameters.getString(ProjectConst.KEY_SOURCE);
			int RowId = Parameters.getInt(OneNote.KEY_ROWID);
			// It is from notification
			if( Source.equals(ProjectConst.ALARM_ALERT_ACTION) )
			{
				// We are from notify alarm, so since it has been in edit note activity
				// Stop playing music service
				// Stop notify alarm and set next one
	    	    Intent StopService = new Intent(ProjectConst.ALARM_NOTIFY_RING);
		    	stopService(StopService);
				Alarms.DeleteOneAlarm(this, RowId);
				// Remove notification on bar
				NotificationManager notificationManager = (NotificationManager)getSystemService(android.content.Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(RowId);
			} else if( Source.equals(ProjectConst.USERDEF_EDIT_ACTION) ) {
				// if it is from user def view, to get postion index in parent layout
				EditOneNote.PosIdx = Parameters.getInt(OneNote.KEY_INDEX);
			}

			// Initialize the note with database 
			Cursor Note = NotesDb.GetOneNote(RowId);
			if( Note.getCount() == 1 )
			{
				EditOneNote.InitializatonFromDb(Note);
		    	// Add list's adapter
		    	ItemAdapter = new CheckListItemAdapter(this, HelperFunctions.ReadCheckListFile(this, EditOneNote.NoteFilePath));
		    	CheckList.setAdapter(ItemAdapter);
	        } else {
		        IsDeleted = true;
       		    EditOneNote.NoteTitle = getString(R.string.note_hasdel_prompt);
		        EditBtn.setEnabled(false);
	        }
		
			// Backup the notify time
			Pre_UseNotifyTime = EditOneNote.Use_NotifyTime;
			Pre_NotifyTime = EditOneNote.NotifyTime;

			// Set control's text
			NoteTitleView.setText(EditOneNote.NoteTitle);
			NoteTitleCtrl.setText(EditOneNote.NoteTitle);

			if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) && HelperFunctions.CmpDatePrefix2(EditOneNote.NotifyTime, Calendar.getInstance())>0 )
				NotifyTimeLabel.setText(HelperFunctions.FormatCalendar2ReadableStr(EditOneNote.NotifyTime));
		}

		EditPanel.setBackgroundDrawable(HelperFunctions.CreateTitleBarBg(ScreenWidth, ScreenHeight,  NotePadPlus.ItemBgClr[EditOneNote.DrawableResIdx], NotePadPlus.TagClr[EditOneNote.DrawableResIdx]));
		
		// Set text changed listener
		NoteTitleCtrl.addTextChangedListener(new TextWatcher(){  
			  
	        @Override  
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}  
	  
	        @Override  
	        public void onTextChanged(CharSequence s, int start, int before, int count) {  
	            EditOneNote.NoteTitle = s.toString();
	        }

			@Override
			public void afterTextChanged(Editable arg0) {}  
	          
	    });
		
		// List item short click listener
    	// 
    	ItemClickListener = new OnItemClickListener() {
			/** When user clicks a note, show edit activity */
			public void onItemClick(AdapterView<?> Adapater, View ListView, int position, long id) {
				   if( position == ProjectConst.Zero ) {
					   FrontOrBack = true;
					   Intent i = new Intent(EditCheckListNoteActivity.this, AddItemActivity.class);
					   startActivityForResult(i, ProjectConst.ACTIVITY_ADD_ITEM);
				   } else if( position == ItemAdapter.getCount()-1 ) {
					   FrontOrBack = false;
					   Intent i = new Intent(EditCheckListNoteActivity.this, AddItemActivity.class);
					   startActivityForResult(i, ProjectConst.ACTIVITY_ADD_ITEM);
				   } else {
					   CurIdx = position;
					   ((EditText)PromptView.findViewById(R.id.item_content)).setText(ItemAdapter.GetItemAt(position));
					   showDialog(ProjectConst.ItemEdit_Dlg);
				   }						   
			}
		};
    	
		// Show tag color change activity
		ChgTagClrBtn.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	Intent intent = new Intent();
				intent.setClass(EditCheckListNoteActivity.this, SetTagClrActivity.class);
				startActivityForResult(intent, ProjectConst.ACTIVITY_SET_TAGCLR);				
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
         // Check title
    	 if( EditOneNote.NoteTitle.length() == 0 )
    	 {
    		 // Title is empty, we prompt it to user and then return
    		 showDialog(ProjectConst.Check_NoteTitle_Dlg);
    		 return;
    	 }
    	 // Save it to original file
    	 HelperFunctions.WriteCheckListFile(this, ItemAdapter.GetItems(), ProjectConst.One, ItemAdapter.getCount()-2, EditOneNote.NoteFilePath);
    	 // Update database record
    	 NotesDb.UpdateOneNote(EditOneNote); 
    	 
    	 // Update the sepcific alarm if user has changed the notify time
    	 if( !Pre_UseNotifyTime.equals(EditOneNote.Use_NotifyTime) )
    	 {
        	 // From Y(use notify) to N(don't use notify), User cancels alarm
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.No) )
    		 {
    		     Log.d(ProjectConst.TAG,"EditNoteActivity: delete alarm");
    		     Alarms.DeleteOneAlarm(this, EditOneNote.NoteRowId);
    		 }
    		 // From N(don't use notify) to Y(use notify), add one alarm
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
    		 {
    			 Log.d(ProjectConst.TAG,"EditNoteActivity: add alarm");
    			 NotesDb.UpdateNoteNotifyRingTime(EditOneNote.NoteRowId, EditOneNote.NotifyTime);
        		 Alarms.AddOneAlarm(this);
    		 }
    	 } else {
    		 // User changes the notify time
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) && HelperFunctions.CmpDatePrefix2(Pre_NotifyTime, EditOneNote.NotifyTime) != 0 )
    		 { 
        		 Log.d(ProjectConst.TAG,"EditNoteActivity: update alarm");
    		     NotesDb.UpdateNoteNotifyRingTime(EditOneNote.NoteRowId, EditOneNote.NotifyTime);
                 Alarms.UpdateOneAlarm(this, EditOneNote.NoteRowId);
    	     }
    	 }
    	 
    	 // Refresh widget note list
    	 HelperFunctions.RefreshWidgetNoteList(this, NotesDb.GetAllNotes());
    	 // Refresh 1x1 widget
    	 if( EditOneNote.WidgetId != 0 )
    	     HelperFunctions.Refresh1x1Widget(this, EditOneNote.WidgetId, EditOneNote.NoteTitle, EditOneNote.NoteRowId, EditOneNote.DrawableResIdx, EditOneNote.Password.length()>0, EditOneNote.NoteType);
    	 // Show toast to notify user settings have been saved
 	     Toast.makeText(this, getString(R.string.notesavingtip), Toast.LENGTH_SHORT).show();
         // Return to main activity
 	     Intent ReturnBackData = new Intent();
	     ReturnBackData.putExtra(OneNote.KEY_TITLE, EditOneNote.NoteTitle);
	     ReturnBackData.putExtra(OneNote.KEY_INDEX, EditOneNote.PosIdx);
	     ReturnBackData.putExtra(OneNote.KEY_DRAWABLE_ID, EditOneNote.DrawableResIdx);
	     setResult(RESULT_OK, ReturnBackData);
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
		menu.add(Menu.NONE, ProjectConst.ITEM0, 1, "·ÅÆú").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(Menu.NONE, ProjectConst.ITEM1, 2, "ÌáÐÑ").setIcon(R.drawable.ic_menu_reminder).setEnabled(!IsDeleted);
		menu.add(Menu.NONE, ProjectConst.ITEM2, 3, "É¾³ý").setIcon(android.R.drawable.ic_menu_delete).setEnabled(!IsDeleted);
		menu.add(Menu.NONE, ProjectConst.ITEM3, 4, "Ëø¶¨").setIcon(R.drawable.ic_menu_lock).setEnabled(!IsDeleted);
		menu.add(Menu.NONE, ProjectConst.ITEM4, 5, "±à¼­").setIcon(android.R.drawable.ic_menu_edit).setEnabled(!IsDeleted);
		menu.add(Menu.NONE, ProjectConst.ITEM5, 6, "·ÖÏí").setIcon(android.R.drawable.ic_menu_share).setEnabled(!IsDeleted);
        return true;
	}
	

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {  
           switch(item.getItemId()) 
           {  
              case ProjectConst.ITEM0:
            	   setResult(RESULT_CANCELED);
	               finish();
	               break;
              case ProjectConst.ITEM1:
            	   StartNotifyActivity();
            	   break;
              case ProjectConst.ITEM2:
            	   showDialog(ProjectConst.DelNote_Prompt_Dlg);
            	   break;
              case ProjectConst.ITEM3:
      			   if( EditOneNote.Password.length() == 0 )
      			   {
      			       Intent PwdDlgIntent = new Intent(this, PwdDlgActivity.class);
      			       PwdDlgIntent.putExtra(OneNote.KEY_ROWID, EditOneNote.NoteRowId);
      			       startActivityForResult(PwdDlgIntent, ProjectConst.ACTIVITY_SET_PWD);
                   } else
      			       showDialog(ProjectConst.NoteHasLock_Dlg);
            	   break;
              case ProjectConst.ITEM4:
            	   StartEditMode();
            	   break;
              case ProjectConst.ITEM5:
            	   showDialog(ProjectConst.ShareBy_Dlg);
            	   break;
           }
           return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		   case ProjectConst.ItemEdit_Dlg:
			    return BuildEditItemDlg(this, R.string.checklist_item_title);
		   case ProjectConst.Check_NoteTitle_Dlg:
			    return HelperFunctions.BuildAltertDialog(this, R.string.prompt_title, R.string.notetitle_empty_tip);
		   case ProjectConst.Set_NotifyDate_Dlg:
			    return HelperFunctions.BuildAltertDialog(this, R.string.prompt_title, R.string.notifydate_expire_tip);
		   case ProjectConst.DelNote_Prompt_Dlg:
				return BuildDelPromptDialog(this, R.string.delnote_title);
		   case ProjectConst.NoteHasLock_Dlg:
		        return BuildNoteHasLockDialog(this, R.string.note_lock_dlg_title, R.string.notehaslock_msg);
		   case ProjectConst.ShareBy_Dlg:
			    return HelperFunctions.BuildShareByDlg(this, R.string.shareby_title, EditOneNote.NoteTitle, 
			    		                               HelperFunctions.ComposeSharedItems(ItemAdapter.GetItems(), ProjectConst.One, ItemAdapter.getCount()-2));
		}
		return null;
	}
	

	
	// Go to edit mode
	private void StartEditMode()
	{
	    EditBtn.setVisibility(View.GONE);
	    NoteTitleView.setVisibility(View.GONE);
	    ChgTagClrBtn.setVisibility(View.VISIBLE);
	    NoteTitleCtrl.setVisibility(View.VISIBLE);
	    CheckList.setOnItemClickListener(ItemClickListener);
	    ChgTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[EditOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
	}
	
	// Turn to set notify time activity
	private void StartNotifyActivity()
	{
		// start notify time activity
    	Intent intent = new Intent();
		intent.setClass(EditCheckListNoteActivity.this, NotifyDateActivity.class);
		// Set time
		Bundle Parameters = new Bundle();
		Parameters.putInt(OneNote.KEY_NOTIFYDURA, EditOneNote.NotifyDura);
		Parameters.putInt(OneNote.KEY_NOTIFYMETHOD, EditOneNote.NotifyMethod);
		Parameters.putString(OneNote.KEY_RINGMUSIC, EditOneNote.RingMusic);
		//Parameters.putString(OneNote.KEY_USE_NOTIFYTIME, EditOneNote.Use_NotifyTime);
		Parameters.putString(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(EditOneNote.NotifyTime));
        // Pass it to next activity 
		intent.putExtras(Parameters);
		// Go to next activity(set note's notify time activity)
		startActivityForResult(intent, ProjectConst.ACTIVITY_SET_NOTIFYTIME);	
	}
	
	private Dialog BuildEditItemDlg(Context AppContext, int Title){
		return new AlertDialog.Builder(AppContext)
               .setIcon(R.drawable.ic_dialog_menu_generic)
               .setTitle(Title)
               .setView(PromptView)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
                	   EditText Content = (EditText)PromptView.findViewById(R.id.item_content);
                	   if( Content.getText().toString().length() > 0 )
                		   ItemAdapter.SetItemAt(CurIdx, Content.getText().toString());
              }
              })
              .setNeutralButton(R.string.checklist_delitem_title, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Log.d("log","the index is "+CurIdx);
						ItemAdapter.RemoveItemAt(CurIdx);
					}
			  })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                  }
              })
              .create();
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
						Log.d("log","delete "+NoteFile.delete());
						// Refresh widget note list
				    	HelperFunctions.RefreshWidgetNoteList(EditCheckListNoteActivity.this, NotesDb.GetAllNotes());
						// Return to main activity
				 	    Intent ReturnBackData = new Intent();
					    //ReturnBackData.putExtra(OneNote.KEY_TITLE, EditOneNote.NoteTitle);
					    ReturnBackData.putExtra(OneNote.KEY_INDEX, ProjectConst.NegativeOne);
					    // ReturnBackData.putExtra(OneNote.KEY_DRAWABLE_ID, EditOneNote.DrawableResIdx);
					    setResult(RESULT_OK, ReturnBackData);
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
						   Intent ChgPwdDlgIntent = new Intent(EditCheckListNoteActivity.this, ChgPwdDlgActivity.class);
						   ChgPwdDlgIntent.putExtra(OneNote.KEY_ROWID, EditOneNote.NoteRowId);
						   startActivity(ChgPwdDlgIntent);
					}
				});
		builder.setCancelable(false);
		builder.setNeutralButton(R.string.clearpwd_tip,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						   Intent ClrPwdDlgIntent = new Intent(EditCheckListNoteActivity.this, ClearPwdDlgActivity.class);
						   ClrPwdDlgIntent.putExtra(OneNote.KEY_ROWID, EditOneNote.NoteRowId);
						   startActivityForResult(ClrPwdDlgIntent, ProjectConst.ACTIVITY_CLR_PWD);
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
        if( requestCode == ProjectConst.ACTIVITY_SET_NOTIFYTIME && resultCode == RESULT_OK ) {
	    	Bundle Result = data.getExtras();
	    	if( Result.isEmpty() )
	    	{
	    		EditOneNote.Use_NotifyTime = ProjectConst.No;
	    		NotifyTimeLabel.setText(ProjectConst.EmptyStr);
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
	    } else if ( requestCode == ProjectConst.ACTIVITY_SET_TAGCLR && resultCode == RESULT_OK ) {
	       	    Bundle SelIdxData = data.getExtras();
	    	    EditOneNote.DrawableResIdx = SelIdxData.getInt(OneNote.KEY_DRAWABLE_ID);
	    	    ChgTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[EditOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
	    	    EditPanel.setBackgroundDrawable(HelperFunctions.CreateTitleBarBg(ScreenWidth, ScreenHeight,  NotePadPlus.ItemBgClr[EditOneNote.DrawableResIdx], NotePadPlus.TagClr[EditOneNote.DrawableResIdx]));
	    } else if( requestCode == ProjectConst.ACTIVITY_SET_PWD && resultCode == RESULT_OK  ) {
	    	    EditOneNote.Password = data.getStringExtra(OneNote.KEY_PWD);
	    } else if( requestCode == ProjectConst.ACTIVITY_CLR_PWD && resultCode == RESULT_OK ) {
	    	    EditOneNote.Password = ProjectConst.EmptyStr;
	    } else if( requestCode == ProjectConst.ACTIVITY_ADD_ITEM && resultCode == RESULT_OK ) {
    	    String Act = data.getStringExtra(AddItemActivity.Action);
    	    String Item = data.getStringExtra(OneNote.KEY_BODY);
    	    Log.d("log",Item);
    	    // Push 
    	    if( FrontOrBack )
    	    	ItemAdapter.PushFontItem(Item);
 		    else
 			    ItemAdapter.PushBackItem(Item);
    	    // Show next if needed
    	    if( Act.equals(AddItemActivity.NextAction) )
    	    {
    	    	Intent i = new Intent(EditCheckListNoteActivity.this, AddItemActivity.class);
				startActivityForResult(i, ProjectConst.ACTIVITY_ADD_ITEM);
    	    }
        }
	}
	
	
}