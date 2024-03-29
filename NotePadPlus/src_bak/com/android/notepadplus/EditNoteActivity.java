package com.android.notepadplus;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import com.android.notepadplus.R;
import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.app.Dialog;

public class EditNoteActivity extends Activity {
	
	/** Request code */
	private static final int ACTIVITY_SET_ENDDATE = 0;
	private static final int ACTIVITY_SET_NOTIFYTIME = 1;
	private static final int ACTIVITY_SET_TAGCLR = 2;
	
	/** Source from notepadplus or notifyalarmreceiver's notification */
	public static final String KEY_SOURCE = "source";
	// Views
	private EditText NoteTitleCtrl = null;
	private EditText NoteBodyCtrl = null;
	private TextView EndTimeLabel = null;
	private TextView NotifyTimeLabel = null;
	private Button ChgTagClrBtn = null;
	
	// Activity parameters
	private OneNote EditOneNote;
	private String Pre_UseNotifyTime;
	private Calendar Pre_NotifyTime;
	
	// Database
	private NoteDbAdapter NotesDb = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editnote);

		// Initialize & open database
		EditOneNote = new OneNote();
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();
		
		// Get control handler
		NoteTitleCtrl = (EditText)findViewById(R.id.title_content);
		NoteBodyCtrl = (EditText)findViewById(R.id.body_content);
		EndTimeLabel = (TextView)findViewById(R.id.endtime_text2);
		NotifyTimeLabel = (TextView)findViewById(R.id.notifytime_text2);
		ChgTagClrBtn = (Button)findViewById(R.id.chgnoteclr);
		
		// Get parameters from intent
		Bundle Parameters = getIntent().getExtras();
		if( Parameters != null ) {
			// It is from notification
			if( Parameters.getString(KEY_SOURCE).equals(Alarms.ALARM_ALERT_ACTION) )
			{
				// Get note's row id
				EditOneNote.NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
				// We are from notify alarm, so since it has been in edit note activity
				// Stop playing music service
				// Stop notify alarm and set next one
	    	    Intent StopService = new Intent(Alarms.ALARM_NOTIFY_RING);
		    	stopService(StopService);
				Alarms.DeleteOneAlarm(this, EditOneNote.NoteRowId);
				// From notify's notification
				Cursor Note = NotesDb.GetOneNote(EditOneNote.NoteRowId);
				EditOneNote = new OneNote(Note);
				// Remove notification on bar
				NotificationManager notificationManager = (NotificationManager)getSystemService(android.content.Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(EditOneNote.NoteRowId);
				if( EditOneNote.NoteFilePath != null ) 
					// Read note's content
					EditOneNote.NoteBody = HelperFunctions.ReadTextFile(this, EditOneNote.NoteFilePath);
			} else {
			    // Initialize the note
				EditOneNote = new OneNote(Parameters);
				if( EditOneNote.NoteFilePath != null ) 
					// Read note's content
					EditOneNote.NoteBody = HelperFunctions.ReadTextFile(this, EditOneNote.NoteFilePath);
			}
			
			// Backup the notify time
			Pre_UseNotifyTime = EditOneNote.Use_NotifyTime;
			Pre_NotifyTime = EditOneNote.NotifyTime;

			// Set control's text
			if( EditOneNote.NoteTitle != null )  
				NoteTitleCtrl.setText(EditOneNote.NoteTitle);
			 
			if( EditOneNote.NoteBody != null )
				NoteBodyCtrl.setText(EditOneNote.NoteBody);

			if( EditOneNote.Use_EndTime.equals(ProjectConst.Yes) )
				EndTimeLabel.setText(HelperFunctions.FormatCalendar2ReadablePrefixStr(EditOneNote.EndTime));

			if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
				NotifyTimeLabel.setText(HelperFunctions.FormatCalendar2ReadableStr(EditOneNote.NotifyTime));
			
			// Set tag color
			ChgTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[EditOneNote.TagImgIdx], PorterDuff.Mode.MULTIPLY);
			NoteBodyCtrl.getBackground().setColorFilter(NotePadPlus.ItemBgClr[EditOneNote.ItemBgIdx], PorterDuff.Mode.MULTIPLY);
		}
		

		// Show tag color change activity
		ChgTagClrBtn.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	Intent intent = new Intent();
				intent.setClass(EditNoteActivity.this, SetTagClrActivity.class);
				startActivityForResult(intent, ACTIVITY_SET_TAGCLR);				
    		}
    	});
		
		// Change end time button
		Button EndTimeBtn = (Button)findViewById(R.id.endtime);
		EndTimeBtn.setOnClickListener(new OnClickListener(){
		    public void onClick(View v){
		    	// Set time and start new activity
		    	Intent intent = new Intent();
				intent.setClass(EditNoteActivity.this, EndDateActivity.class);
				// Set time
				Bundle SelectedTime = new Bundle();
				SelectedTime.putString(OneNote.KEY_USE_ENDTIME, EditOneNote.Use_EndTime);
				SelectedTime.putString(OneNote.KEY_DELNOTE_EXP, EditOneNote.DelNoteExp);
				SelectedTime.putString(OneNote.KEY_ENDTIME, HelperFunctions.Calendar2String(EditOneNote.EndTime));
				// Pass it to next activity 
				intent.putExtras(SelectedTime);
				// Go to next activity(set note's end date activity)
				startActivityForResult(intent, ACTIVITY_SET_ENDDATE);		    	
		    }
		}
		);
		
		// Change notify time button
		Button NotifyTimeBtn = (Button)findViewById(R.id.notifytime);
		NotifyTimeBtn.setOnClickListener(new OnClickListener(){
		    public void onClick(View v){
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
		}
		);
		
	}
	
	 @Override 
	 public void onBackPressed(){
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
         // Check end date & notify date    	 
    	 if( EditOneNote.Use_EndTime.equals(ProjectConst.Yes)&&EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
    	 {
    		 // If the notify date is later than end date, prompt to user
    		 if( HelperFunctions.CmpDatePrefix(EditOneNote.EndTime, EditOneNote.NotifyTime)<0 )
    		 {
    			 showDialog(ProjectConst.Check_NotifyEndDate_Dlg);
    			 return;
    		 }
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
    	 // Show toast to notify user settings have been saved
 	     Toast.makeText(EditNoteActivity.this, getString(R.string.notesavingtip), Toast.LENGTH_SHORT).show();
         // Return to main activity
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
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		   case ProjectConst.Check_NoteTitle_Dlg:
			    return HelperFunctions.BuildAltertDialog(EditNoteActivity.this, R.string.prompt_title, R.string.notetitle_empty_tip);
		   case ProjectConst.Check_EndDate_Dlg:
			    return HelperFunctions.BuildAltertDialog(EditNoteActivity.this, R.string.prompt_title, R.string.enddate_expire_tip);
		   case ProjectConst.Check_NotifyDate_Dlg:
			    return HelperFunctions.BuildAltertDialog(EditNoteActivity.this, R.string.prompt_title, R.string.notifydate_expire_tip);
		   case ProjectConst.Check_NotifyEndDate_Dlg:
			    return HelperFunctions.BuildAltertDialog(EditNoteActivity.this, R.string.prompt_title, R.string.enddate_pre_notifydate_tip);
		}
		return null;
	}
	
	// Save note
	public void WriteTextFile(Context context, String Data, String Path){   
		FileOutputStream fileOutStream = null;     
		OutputStreamWriter osWriter = null;             
		try {
			fileOutStream = openFileOutput(Path, MODE_WORLD_READABLE);
		    osWriter = new OutputStreamWriter(fileOutStream);       
		    osWriter.write(Data);
		    osWriter.flush(); 
		} catch (Exception e) {        
			e.printStackTrace();             
			Toast.makeText(context, R.string.notenotsave_tip, Toast.LENGTH_SHORT).show();                   
        } finally {
        	try{
		       osWriter.close();
		       fileOutStream.close();
        	} catch (IOException e) {
        		e.printStackTrace();
            }
        }
	} 
	
	// Read note
	public String ReadTextFile(Context context, String path){
		FileInputStream fileInStream = null;
		InputStreamReader isReader = null;
		char inputBuf[] = new char[255];
		String Content = ProjectConst.EmptyStr;
		int ReadCount=0;
		try{
			fileInStream = openFileInput(path);  
			isReader = new InputStreamReader(fileInStream);
            while( (ReadCount=isReader.read(inputBuf)) != -1 )
            	   Content += new String(inputBuf, 0, ReadCount);
        } catch (Exception e) {      
            e.printStackTrace();
            Toast.makeText(context, R.string.readnotefile_fail_err,Toast.LENGTH_SHORT).show();
        } finally {
            try {
        	   fileInStream.close();
        	   isReader.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
        Content.trim();
		return Content;
	}
	
	// Handler return code
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if( requestCode == ACTIVITY_SET_ENDDATE ) { 
			if( resultCode == RESULT_OK ) { 
				Bundle Result = data.getExtras();
				if( Result.isEmpty() )
				{
					EditOneNote.Use_EndTime = ProjectConst.No;
					EditOneNote.DelNoteExp = ProjectConst.No;
					EndTimeLabel.setText(R.string.noendtime);					
				} else {
					EditOneNote.Use_EndTime = ProjectConst.Yes;
					EditOneNote.DelNoteExp = Result.getString(OneNote.KEY_DELNOTE_EXP);
					EditOneNote.EndTime = HelperFunctions.String2Calenar(Result.getString(OneNote.KEY_ENDTIME));				
				    EndTimeLabel.setText(HelperFunctions.FormatCalendar2ReadablePrefixStr(EditOneNote.EndTime));
				}
			}
	    } else if( requestCode == ACTIVITY_SET_NOTIFYTIME ) {
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
}