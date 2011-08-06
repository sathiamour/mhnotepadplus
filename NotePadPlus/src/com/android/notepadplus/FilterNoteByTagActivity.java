package com.android.notepadplus;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class FilterNoteByTagActivity extends Activity {
	/** Pading(in pixel unit) */
	public static final int WidthPadding = 15; 
	public static final int SpacePadding = 10;
	
	/** GridView */
	private GridView TagClrs = null;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settagclr);
        
        DisplayMetrics ScreenMetrics = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);   
        int ScreenHeight = ScreenMetrics.heightPixels;   
        int ScreenWidth = ScreenMetrics.widthPixels;
        
        int Length = Math.min(ScreenHeight, ScreenWidth);
        int TagWidth = (Length-WidthPadding*2-SpacePadding*2)/3;
        int HeightPadding = (ScreenHeight-TagWidth*3-SpacePadding*2)/3;
        
	    TagClrs = (GridView)findViewById(R.id.SelTagClrsView);
	    TagClrs.setPadding(WidthPadding, HeightPadding, WidthPadding, HeightPadding);
	    TagClrs.setColumnWidth(TagWidth);
	    TagClrs.setHorizontalSpacing(SpacePadding);
	    TagClrs.setVerticalSpacing(SpacePadding);
	    TagClrs.setOnItemClickListener(new OnItemClickListener() {  
                @Override  
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
                	   // Set return data
   		    	       Bundle SelIdxData = new Bundle();
   		    	       SelIdxData.putInt(OneNote.KEY_DRAWABLE_ID, position);
   		    	       Intent ReturnBackIntent = new Intent();
   		    	       ReturnBackIntent.putExtras(SelIdxData);
   		    	       FilterNoteByTagActivity.this.setResult(RESULT_OK, ReturnBackIntent);
   		    	       // Return to launching activity
   			    	   finish();
                }  
        });  

        ArrayList<HashMap<String,Object>> Tags = new ArrayList<HashMap<String, Object>>();
	    int[] BgColor = new int[NotePadPlus.ClrNum];
	    for( int i = 0; i < NotePadPlus.ClrNum; ++i )
	    {
	         HashMap<String, Object> OneTag = new HashMap<String, Object>();
	         // Dummy item
	         Tags.add(OneTag);
	         // Set up colors
	         BgColor[i] = NotePadPlus.ItemBgClr[i];
	    }
	    
	    TagClrItemAdapter TagAdapter = new TagClrItemAdapter(this, Tags, 
                                                R.layout.clrtagitem,
                                                new String[]{},
                                                new int[]{},BgColor, TagWidth);
	    

	    TagClrs.setAdapter(TagAdapter);
    }

}
