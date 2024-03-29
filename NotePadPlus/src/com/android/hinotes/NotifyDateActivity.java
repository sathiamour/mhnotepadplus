package com.android.hinotes;


import java.util.Calendar;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;



public class NotifyDateActivity extends Activity {
	
	/** Date picker controls */
	private Button Date_Picker = null;
	private Button Minute_Picker = null;
	private Button RingMusicSel = null;
	private Spinner RingTypeSel = null;
	private Spinner RingMethodTypeSel = null;
	
	/** Variables for save selected time */
	private Calendar SelectedTime = Calendar.getInstance();
	//private String Use_NotifyTime = ProjectConst.No;
	private int NotifyDura = ProjectConst.Zero;
	private int NotifyMethod = ProjectConst.Zero;
	private String RingMusic = ProjectConst.EmptyStr;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifydate_picker);
        
        // Views initialization
        Date_Picker = (Button)findViewById(R.id.notifytime_date_picker);
        Minute_Picker = (Button)findViewById(R.id.notifytime_minute_picker);
        RingTypeSel = (Spinner)findViewById(R.id.RingType);
        RingMusicSel = (Button)findViewById(R.id.RingMusicType);
        RingMethodTypeSel = (Spinner)findViewById(R.id.NotifyMethodType);

        Button Confirm =(Button)findViewById(R.id.save_notifytime);
        Button Cancel = (Button)findViewById(R.id.canel_notifytime);
        Button Remove = (Button)findViewById(R.id.remove_notifytime);
        
        // Get passed parameters
        Bundle Parameters = getIntent().getExtras();
		if( Parameters != null ) {
			//Use_NotifyTime = Parameters.getString(OneNote.KEY_USE_NOTIFYTIME);
		    SelectedTime = HelperFunctions.String2Calenar(Parameters.getString(OneNote.KEY_NOTIFYTIME));
			NotifyDura = Parameters.getInt(OneNote.KEY_NOTIFYDURA);
			NotifyMethod = Parameters.getInt(OneNote.KEY_NOTIFYMETHOD);
			RingMusic = Parameters.getString(OneNote.KEY_RINGMUSIC);
		} 
		
        // Set title & selections
	    Date_Picker.setText(HelperFunctions.FormatCalendar2ReadablePrefixStr(SelectedTime));
	    Minute_Picker.setText(HelperFunctions.FormatCalendar2ReadableSuffixStr(SelectedTime));
	    RingTypeSel.setSelection(NotifyDura);
	    RingMethodTypeSel.setSelection(NotifyMethod);
	    
        // Set notify date button 
        DisplayMetrics ScreenMetrics = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);   
        int ScreenWidth = ScreenMetrics.widthPixels;
        Date_Picker.setWidth(ScreenWidth*2/3);
        Minute_Picker.setWidth(ScreenWidth/3);
        
        // Set ring duration type content
        ArrayAdapter<String> TypeContent = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, OneNote.RingTypeStr);
        TypeContent.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        RingTypeSel.setAdapter(TypeContent);
        RingTypeSel.setSelection(NotifyDura);
        RingTypeSel.setOnItemSelectedListener(new OnItemSelectedListener(){
			        @Override
			        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			        	        NotifyDura = arg2;
				    }

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}
	    });

        // Set ring music type content
        if( RingMusic.length() == 0 )
            RingMusicSel.setText(OneNote.SilentMusicTitle);
        else
        	RingMusicSel.setText(RingtoneManager.getRingtone(this,Uri.parse(RingMusic)).getTitle(this));
        
        RingMusicSel.setOnClickListener(new OnClickListener(){
		     public void onClick(View v){
		    	 Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);  
                 intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM); 
                 intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择提示铃音");  
                 if( RingMusic.length() > 0 )
                     intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(RingMusic));
                 startActivityForResult(intent, ProjectConst.ACTIVITY_SEL_NOTIFYMUSIC);
		     }
        });
        
        // Set notify method type content
        ArrayAdapter<String> MethodTypeContent = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, OneNote.NotifyMethodTypeStr);
        MethodTypeContent.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        RingMethodTypeSel.setAdapter(MethodTypeContent);
        RingMethodTypeSel.setSelection(NotifyMethod);
        RingMethodTypeSel.setOnItemSelectedListener(new OnItemSelectedListener(){
	        @Override
	        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	        	        NotifyMethod = arg2;
		    }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
        });
        
		// Set time and action listener
		Date_Picker.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				showDialog(ProjectConst.Date_Picker_Dlg);
			}
		});        
		Minute_Picker.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				showDialog(ProjectConst.Minute_Picker_Dlg);
			}
		});   
		
		// Buttons
		Confirm.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
		    	   // Set return data, if saving fails, just return
		    	   if( !SaveConfigure() )
		    		   return;
		           // Return to launching activity
		   	       finish();
		   	}
		});
		
		Cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();				
			}
		});
		
		Remove.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SelectedTime = Calendar.getInstance();
				Date_Picker.setText(HelperFunctions.FormatCalendar2ReadablePrefixStr(SelectedTime));
				Minute_Picker.setText(HelperFunctions.FormatCalendar2ReadableSuffixStr(SelectedTime));
  	       }
		});
        
    }
    
    @Override 
    public void onBackPressed(){
    	   // Set return data, if saving fails, just return
    	   if( !SaveConfigure() )
    		   return;
           // Return to launching activity
   	       finish();
    }
    
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
        if( requestCode == ProjectConst.ACTIVITY_SEL_NOTIFYMUSIC && resultCode == RESULT_OK ) { 
            Uri Ring = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if( Ring != null ) // Maybe user selects the "Silent"
            {
                Ringtone ringtone = RingtoneManager.getRingtone(getBaseContext(), Ring);
                RingMusic = Ring.toString();
                RingMusicSel.setText(ringtone.getTitle(getBaseContext()));
            } else {
            	RingMusic = ProjectConst.EmptyStr;
            	RingMusicSel.setText(OneNote.SilentMusicTitle); 
            }
            	
        }  
    }
    
    @Override
    protected Dialog onCreateDialog(int id){
        switch(id){
             case ProjectConst.Date_Picker_Dlg:
                  return new DatePickerDialog(this, 
                		      new OnDateSetListener() {
                                  @Override
                                  public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                	          Date_Picker.setText(year+"年"+(monthOfYear+1)+"月"+dayOfMonth+"日");
                                	          SelectedTime.set(year, monthOfYear, dayOfMonth, SelectedTime.get(Calendar.HOUR_OF_DAY), SelectedTime.get(Calendar.MINUTE)); 
                              }},
                		      SelectedTime.get(Calendar.YEAR), 
    			              SelectedTime.get(Calendar.MONTH), 
    			              SelectedTime.get(Calendar.DAY_OF_MONTH));

              case ProjectConst.Minute_Picker_Dlg:
            	   return new TimePickerDialog(this, 
            			     new OnTimeSetListener() {
                                 @Override
                                 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                	         Minute_Picker.setText(hourOfDay+"时"+minute+"分");
                                	         SelectedTime.set(SelectedTime.get(Calendar.YEAR),
                                	        		          SelectedTime.get(Calendar.MONDAY),
                                	        		          SelectedTime.get(Calendar.DAY_OF_MONTH),
                                	        		          hourOfDay, 
                                	        		          minute); 

                                 }
                             }, SelectedTime.get(Calendar.HOUR_OF_DAY), SelectedTime.get(Calendar.MINUTE), true);
            
              case ProjectConst.NotifyTime_Err_Dlg:
            	   return HelperFunctions.BuildAltertDialog(this, R.string.notifytime_err_title, R.string.notifytime_err_msg);
        }
        return null;
   }
    
   private boolean SaveConfigure()
   {
	    // The notify time must be greater than now
	    if( HelperFunctions.CmpDatePrefix2(SelectedTime, Calendar.getInstance()) < 0 )
	    {
	    	showDialog(ProjectConst.NotifyTime_Err_Dlg);
	    	return false;
	    }
	    // Set return data
	    Bundle Result = new Bundle();
	    // If the notify time is greater than now, then it is a validate time, record it
	    if( HelperFunctions.CmpDatePrefix2(SelectedTime, Calendar.getInstance()) > 0 )
	    {
	        Result.putString(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(SelectedTime));
	        Result.putString(OneNote.KEY_RINGMUSIC, RingMusic);
	        Result.putInt(OneNote.KEY_NOTIFYDURA, NotifyDura);
	        Result.putInt(OneNote.KEY_NOTIFYMETHOD, NotifyMethod);
	    }
	    // Set return code
	    Intent ReturnBackData = new Intent();
	    ReturnBackData.putExtras(Result);
	    setResult(RESULT_OK, ReturnBackData);
	    // Show toast to notify user settings have been saved
	    Toast.makeText(this, getString(R.string.notifysettingsavingtip), Toast.LENGTH_SHORT).show();
	    // Yes, we have saved it
	    return true;
   }
}