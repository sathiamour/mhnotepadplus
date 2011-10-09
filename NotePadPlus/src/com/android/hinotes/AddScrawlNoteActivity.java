/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.hinotes;

import java.util.UUID;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddScrawlNoteActivity extends GraphicsActivity
        implements ColorPickerDialog.OnColorChangedListener {    

	// Content view
	private FingerPaintView ContentView;
	/** Database */
	private NoteDbAdapter NotesDb;
	/** One note */
	private OneNote AddOneNote;
	// Paint flag
	boolean IsPainted;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addscrawlnote);
        
		// Initialize & open database
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();
		// Initialize the note
		AddOneNote = new OneNote(OneNote.ScrawlNote);
		// Is painted flag
		IsPainted = false;
		
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);
        
        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        
        ContentView = (FingerPaintView) findViewById(R.id.scraw_content);
        //LinearLayout AddPanel = (LinearLayout)findViewById(R.id.addnote_panel);
		
        ContentView.InitFingerPaintView(mPaint,HelperFunctions.CreateTitleBarBg(NotePadPlus.ScreenWidthDip, NotePadPlus.ScreenHeightDip, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx], NotePadPlus.TagClr[AddOneNote.DrawableResIdx]));
        ContentView.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(AddScrawlNoteActivity.this, "sfsf", Toast.LENGTH_SHORT);
				return false;
			}
        	
        });
		// Randomly select color
    	//AddPanel.setBackgroundDrawable(new BitmapDrawable()); 
    }
    
 
    
    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter  mBlur;
    
    public void colorChanged(int color) {
        mPaint.setColor(color);
    }


    
    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
		menu.add(Menu.NONE, ProjectConst.ITEM0, 1, "放弃").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(Menu.NONE, ProjectConst.ITEM1, 2, "提醒").setIcon(R.drawable.ic_menu_reminder);
		menu.add(Menu.NONE, ProjectConst.ITEM2, 3, "颜色").setIcon(android.R.drawable.ic_menu_view);
		menu.add(Menu.NONE, ProjectConst.ITEM3, 4, "标题").setIcon(R.drawable.ic_menu_compose);
		menu.add(Menu.NONE, ProjectConst.ITEM4, 5, "锁定").setIcon(R.drawable.ic_menu_lock);
		menu.add(Menu.NONE, ProjectConst.ITEM5, 6, "分享").setIcon(android.R.drawable.ic_menu_share);
		
        //menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        //menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        //menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        //menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        //menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

        /****   Is this the mechanism to extend with filter effects?
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
                              Menu.ALTERNATIVE, 0,
                              new ComponentName(this, NotesList.class),
                              null, intent, 0, null);
        *****/
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                return true;
            case SRCATOP_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override 
	public void onBackPressed(){
    	if( IsPainted )
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
}
