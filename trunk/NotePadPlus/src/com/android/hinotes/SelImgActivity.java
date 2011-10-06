package com.android.hinotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;   
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;   
import android.graphics.BitmapFactory;   
import android.graphics.Matrix;   
import android.graphics.drawable.BitmapDrawable;   
import android.net.Uri;
import android.os.Bundle;   
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;   
import android.widget.RelativeLayout;
import android.widget.Toast;
  
  
public class SelImgActivity extends Activity {   

    public static final String Key_PicUri = "uri";  
    public static final String Key_PicData = "data";

  
	private RelativeLayout EditPanel;
	private Bitmap SelPic;
	private ImageView ShowPic;
	private Uri PicUri;
	
	// Enlarge & Lessen
	private static final int EnlargeNum = 4;
	private static final int LessenNum = -4;
	private static final float ResizeFactor = 1.4f;
	private static float ResizeScale = 1.0f;
	private int ResizeNum = 0;
	
	// Rotate
	private int TurnAngle = 0;
	private static final int BaseAngle =90;
	private static final int CircleAngle = 360;
	
	// Bitmap saving path 
	String OriginalFileName;
	String OriginalFillPath;
	
	// Has been edited flag
	boolean HasEdited;
	
    @Override  
    public void onCreate(Bundle savedInstanceState) {   
         super.onCreate(savedInstanceState);   
         setContentView(R.layout.selimg);
         
         // Get picture's uri
         PicUri = getIntent().getParcelableExtra(Key_PicUri);
         String FullPath = GetPathFromUri(PicUri);
         int LastSlashIdx = FullPath.lastIndexOf('/');
         OriginalFillPath = FullPath.substring(0, LastSlashIdx+1);
         String FileName = FullPath.substring(LastSlashIdx+1);
         OriginalFileName = FileName.substring(0, FileName.lastIndexOf('.'));
         
         // Has been edited flag
         HasEdited = false;
         
         // Create float edit panel 
         EditPanel = (RelativeLayout)findViewById(R.id.edit_panel);
         
         // Load picture
         ContentResolver cr = getContentResolver();  
         BitmapFactory.Options opts = new BitmapFactory.Options();
         opts.inJustDecodeBounds = true;
         SelPic = null;
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
	    
         ShowPic = (ImageView)findViewById(R.id.selpic);
         ShowPic.setImageDrawable(new BitmapDrawable(SelPic));   
  
         // Edit panel
         // Edit
         ImageButton Crop = (ImageButton)findViewById(R.id.crop);
         Crop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        Intent CropIntent = new Intent("com.android.camera.action.CROP");    
		        CropIntent.setDataAndType(PicUri, "image/*");    
		        CropIntent.putExtra("crop", "true");      
		        CropIntent.putExtra("aspectX", 1);    
		        CropIntent.putExtra("aspectY", 1);     
		        CropIntent.putExtra("outputX", 64);    
		        CropIntent.putExtra("outputY", 64);    
		        CropIntent.putExtra("return-data", true);    
		        startActivityForResult(CropIntent, ProjectConst.ACTIVITY_CROP);    
			}
        	 
         });
         
         // Enlarge
         ImageButton Enlarge = (ImageButton)findViewById(R.id.enlarge);
         Enlarge.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				++ResizeNum;
				if( ResizeNum <= EnlargeNum )
				{
					ResizeScale *= ResizeFactor;
				    Matrix TransMatrix=new Matrix();   
				    TransMatrix.postScale(ResizeScale, ResizeScale);        
		            Bitmap RotateBitmap=Bitmap.createBitmap(SelPic,0,0,SelPic.getWidth(),SelPic.getHeight(),TransMatrix,true);
		            //SelPic = RotateBitmap;
		            ImageView ShowPic = (ImageView)findViewById(R.id.selpic);
		            ShowPic.setImageDrawable(new BitmapDrawable(RotateBitmap));   
				} else {
					--ResizeNum;
					Toast.makeText(SelImgActivity.this, R.string.enlarge_img_err, Toast.LENGTH_SHORT).show();
				}
			}
        	 
         });
         
         // Lessen
         ImageButton Lessen = (ImageButton)findViewById(R.id.lessen);
         Lessen.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				--ResizeNum;
				if( ResizeNum >= LessenNum )
				{
				    ResizeScale /= ResizeFactor;
				    Matrix TransMatrix=new Matrix();   
				    TransMatrix.postScale(ResizeScale, ResizeScale);        
		            Bitmap RotateBitmap=Bitmap.createBitmap(SelPic,0,0,SelPic.getWidth(),SelPic.getHeight(),TransMatrix,true);
		            
		            ImageView ShowPic = (ImageView)findViewById(R.id.selpic);
		            ShowPic.setImageDrawable(new BitmapDrawable(RotateBitmap));  
				} else {
					++ResizeNum;
					Toast.makeText(SelImgActivity.this, R.string.lessen_img_err, Toast.LENGTH_SHORT).show();
				}
			}
        	 
         });
         
         // Rotate left
         ImageButton RotateLeft = (ImageButton)findViewById(R.id.rotateleft);
         RotateLeft.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TurnAngle -= BaseAngle;
				if( TurnAngle == -1*CircleAngle ) TurnAngle += CircleAngle;
				
				Matrix TransMatrix=new Matrix();   
				TransMatrix.postScale(1.0f,1.0f);   
				TransMatrix.postRotate(TurnAngle);      
		        Bitmap RotateBitmap=Bitmap.createBitmap(SelPic,0,0,SelPic.getWidth(),SelPic.getHeight(),TransMatrix,true);
		        //SelPic = RotateBitmap;
		        ImageView ShowPic = (ImageView)findViewById(R.id.selpic);
		        ShowPic.setImageDrawable(new BitmapDrawable(RotateBitmap));   
			}
        	 
         });
         
         // Rotate right
         ImageButton RotateRight = (ImageButton)findViewById(R.id.rotateright);
         RotateRight.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TurnAngle += BaseAngle;
				if( TurnAngle == CircleAngle ) TurnAngle += CircleAngle;
				
			    Matrix TransMatrix=new Matrix();   
				TransMatrix.postScale(1.0f,1.0f);   
				TransMatrix.postRotate(TurnAngle);      
		        Bitmap RotateBitmap=Bitmap.createBitmap(SelPic,0,0,SelPic.getWidth(),SelPic.getHeight(),TransMatrix,true);
		        //SelPic = RotateBitmap;
		        ImageView ShowPic = (ImageView)findViewById(R.id.selpic);
		        ShowPic.setImageDrawable(new BitmapDrawable(RotateBitmap));   
			}
        	 
         });
         
         // Set button click listener
         Button ReSelected = (Button)findViewById(R.id.resel_btn);
         ReSelected.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();				
			}
        	 
         });
         
         Button Edit = (Button)findViewById(R.id.edit_btn);
         Edit.setOnClickListener(new OnClickListener(){

  			@Override
  			public void onClick(View v) {
  		         if( EditPanel.getVisibility() == View.INVISIBLE ) 
  		        	 EditPanel.setVisibility(View.VISIBLE);
  		         else
  		        	 EditPanel.setVisibility(View.INVISIBLE);
  			}
          	 
           });

         Button Sel = (Button)findViewById(R.id.sel_btn);
         Sel.setOnClickListener(new OnClickListener(){

 			@Override
 			public void onClick(View v) {
                String BmpFilePath = SavePicture();
                Uri Path = PicUri;
                if( !BmpFilePath.equals(ProjectConst.EmptyStr) )
                    Path = Uri.fromFile(new File(BmpFilePath));
                Intent Redirect = new Intent();
                Redirect.putExtra(Key_PicUri, Path);
                Redirect.putExtra(Key_PicData, TurnAngle);
 				setResult(RESULT_OK, Redirect);
 				finish();				
 			}
         	 
          });
    }   
    
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if( requestCode == ProjectConst.ACTIVITY_CROP && resultCode == RESULT_OK ) {  
        	Bundle extras = data.getExtras();   
        	if(extras != null ) {   
        		SelPic = extras.getParcelable(Key_PicData);   
        	    ShowPic.setImageDrawable(new BitmapDrawable(SelPic));   
        	    HasEdited = true;
        	}  
        }
	}
	
	private String GetPathFromUri(Uri PicUri)
	{
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor ActualImageCursor = managedQuery(PicUri, proj,null, null, null);
		if( ActualImageCursor != null )
		{
		    int actual_image_column_index = ActualImageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
		    ActualImageCursor.moveToFirst();
		    String ImgPath = ActualImageCursor.getString(actual_image_column_index);
		    return ImgPath;
		} else {
			String ImgPath = PicUri.toString();
			if( ImgPath.indexOf("file:///") != -1 )
				return ImgPath.substring(7);
			else
				return ProjectConst.EmptyStr;
		}
	}
	
	private String SavePicture()
	{
		String BmpFilePath = ProjectConst.EmptyStr;
		if( HasEdited )
		{
		    FileOutputStream  BmpFileOutPutStream = null; 
		    BmpFilePath =  OriginalFillPath + "bak_"+ OriginalFileName + ".jpg";
		    try { 
			      BmpFileOutPutStream = new FileOutputStream(BmpFilePath);//写入的文件路径 
		    } catch (FileNotFoundException e) { 
			      // TODO Auto-generated catch block 
			      e.printStackTrace(); 
			} 
			SelPic.compress(Bitmap.CompressFormat.JPEG, 100, BmpFileOutPutStream);
			try { 
			      BmpFileOutPutStream.flush(); 
			      BmpFileOutPutStream.close(); 
			} catch (IOException e) { 
			      // TODO Auto-generated catch block 
			      e.printStackTrace(); 
			} 
		}
		
		return BmpFilePath;
	}
 
}  
