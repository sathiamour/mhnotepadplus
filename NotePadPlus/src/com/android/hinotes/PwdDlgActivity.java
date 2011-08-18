package com.android.hinotes;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PwdDlgActivity extends Activity {
    
	// Row id
	private int NoteRowId = ProjectConst.NegativeOne;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.password_dlg);
        
        // Get passed parameter
        Bundle Parameters = getIntent().getExtras();
		if( Parameters != null )
			NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
       
        Button Confirm =(Button)findViewById(R.id.pwd_confirm);
        //Confirm.setWidth(NotePadPlus.ScreenWidth/2);
        Confirm.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			EditText Pwd_First = (EditText)findViewById(R.id.pwd_first_edit);
		        EditText Pwd_Second = (EditText)findViewById(R.id.pwd_second_edit);
		      
		        // Empty check
		        if( Pwd_First.getText().toString().length() == 0 )
		        {
		        	showDialog(ProjectConst.PwdEmpty_Prompt_Dlg);
		        	return;
		        }
		        // Is same ?
		        if( !Pwd_First.getText().toString().equals(Pwd_Second.getText().toString())) {
			        showDialog(ProjectConst.PwdErr_Dlg);
			        return;
		        } else if (NoteRowId != ProjectConst.NegativeOne ) {
		    		// save to note database
		        	NoteDbAdapter NotesDb = new NoteDbAdapter(PwdDlgActivity.this);
		    		NotesDb.open();
			        NotesDb.SetNotePwd(NoteRowId, Pwd_First.getText().toString());
			        NotesDb.close();
                }	
		   	       
		        Intent ReturnBackData = new Intent();
		   	    ReturnBackData.putExtra(OneNote.KEY_PWD, Pwd_First.getText().toString());
		        setResult(RESULT_OK, ReturnBackData);
		        finish();
    		}
       	});
        
        Button Cancel=(Button)findViewById(R.id.pwd_cancel);
        //Cancel.setWidth(NotePadPlus.ScreenWidth/2);
        Cancel.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
    			setResult(RESULT_CANCELED);
		    	finish();				
    		}
    	});
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
            case ProjectConst.PwdErr_Dlg:
			     return HelperFunctions.BuildAltertDialog(PwdDlgActivity.this, R.string.pwderr_title, R.string.pwderr_prompt);
            case ProjectConst.PwdEmpty_Prompt_Dlg:
            	 return HelperFunctions.BuildAltertDialog(PwdDlgActivity.this, R.string.pwderr_title, R.string.pwdnull_prompt);
		}
		
		return null;
	}

}
