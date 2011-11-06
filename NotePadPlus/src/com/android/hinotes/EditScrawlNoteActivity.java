package com.android.hinotes;

import java.util.Calendar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BlurMaskFilter;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditScrawlNoteActivity extends Activity 
	   implements ColorPickerDialog.OnColorChangedListener {
	// Content view
	private FingerPaintView ContentView;
	private LinearLayout FootPanel;
	private LinearLayout EditPanel;
	private TextView NotifyTimeLabel;
	
	// Activity parameters
	private OneNote EditOneNote;
	private String Pre_UseNotifyTime;
	private Calendar Pre_NotifyTime;
	
	// Database
	private NoteDbAdapter NotesDb;
	
	// Graphic object
	private Paint       mPaint;
	private MaskFilter  mEmboss;
	private MaskFilter  mBlur;
	
	// Note has been deleted
	boolean IsDeleted;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.editmultimedianote);

	    // Initialize & open database
	    NotesDb = new NoteDbAdapter(this);
	    NotesDb.open();
	    
	    // Paint objects
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);
        
        mEmboss = new EmbossMaskFilter(new float[] {1, 1, 1}, 0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        
        // Controls
	    ContentView = (FingerPaintView) findViewById(R.id.scraw_content);
	    FootPanel = (LinearLayout)findViewById(R.id.foot_btn_panel);
	    EditPanel = (LinearLayout)findViewById(R.id.edit_panel);
        NotifyTimeLabel = (TextView)findViewById(R.id.notifytime_text);
        Button ClrPick = (Button)findViewById(R.id.sel_pen_clr);
        Button Emboss = (Button)findViewById(R.id.emboss);
        Button Blur = (Button)findViewById(R.id.blur);
        Button Erase = (Button)findViewById(R.id.erase);
        Button SrcTop = (Button)findViewById(R.id.srctop);
        Button EditBtn = (Button)findViewById(R.id.editnotebtn);
        TextView DelPrompt = (TextView)findViewById(R.id.notedel_prompt);

       
	    // Get parameters from intent
	    EditOneNote = new OneNote(OneNote.ScrawlNote);
	    Bundle Parameters = getIntent().getExtras();
	    if( Parameters != null ) {
		    String Source = Parameters.getString(ProjectConst.KEY_SOURCE);
		    int RowId = Parameters.getInt(OneNote.KEY_ROWID);
		    // It is from notification
		    if( Source.equals(ProjectConst.ALARM_ALERT_ACTION) )
		    {
			    // We are from notify alarm, so since it has been in edit note activity
			    // Stop playing music service
			    // Stop notify alarm and set next one
    	        Intent StopService = new Intent(ProjectConst.ALARM_NOTIFY_RING);
	    	    stopService(StopService);
			    Alarms.DeleteOneAlarm(this, RowId);
			    // Remove notification on bar
			    NotificationManager notificationManager = (NotificationManager)getSystemService(android.content.Context.NOTIFICATION_SERVICE);
			    notificationManager.cancel(RowId);

		    } else if( Source.equals(ProjectConst.USERDEF_EDIT_ACTION) ) {
			    // if it is from user def view, to get position index in parent layout
		        EditOneNote.PosIdx = Parameters.getInt(OneNote.KEY_INDEX);
		    }
		
		    // Initialize the note with database 
		    Cursor Note = NotesDb.GetOneNote(RowId);
		    if( Note.getCount() == 1 )
			    EditOneNote.InitializatonFromDb(Note);
            else {
			    IsDeleted = true;
			    EditOneNote.NoteTitle = getString(R.string.note_hasdel_prompt);
			    EditPanel.setVisibility(View.GONE);
			    ContentView.setVisibility(View.GONE);
            }
			
		    // Backup the notify time
		    Pre_UseNotifyTime = EditOneNote.Use_NotifyTime;
		    Pre_NotifyTime = EditOneNote.NotifyTime;

		    // Set note title
		    DelPrompt.setText(EditOneNote.NoteTitle);

		    if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) && HelperFunctions.CmpDatePrefix2(EditOneNote.NotifyTime, Calendar.getInstance())>0 )
			    NotifyTimeLabel.setText(HelperFunctions.FormatCalendar2ReadableStr(EditOneNote.NotifyTime));
	    }

	    ContentView.InitFingerPaintView(HelperFunctions.CreateBgFromFile(EditOneNote.NoteFilePath), mPaint, NotePadPlus.ItemBgClr[EditOneNote.DrawableResIdx]);
        
	    // Graphic edit
	    ClrPick.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				   new ColorPickerDialog(EditScrawlNoteActivity.this, EditScrawlNoteActivity.this, mPaint.getColor()).show();
			}
        	
        });
        
        Emboss.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mPaint.setXfermode(null);
		        mPaint.setAlpha(0xFF);
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
			}
        	
        });
        
        Blur.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mPaint.setXfermode(null);
		        mPaint.setAlpha(0xFF);
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
			}
        	
        });
        
        
        Erase.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mPaint.setMaskFilter(null);
				mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
				mPaint.setColor(NotePadPlus.ItemBgClr[EditOneNote.DrawableResIdx]);
				//mPaint.setAlpha(100);
		        //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			}
        	
        });
        
        SrcTop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//mPaint.setXfermode(null);
		        //mPaint.setAlpha(0xFF);
		        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
				
			}
        	
        });
        
        // Edit button
	    EditBtn.setOnClickListener(new OnClickListener(){
		    public void onClick(View v){
			       StartEditMode();
		}
	    });
    }
	
	/** When the activity is destroyed, close database*/
    @Override
    protected void onDestroy(){
    	if( NotesDb != null )
    		NotesDb.close(); 
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
		menu.add(Menu.NONE, ProjectConst.ITEM0, 1, "放弃").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(Menu.NONE, ProjectConst.ITEM1, 2, "提醒").setIcon(R.drawable.ic_menu_reminder);
		menu.add(Menu.NONE, ProjectConst.ITEM2, 3, "颜色").setIcon(android.R.drawable.ic_menu_view);
		menu.add(Menu.NONE, ProjectConst.ITEM3, 4, "标题").setIcon(R.drawable.ic_menu_compose);
		menu.add(Menu.NONE, ProjectConst.ITEM4, 5, "锁定").setIcon(R.drawable.ic_menu_lock);
		menu.add(Menu.NONE, ProjectConst.ITEM5, 6, "分享").setIcon(android.R.drawable.ic_menu_share);
		
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case ProjectConst.ITEM0:
            	 setResult(RESULT_CANCELED);
	             finish();
	             break;
            case ProjectConst.ITEM1:
            	 StartNotifyActivity();
                 break;
            case ProjectConst.ITEM2:
            	 Intent intent = new Intent();
        		 intent.setClass(this, SetItemClrActivity.class);
        		 intent.putExtra(SetItemClrActivity.Key_ClrType, SetItemClrActivity.Val_ItemType_Bg);
        		 startActivityForResult(intent, ProjectConst.ACTIVITY_SET_TAGCLR);
                 break;
            case ProjectConst.ITEM3:
            	 showDialog(ProjectConst.Input_Title_Dlg);
                 break;
            case ProjectConst.ITEM4:
            	 Intent PwdDlgIntent = new Intent(this, PwdDlgActivity.class);
			     startActivityForResult(PwdDlgIntent, ProjectConst.ACTIVITY_SET_PWD);
         	     break;
            case ProjectConst.ITEM5:
       	         HelperFunctions.SaveBmpPicture(ContentView.getFingerPaint(), EditOneNote.NoteFilePath);
                 showDialog(ProjectConst.ShareBy_Dlg);
     	         break;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override 
	public void onBackPressed(){
		 // If the note has been deleted, just return
		 if( IsDeleted )
		 {
			 setResult(RESULT_CANCELED);
			 finish();
			 return;
		 }
		 
    	 // Save it to original file
		 HelperFunctions.SaveBmpPicture(ContentView.getFingerPaint(), EditOneNote.NoteFilePath);
    	 // Update database record
    	 NotesDb.UpdateOneNote(EditOneNote); 
    	 
    	 // Update the specific alarm if user has changed the notify time
    	 if( !Pre_UseNotifyTime.equals(EditOneNote.Use_NotifyTime) )
    	 {
        	 // From Y(use notify) to N(don't use notify), User cancels alarm
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.No) )
    		 {
    		     Log.d(ProjectConst.TAG,"EditNoteActivity: delete alarm");
    		     Alarms.DeleteOneAlarm(this, EditOneNote.NoteRowId);
    		 }
    		 // From N(don't use notify) to Y(use notify), add one alarm
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
    		 {
    			 Log.d(ProjectConst.TAG,"EditNoteActivity: add alarm");
    			 NotesDb.UpdateNoteNotifyRingTime(EditOneNote.NoteRowId, EditOneNote.NotifyTime);
        		 Alarms.AddOneAlarm(this);
    		 }
    	 } else {
    		 // User changes the notify time
    		 if( EditOneNote.Use_NotifyTime.equals(ProjectConst.Yes) && HelperFunctions.CmpDatePrefix2(Pre_NotifyTime, EditOneNote.NotifyTime) != 0 )
    		 { 
        		 Log.d(ProjectConst.TAG,"EditNoteActivity: update alarm");
    		     NotesDb.UpdateNoteNotifyRingTime(EditOneNote.NoteRowId, EditOneNote.NotifyTime);
                 Alarms.UpdateOneAlarm(this, EditOneNote.NoteRowId);
    	     }
    	 }
    	 
    	 // Refresh widget note list
    	 HelperFunctions.RefreshWidgetNoteList(this, NotesDb.GetAllNotes());
    	 // Refresh 1x1 widget
    	 if( EditOneNote.WidgetId != 0 )
    	     HelperFunctions.Refresh1x1Widget(this, EditOneNote.WidgetId, EditOneNote.NoteTitle, EditOneNote.NoteRowId, EditOneNote.DrawableResIdx, EditOneNote.Password.length()>0, EditOneNote.NoteType);
    	 // Show toast to notify user settings have been saved
 	     Toast.makeText(this, getString(R.string.notesavingtip), Toast.LENGTH_SHORT).show();
         // Return to main activity
 	     Intent ReturnBackData = new Intent();
	     ReturnBackData.putExtra(OneNote.KEY_TITLE, EditOneNote.NoteTitle);
	     ReturnBackData.putExtra(OneNote.KEY_INDEX, EditOneNote.PosIdx);
	     ReturnBackData.putExtra(OneNote.KEY_DRAWABLE_ID, EditOneNote.DrawableResIdx);
	     setResult(RESULT_OK, ReturnBackData);
   	     finish(); 
	}
	 
	// Go to edit mode
	private void StartEditMode()
	{
		EditPanel.setVisibility(View.GONE);
		FootPanel.setVisibility(View.VISIBLE);
	}

    private void StartNotifyActivity()
	{
    	// start notify time activity
    	Intent intent = new Intent();
		intent.setClass(this, NotifyDateActivity.class);
		// Set time
		Bundle Parameters = new Bundle();
		Parameters.putString(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(EditOneNote.NotifyTime));
		Parameters.putInt(OneNote.KEY_NOTIFYDURA, EditOneNote.NotifyDura);
		Parameters.putString(OneNote.KEY_RINGMUSIC, EditOneNote.RingMusic);
		Parameters.putInt(OneNote.KEY_NOTIFYMETHOD, EditOneNote.NotifyMethod);
        // Pass it to next activity 
		intent.putExtras(Parameters);
		// Go to next activity(set note's notify time activity)
		startActivityForResult(intent, ProjectConst.ACTIVITY_SET_NOTIFYTIME);		
	}
	
	@Override
	public void colorChanged(int color) {
		mPaint.setColor(color);
    }
}
