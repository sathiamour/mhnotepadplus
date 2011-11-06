package com.android.hinotes;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddScrawlNoteActivity extends GraphicsActivity
        implements ColorPickerDialog.OnColorChangedListener {    

	// Content view
	private FingerPaintView ContentView;
	private TextView NotifyTimeLabel;
	/** Database */
	private NoteDbAdapter NotesDb;
	/** One note */
	private OneNote AddOneNote;
	// Graphic object
	private Paint       mPaint;
	private MaskFilter  mEmboss;
	private MaskFilter  mBlur;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addscrawlnote);
        
		// Initialize & open database
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();
		// Initialize the note
		AddOneNote = new OneNote(OneNote.ScrawlNote);
		
		// Graphic objects
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
        
        ContentView = (FingerPaintView) findViewById(R.id.scraw_content);
        NotifyTimeLabel = (TextView)findViewById(R.id.notifytime_text);
        Button ClrPick = (Button)findViewById(R.id.sel_pen_clr);
        Button Emboss = (Button)findViewById(R.id.emboss);
        Button Blur = (Button)findViewById(R.id.blur);
        Button Erase = (Button)findViewById(R.id.erase);
        Button SrcTop = (Button)findViewById(R.id.srctop);
        
        ContentView.InitFingerPaintView(mPaint, NotePadPlus.ScreenWidthDip, NotePadPlus.ScreenHeightDip, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx]);
        
        ClrPick.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				   new ColorPickerDialog(AddScrawlNoteActivity.this, AddScrawlNoteActivity.this, mPaint.getColor()).show();
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
				mPaint.setColor(NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx]);
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
    }
    
    public void colorChanged(int color) {
        mPaint.setColor(color);
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
            	 AddOneNote.NoteFilePath = HelperFunctions.MakeCameraFolder()+"/"+UUID.randomUUID().toString()+".jpg";
       	         HelperFunctions.SaveBmpPicture(ContentView.getFingerPaint(), AddOneNote.NoteFilePath);
                 showDialog(ProjectConst.ShareBy_Dlg);
     	         break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override 
	public void onBackPressed(){
    	if( ContentView.getPainted() )
    	{
    	    // Get a random file name
   	        AddOneNote.NoteFilePath = HelperFunctions.MakeCameraFolder()+"/"+UUID.randomUUID().toString()+".jpg";
   	        HelperFunctions.SaveBmpPicture(ContentView.getFingerPaint(), AddOneNote.NoteFilePath);
   	        // Add database record
   	        NotesDb.CreateOneNote(AddOneNote);
   	        // Refresh widget note list
   	        HelperFunctions.RefreshWidgetNoteList(this, NotesDb.GetAllNotes());
    	}
   	    // Close the activity
   	    finish();
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		   case ProjectConst.ShareBy_Dlg:
			    ArrayList<Uri> MediaUri = new ArrayList<Uri>();
			    MediaUri.add(Uri.fromFile(new File(AddOneNote.NoteFilePath)));
			    return HelperFunctions.BuildMediaShareByDlg(this, R.string.shareby_title, AddOneNote.NoteTitle, AddOneNote.NoteTitle, MediaUri);
		   case ProjectConst.Input_Title_Dlg:
			    return BuildEditNotePromptPwdDlg(this, R.string.input_title_tip);
		}
		return null;
	}
    
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
      	if ( requestCode == ProjectConst.ACTIVITY_SET_TAGCLR && resultCode == RESULT_OK ) {
    	    Bundle SelIdxData = data.getExtras();
    	    AddOneNote.DrawableResIdx = SelIdxData.getInt(OneNote.KEY_DRAWABLE_ID);
    	    ContentView.SetBgColor(NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx]);
    	    //SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
    	    //AddPanel.setBackgroundDrawable(new BitmapDrawable(HelperFunctions.CreateTitleBarBg(NotePadPlus.ScreenWidthDip, NotePadPlus.ScreenHeightDip, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx], NotePadPlus.TagClr[AddOneNote.DrawableResIdx])));
        } else if( requestCode == ProjectConst.ACTIVITY_SET_NOTIFYTIME ) {
	    	if( resultCode == RESULT_OK ) {
	    		Bundle Result = data.getExtras();
	    		
	    		if( Result.isEmpty() )
	    		{
	    			AddOneNote.Use_NotifyTime = ProjectConst.No;
	    			NotifyTimeLabel.setText(ProjectConst.EmptyStr);
	    		} else {
	    			AddOneNote.Use_NotifyTime = ProjectConst.Yes;
	    			AddOneNote.NotifyMethod = Result.getInt(OneNote.KEY_NOTIFYMETHOD);
	    			AddOneNote.NotifyDura = Result.getInt(OneNote.KEY_NOTIFYDURA);
	    			AddOneNote.RingMusic = Result.getString(OneNote.KEY_RINGMUSIC);
	    			AddOneNote.NotifyTime = HelperFunctions.String2Calenar(Result.getString(OneNote.KEY_NOTIFYTIME));
	    		    NotifyTimeLabel.setText(HelperFunctions.FormatCalendar2ReadableStr(AddOneNote.NotifyTime));
	    		}
	    	}
	    } else if( requestCode == ProjectConst.ACTIVITY_SET_PWD && resultCode == RESULT_OK )
	    	AddOneNote.Password = data.getStringExtra(OneNote.KEY_PWD);   
         
        super.onActivityResult(requestCode, resultCode, data);  
    }  
	
    private void StartNotifyActivity()
	{
    	// start notify time activity
    	Intent intent = new Intent();
		intent.setClass(this, NotifyDateActivity.class);
		// Set time
		Bundle Parameters = new Bundle();
		Parameters.putString(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(AddOneNote.NotifyTime));
		Parameters.putInt(OneNote.KEY_NOTIFYDURA, AddOneNote.NotifyDura);
		Parameters.putString(OneNote.KEY_RINGMUSIC, AddOneNote.RingMusic);
		Parameters.putInt(OneNote.KEY_NOTIFYMETHOD, AddOneNote.NotifyMethod);
        // Pass it to next activity 
		intent.putExtras(Parameters);
		// Go to next activity(set note's notify time activity)
		startActivityForResult(intent, ProjectConst.ACTIVITY_SET_NOTIFYTIME);		
	}
    
    private Dialog BuildEditNotePromptPwdDlg(Context AppContext, int Title){
        LayoutInflater factory = LayoutInflater.from(this);
        final View PromptView = factory.inflate(R.layout.scraw_title_layout, null);
        return new AlertDialog.Builder(this)
               .setIcon(R.drawable.alert_dialog_icon)
               .setTitle(Title)
               .setView(PromptView)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
            	      EditText Title = (EditText)PromptView.findViewById(R.id.title_edit);
            	      AddOneNote.NoteTitle = Title.getText().toString();  
              }
              })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
              }
              })
              .create();
	}
}
