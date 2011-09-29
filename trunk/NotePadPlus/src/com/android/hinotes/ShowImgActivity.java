package com.android.hinotes;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

public class ShowImgActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showimg);
		
		Uri PicUri = getIntent().getParcelableExtra(SelImgActivity.Key_PicUri);
		
		 // Load picture
        ContentResolver cr = getContentResolver();  
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        Bitmap SelPic = null;
        // Get original picture's width & height
		 try {
			BitmapFactory.decodeStream(cr.openInputStream(PicUri), null, opts);
		 } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 } 
		 int Width = NotePadPlus.ScreenWidth-20;
		 int Height =  Math.min((int) (Width*1.0/opts.outWidth* opts.outHeight), NotePadPlus.ScreenHeight-60);
	     opts.inSampleSize = HelperFunctions.computeSampleSize(opts, -1, Width*Height);		
	     opts.inJustDecodeBounds = false;
	        
	      
	     try {
	    	 SelPic = BitmapFactory.decodeStream(cr.openInputStream(PicUri), null, opts);
		 } catch (FileNotFoundException e) {
			   // TODO Auto-generated catch block
			   e.printStackTrace();
		 } 
	    
        ImageView ShowPic = (ImageView)findViewById(R.id.pic);
        ShowPic.setImageDrawable(new BitmapDrawable(SelPic));   
		
	}
}
