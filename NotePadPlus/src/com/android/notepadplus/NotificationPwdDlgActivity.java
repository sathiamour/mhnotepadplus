package com.android.notepadplus;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NotificationPwdDlgActivity extends Activity {
    
	/** Dialog id */
	private static final int OrignalPwdErr_Dlg = 1;
	
	// Row id
	private int NoteRowId = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notificationpwd_dlg);
        
        // Get passed parameter
        Bundle Parameters = getIntent().getExtras();
		if( Parameters != null )
			NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
		Log.d("log","in NotificationPwdDlgActivity");
        Button Confirm =(Button)findViewById(R.id.chk_pwd_confirm);
        Confirm.setWidth(NotePadPlus.ScreenWidth/2);
        Confirm.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			EditText Pwd_Orignal = (EditText)findViewById(R.id.chk_pwd_edit);
    			if (NoteRowId != 0 ) {
		        	NoteDbAdapter NotesDb = new NoteDbAdapter(NotificationPwdDlgActivity.this);
		    		NotesDb.open();
		        	// Check orignal password
		    		Cursor Note = NotesDb.GetOneNote(NoteRowId);
		    		if( !Pwd_Orignal.getText().toString().equals(Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PWD))))
		    		{
		    			showDialog(OrignalPwdErr_Dlg);
		    			NotesDb.close();
		    			return;
		    		} else {
				        NotesDb.close();
				        Intent EditNoteIntent = new Intent(NotificationPwdDlgActivity.this, EditNoteActivity.class);
				        EditNoteIntent.putExtra(OneNote.KEY_ROWID, NoteRowId);
				        EditNoteIntent.putExtra(EditNoteActivity.KEY_SOURCE, Alarms.ALARM_ALERT_ACTION);
				        startActivity(EditNoteIntent);
		    		}
                }	
		        
		        finish();
    		}
       	});
        
        Button Cancel=(Button)findViewById(R.id.chk_pwd_cancel);
        Cancel.setWidth(NotePadPlus.ScreenWidth/2);
        Cancel.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	finish();				
    		}
    	});
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
            case OrignalPwdErr_Dlg:
            	 return HelperFunctions.BuildAltertDialog(NotificationPwdDlgActivity.this, R.string.pwderr_title, R.string.orignalpwd_err_prompt);
		}
		
		return null;
	}

}
