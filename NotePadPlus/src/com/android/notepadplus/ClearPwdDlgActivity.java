package com.android.notepadplus;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ClearPwdDlgActivity extends Activity {
    
	/** Dialog id */
	private static final int OrignalPwdErr_Dlg = 1;
	
	// Row id
	private int NoteRowId = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clearpassword_dlg);
        
        // Get passed parameter
        Bundle Parameters = getIntent().getExtras();
		if( Parameters != null )
			NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);

        Button Confirm =(Button)findViewById(R.id.clrpwd_confirm);
        Confirm.setWidth(NotePadPlus.ScreenWidth/2);
        Confirm.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			EditText Pwd_Orignal = (EditText)findViewById(R.id.pwd_edit);
    			if (NoteRowId != 0 ) {
		        	NoteDbAdapter NotesDb = new NoteDbAdapter(ClearPwdDlgActivity.this);
		    		NotesDb.open();
		        	// Check orignal password
		    		Cursor Note = NotesDb.GetOneNote(NoteRowId);
		    		if( !Pwd_Orignal.getText().toString().equals(Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PWD))))
		    		{
		    			showDialog(OrignalPwdErr_Dlg);
		    			NotesDb.close();
		    			return;
		    		}
		    		// clear note's password
			        NotesDb.SetNotePwd(NoteRowId, ProjectConst.EmptyStr);
			        NotesDb.close();
                }	
    			ClearPwdDlgActivity.this. setResult(RESULT_OK);
		        finish();
    		}
       	});
		
        Button Cancel=(Button)findViewById(R.id.clrpwd_cancel);
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
            	 return HelperFunctions.BuildAltertDialog(ClearPwdDlgActivity.this, R.string.pwderr_title, R.string.orignalpwd_err_prompt);
		}
		
		return null;
	}

}
