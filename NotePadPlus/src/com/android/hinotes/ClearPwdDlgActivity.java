package com.android.hinotes;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ClearPwdDlgActivity extends Activity {
    
	// Row id
	private int NoteRowId = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.clearpassword_dlg);
        
        // Get passed parameter
        Bundle Parameters = getIntent().getExtras();
		if( Parameters != null )
			NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);

        Button Confirm =(Button)findViewById(R.id.clrpwd_confirm);
        Confirm.setWidth(NotePadPlus.ScreenWidthDip/2);
        Confirm.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			EditText Pwd_Orignal = (EditText)findViewById(R.id.pwd_edit);
    			if (NoteRowId != 0 ) {
		        	NoteDbAdapter NotesDb = new NoteDbAdapter(ClearPwdDlgActivity.this);
		    		NotesDb.open();
		        	// Check original password
		    		Cursor Note = NotesDb.GetOneNote(NoteRowId);
		    		if( !Pwd_Orignal.getText().toString().equals(Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PWD))))
		    		{
		    			showDialog(ProjectConst.OrignalPwdErr_Dlg);
		    			NotesDb.close();
		    			return;
		    		}
		    		// Clear note's password
			        NotesDb.SetNotePwd(NoteRowId, ProjectConst.EmptyStr);
			        // Close database			    	
			        NotesDb.close();
                }	
    			setResult(RESULT_OK);
		        finish();
    		}
       	});
		
        Button Cancel=(Button)findViewById(R.id.clrpwd_cancel);
        Cancel.setWidth(NotePadPlus.ScreenWidthDip/2);
        Cancel.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	finish();				
    		}
    	});
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
            case ProjectConst.OrignalPwdErr_Dlg:
            	 return HelperFunctions.BuildAltertDialog(ClearPwdDlgActivity.this, R.string.pwderr_title, R.string.orignalpwd_err_prompt);
		}
		
		return null;
	}

}
