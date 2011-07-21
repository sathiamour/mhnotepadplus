package com.android.notepadplus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
import com.android.notepadplus.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.app.Dialog;

public class AddNoteActivity extends Activity {
	
	/** Action id for activity redirection */
	public static final int ACTIVITY_SET_ENDDATE = 0;
	public static final int ACTIVITY_SET_NOTIFYTIME = 1;
	public static final int ACTIVITY_SET_TAGCLR = 2;

	/** Control */
	private EditText NoteTitleCtrl = null;
	private EditText NoteBodyCtrl = null;
	private TextView NotifyTimeLabel = null;
	private Button   SelectTagClrBtn = null;
	
	/**Menu id */
	public static final int ITEM0 = Menu.FIRST;
	public static final int ITEM1 = Menu.FIRST + 1;
	public static final int ITEM2 = Menu.FIRST + 2;
	public static final int ITEM3 = Menu.FIRST + 3;
	public static final int ITEM4 = Menu.FIRST + 4;
	public static final int ITEM5 = Menu.FIRST + 5;
	
	/** Database */
	private NoteDbAdapter NotesDb = null;
	
	/** One note */
	private OneNote AddOneNote;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addnote);
		
		// Initialize & open database
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();
		AddOneNote = new OneNote();
		
		// Get control handler
		NoteTitleCtrl = (EditText)findViewById(R.id.title_content);
		SelectTagClrBtn = (Button)findViewById(R.id.selnoteclr);
		NoteBodyCtrl = (EditText)findViewById(R.id.body_content);
		NotifyTimeLabel = (TextView)findViewById(R.id.notfiytime_text);
		
		// Randomly select color
    	SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.TagImgIdx], PorterDuff.Mode.MULTIPLY);
    	NoteBodyCtrl.getBackground().setColorFilter(NotePadPlus.ItemBgClr[AddOneNote.ItemBgIdx], PorterDuff.Mode.MULTIPLY);
    	 
		// Set tag color
    	SelectTagClrBtn.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	Intent intent = new Intent();
				intent.setClass(AddNoteActivity.this, SetTagClrActivity.class);
				startActivityForResult(intent, ACTIVITY_SET_TAGCLR);				
    		}
    	});
    	
		
		// Set notify time for note 
		AddOneNote.NotifyTime = Calendar.getInstance(Locale.CHINA);
		Button NotifyTimeBtn = (Button)findViewById(R.id.notifytime);
		NotifyTimeBtn.setOnClickListener(new OnClickListener(){
		    public void onClick(View v){
		    	// start notify time activity
		    	Intent intent = new Intent();
				intent.setClass(AddNoteActivity.this, NotifyDateActivity.class);
				// Set time
				Bundle Parameters = new Bundle();
				Parameters.putString(OneNote.KEY_USE_NOTIFYTIME, AddOneNote.Use_NotifyTime);
				Parameters.putString(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(AddOneNote.NotifyTime));
				Parameters.putInt(OneNote.KEY_NOTIFYDURA, AddOneNote.NotifyDura);
				Parameters.putString(OneNote.KEY_RINGMUSIC, AddOneNote.RingMusic);
				Parameters.putInt(OneNote.KEY_NOTIFYMETHOD, AddOneNote.NotifyMethod);
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
   	    AddOneNote.NoteTitle = NoteTitleCtrl.getText().toString().trim();
   	    AddOneNote.NoteBody = NoteBodyCtrl.getText().toString().trim();
   	    if( AddOneNote.NoteTitle.length() == 0 )
   	    {
   		    // Title is empty, we prompt it to user and then return
   		    showDialog(ProjectConst.Check_NoteTitle_Dlg);
   		    return;
   	    }
        // Check notify date
   	    Calendar Now = Calendar.getInstance();
   	    if( AddOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
   	    {
   		    // If the notify date is expired, prompt to user
   		    if( HelperFunctions.CmpDatePrefix(AddOneNote.NotifyTime, Now)<0 )
   		    {
   			    showDialog(ProjectConst.Check_NotifyDate_Dlg);
   			    return;
   		    }
   	    }
   	 
   	    // Save it to file
   	    // Get a random file name
   	    AddOneNote.NoteFilePath = UUID.randomUUID().toString()+ ProjectConst.NoteFileExt;
   	    WriteTextFile(AddNoteActivity.this, AddOneNote.NoteBody, AddOneNote.NoteFilePath);
   	    // Add database record
   	    NotesDb.CreateOneNote(AddOneNote);   
   	    // If user choose to use notify time, add the alarm
   	    if( AddOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
   	        Alarms.AddOneAlarm(AddNoteActivity.this);
   	    // Refresh widget note list
   	    HelperFunctions.RefreshWidgetNoteList(AddNoteActivity.this, NotesDb.GetAllNotes());
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
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		   case ProjectConst.Check_NoteTitle_Dlg:
			    return HelperFunctions.BuildAltertDialog(AddNoteActivity.this, R.string.prompt_title, R.string.notetitle_empty_tip);
		   case ProjectConst.Check_NotifyDate_Dlg:
			    return HelperFunctions.BuildAltertDialog(AddNoteActivity.this, R.string.prompt_title, R.string.notifydate_expire_tip);
		}
		return null;
	}
	
	// Save note
	public void WriteTextFile(Context context, String data, String title){   
		FileOutputStream fileOutStream = null;     
		OutputStreamWriter osWriter = null;             
		try {
			fileOutStream = openFileOutput(title, MODE_PRIVATE);
		    osWriter = new OutputStreamWriter(fileOutStream);       
		    osWriter.write(data);
		    osWriter.flush(); 
		} catch (Exception e) {        
			e.printStackTrace();             
			Toast.makeText(context, R.string.notenotsave_tip, Toast.LENGTH_SHORT).show();                   
        } finally {
        	try{
		       osWriter.close();
		       fileOutStream.close();
		       Toast.makeText(context, R.string.notesave_tip, Toast.LENGTH_SHORT).show();
        	} catch (IOException e) {
        	   e.printStackTrace();
            }
        	
        }
	} 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add(Menu.NONE, ITEM0, 1, "·ÅÆú").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(Menu.NONE, ITEM1, 2, "Í¼Æ¬").setIcon(android.R.drawable.ic_menu_gallery);
		menu.add(Menu.NONE, ITEM2, 3, "Â¼Òô").setIcon(android.R.drawable.ic_menu_mylocation);
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
           }
           return false;
	}
	
	// Handler return code
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == ACTIVITY_SET_NOTIFYTIME ) {
	    	if( resultCode == RESULT_OK ) {
	    		Bundle Result = data.getExtras();
	    		if( Result.isEmpty() )
	    		{
	    			AddOneNote.Use_NotifyTime = ProjectConst.No;
	    			NotifyTimeLabel.setText(R.string.nonotifytime);
	    		} else {
	    			AddOneNote.Use_NotifyTime = ProjectConst.Yes;
	    			AddOneNote.NotifyMethod = Result.getInt(OneNote.KEY_NOTIFYMETHOD);
	    			AddOneNote.NotifyDura = Result.getInt(OneNote.KEY_NOTIFYDURA);
	    			AddOneNote.RingMusic = Result.getString(OneNote.KEY_RINGMUSIC);
	    			AddOneNote.NotifyTime = HelperFunctions.String2Calenar(Result.getString(OneNote.KEY_NOTIFYTIME));
	    		    NotifyTimeLabel.setText(HelperFunctions.FormatCalendar2ReadableStr(AddOneNote.NotifyTime));
	    		}
	    	}
	    } else if ( requestCode == ACTIVITY_SET_TAGCLR ) {
	    	if( resultCode == RESULT_OK ) {
	    	    Bundle SelIdxData = data.getExtras();
	    	    AddOneNote.TagImgIdx = AddOneNote.ItemBgIdx = SelIdxData.getInt(OneNote.KEY_TAGIMG_ID);
	    	    SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.TagImgIdx], PorterDuff.Mode.MULTIPLY);
	    	    NoteBodyCtrl.getBackground().setColorFilter(NotePadPlus.ItemBgClr[AddOneNote.ItemBgIdx], PorterDuff.Mode.MULTIPLY);
	    	}
	    }
	}
}
