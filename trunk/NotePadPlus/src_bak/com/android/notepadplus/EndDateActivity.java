package com.android.notepadplus;


import java.util.Calendar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker.OnDateChangedListener; 
import android.widget.TextView;



public class EndDateActivity extends Activity {
	
	/** The parameter passed */
	private Calendar SelectedTime = Calendar.getInstance();
	private String Use_EndTime = ProjectConst.No;
	private String DelNoteExp = ProjectConst.No;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enddate_picker);
        
        // Views
        final CheckBox WetherUseEndDate = (CheckBox)findViewById(R.id.wether_use_enddate);
        final CheckBox WetherDelNote = (CheckBox)findViewById(R.id.wether_delnote_expire);
        final DatePicker EndDate = (DatePicker)findViewById(R.id.enddate);
        final TextView  EndDateText = (TextView)findViewById(R.id.time_text);
        
        // Get passed parameters
        Bundle Parameters = getIntent().getExtras();
		if( Parameters != null ) {
			Use_EndTime = Parameters.getString(OneNote.KEY_USE_ENDTIME);
			DelNoteExp = Parameters.getString(OneNote.KEY_DELNOTE_EXP);
			if( Use_EndTime.equals(ProjectConst.Yes) ) {
			    SelectedTime = HelperFunctions.String2Calenar(Parameters.getString(OneNote.KEY_ENDTIME));
			    WetherUseEndDate.setChecked(true);
			    if( DelNoteExp.equals(ProjectConst.Yes) )
			    	WetherDelNote.setChecked(true);  
			}
		}
		EndDateText.setText(HelperFunctions.FormatCalendar2ReadablePrefixStr(SelectedTime));
		
		// Set time and action listener
        EndDate.init(SelectedTime.get(Calendar.YEAR), SelectedTime.get(Calendar.MONTH), SelectedTime.get(Calendar.DAY_OF_MONTH),
        		     new OnDateChangedListener() {  
        	              public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) { 
        	            	  SelectedTime.set(arg1, arg2, arg3);
        	            	  EndDateText.setText(arg1 + "Äê" + (arg2 + 1) + "ÔÂ" + arg3 + "ÈÕ");        	   
        	             }  
                    }
        );  
        
        // Set check box
        WetherDelNote.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		if( isChecked ) 
        		    DelNoteExp = ProjectConst.Yes;
        		else
        			DelNoteExp = ProjectConst.No;
        	}
        });
        
        WetherUseEndDate.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		if( isChecked ) 
        			Use_EndTime = ProjectConst.Yes;
        		else
        			Use_EndTime = ProjectConst.No;
        	}
        });
        
    }
    
    @Override 
    public void onBackPressed(){
    	   // Set return data
   	      Bundle Result = new Bundle();
   	      if( Use_EndTime.equals(ProjectConst.Yes) )
   	      {
   	    	  Result.putString(OneNote.KEY_DELNOTE_EXP, DelNoteExp);
   	    	  Result.putString(OneNote.KEY_ENDTIME, HelperFunctions.Calendar2String(SelectedTime));
   	      }
   	      // Set return code
   	      Intent ReturnBackData = new Intent();
   	      ReturnBackData.putExtras(Result);
   	      EndDateActivity.this.setResult(RESULT_OK, ReturnBackData);
          // Show toast to notify user settings have been saved
  	      Toast.makeText(EndDateActivity.this, getString(R.string.enddatesavingtip), Toast.LENGTH_SHORT).show();
          // Return to launching activity
   	      finish();
   }
}