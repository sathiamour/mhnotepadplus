package com.android.hinotes;

import java.io.FileNotFoundException;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

public class ShowImgListActivity extends Activity {
  
	/** Padding(in pixel unit) */
	private static final int SpacePadding = 5;
	// Media uri
	private Vector<Uri> MediaUri;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showimglist);
		
		MediaUri = new Vector<Uri>();
		int Count = getIntent().getIntExtra(ProjectConst.Key_Count, ProjectConst.Zero);
		for( int i = 0; i < Count; ++i )
			 MediaUri.add(Uri.parse(getIntent().getStringExtra(ProjectConst.Key_Uri+Integer.toString(i))));
			
		
        // Grid view
		GridView ImgGrid = (GridView) findViewById(R.id.imggrid); 
        
		// Screen width & height
        DisplayMetrics ScreenMetrics = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);   
        int ScreenHeight = ScreenMetrics.heightPixels;   
        int ScreenWidth = ScreenMetrics.widthPixels;
        
        int Length = Math.min(ScreenHeight, ScreenWidth);
        int TagWidth = (Length-SpacePadding*3)/4;
        //int HeightPadding = (ScreenHeight-TagWidth*4-SpacePadding*3)/4;
        
	    //ImgGrid.setPadding(WidthPadding, HeightPadding, WidthPadding, HeightPadding);
	    ImgGrid.setColumnWidth(TagWidth);
	    ImgGrid.setHorizontalSpacing(SpacePadding);
	    ImgGrid.setVerticalSpacing(SpacePadding);
	    
		// Add Listeners
		ImgGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    	       Intent ShowImgIntent = new Intent(ShowImgListActivity.this, ShowImgActivity.class);
	    	       ShowImgIntent.putExtra(ProjectConst.Key_Uri, MediaUri.get(position));
	    	       startActivity(ShowImgIntent);
			}
		});
		
		ImageAdapter ImgAdapter = new ImageAdapter(this, MediaUri, TagWidth);
		ImgGrid.setAdapter(ImgAdapter);
	}
	
	public class ImageAdapter extends BaseAdapter { 
		  private Vector<Uri> MediaUri;
		  private int CellWidth;
	      public ImageAdapter(Context c, Vector<Uri> Uris, int Width) 
	      { 
	             mContext = c;   
	             MediaUri = Uris;
	             CellWidth = Width;
	      }

	      public int getCount()
	      { 
	             return MediaUri.size(); 
	      } 

	      public Object getItem(int position)
	      {      
	             return position;
	      }  
	     
	      public long getItemId(int position)
	      {        
	             return position; 
	      }      

	      public View getView(int position, View convertView, ViewGroup parent) 
	      {    
	             ImageView imageView;
	             if( convertView == null ) 
	             {
	                 imageView = new ImageView(mContext);
	                 imageView.setLayoutParams(new GridView.LayoutParams(CellWidth, CellWidth));
	                 imageView.setAdjustViewBounds(false);
	                 imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	                 //imageView.setPadding(8, 8, 8, 8);
	             } else {
	                 imageView = (ImageView) convertView;  
	             }          
	             
	             Bitmap Picture = null;
	             
	    		 try {
					Picture = HelperFunctions.DecodeBitmapFromUri(mContext, MediaUri.get(position), CellWidth, CellWidth);
		            imageView.setImageBitmap(Picture);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    				

	             
	             return imageView;
	      }      

	      private Context mContext;

	} 
	
	
}
