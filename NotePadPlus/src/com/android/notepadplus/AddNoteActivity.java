package com.android.notepadplus;


import java.util.UUID;
import com.android.notepadplus.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.app.Dialog;

public class AddNoteActivity extends Activity {
	
	/** Control */
	private EditText NoteTitleCtrl = null;
	private EditText NoteBodyCtrl = null;
	private TextView NotifyTimeLabel = null;
	private Button   SelectTagClrBtn = null;
	private LinearLayout AddPanel;
	

	/** Database */
	private NoteDbAdapter NotesDb = null;
	
	/** One note */
	private OneNote AddOneNote;
	
	// Width&Height
	private int ScreenHeight;
	private int ScreenWidth;
	
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
		NoteBodyCtrl = (EditText)findViewById(R.id.add_body_content);
		NotifyTimeLabel = (TextView)findViewById(R.id.notifytime_text);
		AddPanel = (LinearLayout)findViewById(R.id.addnote_panel);
		
		// Get screen resolution
		DisplayMetrics ScreenMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);
		ScreenHeight = ScreenMetrics.heightPixels;
		ScreenWidth = ScreenMetrics.widthPixels;
		
		// Randomly select color
    	SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
    	AddPanel.setBackgroundDrawable(HelperFunctions.CreateTitleBarBg(ScreenWidth, ScreenHeight, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx], NotePadPlus.TagClr[AddOneNote.DrawableResIdx])); 

		// Set text changed listener
		NoteTitleCtrl.addTextChangedListener(new TextWatcher(){  
	        @Override  
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}  
	  
	        @Override  
	        public void onTextChanged(CharSequence s, int start, int before, int count) {  
	            AddOneNote.NoteTitle = s.toString();
	        }

			@Override
			public void afterTextChanged(Editable arg0) {}  
	    });
		
    	NoteBodyCtrl.addTextChangedListener(new TextWatcher(){  	  
	        @Override  
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}  
	  
	        @Override  
	        public void onTextChanged(CharSequence s, int start, int before, int count) {  
	        	AddOneNote.NoteBody = s.toString();
	        }

			@Override
			public void afterTextChanged(Editable arg0) {}     
	    });
    	
    	// Set tag color
    	SelectTagClrBtn.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	Intent intent = new Intent();
				intent.setClass(AddNoteActivity.this, SetTagClrActivity.class);
				startActivityForResult(intent, ProjectConst.ACTIVITY_SET_TAGCLR);				
    		}
    	});

    	// Handle Action_Send sys action
    	Intent PreIntent = getIntent();
    	if( PreIntent.getAction() != null && PreIntent.getAction().equals(Intent.ACTION_SEND) )
    	{
    	    Bundle Parameters = PreIntent.getExtras();
    	    AddOneNote.NoteTitle = Parameters.getString(Intent.EXTRA_SUBJECT);
    	    AddOneNote.NoteBody = Parameters.getString(Intent.EXTRA_TEXT);
    	    
    	    NoteTitleCtrl.setText(AddOneNote.NoteTitle);
    	    NoteBodyCtrl.setText(AddOneNote.NoteBody); 
    	}
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
   	 
   	    // Save it to file
   	    // Get a random file name
   	    AddOneNote.NoteFilePath = UUID.randomUUID().toString()+ ProjectConst.NoteFileExt;
   	    HelperFunctions.WriteTextFile(AddNoteActivity.this, AddOneNote.NoteBody, AddOneNote.NoteFilePath);
   	    // Add database record
   	    NotesDb.CreateOneNote(AddOneNote);   
   	    // If user choose to use notify time, add the alarm
   	    if( AddOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
   	        Alarms.AddOneAlarm(AddNoteActivity.this);
   	    // Refresh widget note list
   	    HelperFunctions.RefreshWidgetNoteList(AddNoteActivity.this, NotesDb.GetAllNotes());
   	    // Return to main activity
        // Set return code
	    Intent ReturnBackData = new Intent();
	    ReturnBackData.putExtra(OneNote.KEY_TITLE, AddOneNote.NoteTitle);
	    ReturnBackData.putExtra(OneNote.KEY_ROWID, AddOneNote.NoteRowId);
	    ReturnBackData.putExtra(OneNote.KEY_DRAWABLE_ID, AddOneNote.DrawableResIdx);
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
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		   case ProjectConst.Check_NoteTitle_Dlg:
			    return HelperFunctions.BuildAltertDialog(AddNoteActivity.this, R.string.prompt_title, R.string.notetitle_empty_tip);
		   case ProjectConst.Set_NotifyDate_Dlg:
			    return HelperFunctions.BuildAltertDialog(AddNoteActivity.this, R.string.prompt_title, R.string.notifydate_expire_tip);
		   case ProjectConst.ShareBy_Dlg:
			    return HelperFunctions.BuildShareByDlg(this, R.string.shareby_title, AddOneNote.NoteTitle, AddOneNote.NoteBody);

		}
		return null;
	}
	
    private void StartNotifyActivity()
	{
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
		startActivityForResult(intent, ProjectConst.ACTIVITY_SET_NOTIFYTIME);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add(Menu.NONE, ProjectConst.ITEM0, 1, "·ÅÆú").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(Menu.NONE, ProjectConst.ITEM1, 2, "ÌáÐÑ").setIcon(R.drawable.ic_menu_reminder);
		menu.add(Menu.NONE, ProjectConst.ITEM2, 3, "Ëø¶¨").setIcon(R.drawable.ic_menu_lock);
		menu.add(Menu.NONE, ProjectConst.ITEM3, 4, "·ÖÏí").setIcon(android.R.drawable.ic_menu_share);
		//menu.add(Menu.NONE, ITEM1, 2, "Í¼Æ¬").setIcon(android.R.drawable.ic_menu_gallery);
		//menu.add(Menu.NONE, ITEM2, 3, "Â¼Òô").setIcon(android.R.drawable.ic_menu_mylocation);
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
            	   Intent PwdDlgIntent = new Intent(this, PwdDlgActivity.class);
 			       startActivityForResult(PwdDlgIntent, ProjectConst.ACTIVITY_SET_PWD);
            	   break;
              case ProjectConst.ITEM3:
           	       showDialog(ProjectConst.ShareBy_Dlg);
            	   break;
            
           }
           return false;
	}
	
 
	// Handler return code
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == ProjectConst.ACTIVITY_SET_NOTIFYTIME ) {
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
	    } else if ( requestCode == ProjectConst.ACTIVITY_SET_TAGCLR && resultCode == RESULT_OK) {
	    	    Bundle SelIdxData = data.getExtras();
	    	    AddOneNote.DrawableResIdx = SelIdxData.getInt(OneNote.KEY_DRAWABLE_ID);
	    	    SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
	    	    AddPanel.setBackgroundDrawable(HelperFunctions.CreateTitleBarBg(ScreenWidth, ScreenHeight, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx], NotePadPlus.TagClr[AddOneNote.DrawableResIdx]));
	    } else if( requestCode == ProjectConst.ACTIVITY_SET_PWD && resultCode == RESULT_OK )
	    	    AddOneNote.Password = data.getStringExtra(OneNote.KEY_PWD); 
	}
}
