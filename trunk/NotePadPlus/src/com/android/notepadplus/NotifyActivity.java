package com.android.notepadplus;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NotifyActivity extends Activity {

	    private int RowId;
	    private Button DismissBtn;
	    @Override
		protected void onCreate(Bundle savedInstanceState) {
		    requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(savedInstanceState);
			
			setContentView(R.layout.note_alarm_notify);
			
			Bundle Parameters = getIntent().getExtras();
			if( Parameters != null ) 
				RowId = Parameters.getInt(OneNote.KEY_ROWID);
			
			DismissBtn = (Button)findViewById(R.id.dismiss_notify);
			DismissBtn.setOnClickListener(new OnClickListener(){
    		     public void onClick(View v){
    		    	    // Stop playing music service
    		    	    Intent StopService = new Intent(ProjectConst.ALARM_ALERT_ACTION);
    			    	stopService(StopService);
    			    	
    			    	Alarms.DisableDbNoteAlarm(NotifyActivity.this, RowId);
    		     }
			});
			
	    }
}
