package com.android.notepadplus;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PwdDlgActivity extends Activity {
    
	/** Dialog id */
	private static final int PwdErr_Dlg = 1;
	private static final int PwdNull_Dlg = PwdErr_Dlg+1;
	
	// Row id
	private int NoteRowId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_dlg);
        
        // Get passed parameter
        Bundle Parameters = getIntent().getExtras();
		if( Parameters != null )
			NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
        Log.d("log","rowid "+NoteRowId);
        Button Confirm =(Button)findViewById(R.id.pwd_confirm);
        Confirm.setWidth(NotePadPlus.ScreenWidth/2);
        Confirm.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			EditText Pwd_First = (EditText)findViewById(R.id.pwd_first_edit);
		        EditText Pwd_Second = (EditText)findViewById(R.id.pwd_second_edit);
		      
		        // Empty check
		        if( Pwd_First.getText().toString().length() == 0 )
		        {
		        	showDialog(PwdNull_Dlg);
		        	return;
		        }
		        // Is same ?
		        if( !Pwd_First.getText().toString().equals(Pwd_Second.getText().toString())) {
			        showDialog(PwdErr_Dlg);
			        return;
		        } else if (NoteRowId != 0 ) {
		    		// save to note database
		        	NoteDbAdapter NotesDb = new NoteDbAdapter(PwdDlgActivity.this);
		    		NotesDb.open();
			        NotesDb.SetNotePwd(NoteRowId, Pwd_First.getText().toString());
			        NotesDb.close();
                }	
		        PwdDlgActivity.this.setResult(RESULT_OK, null);
		        finish();
    		}
       	});
        
        Button Cancel=(Button)findViewById(R.id.pwd_cancel);
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
            case PwdErr_Dlg:
			     return HelperFunctions.BuildAltertDialog(PwdDlgActivity.this, R.string.pwderr_title, R.string.pwderr_prompt);
            case PwdNull_Dlg:
            	 return HelperFunctions.BuildAltertDialog(PwdDlgActivity.this, R.string.pwderr_title, R.string.pwdnull_prompt);
		}
		
		return null;
	}

}
