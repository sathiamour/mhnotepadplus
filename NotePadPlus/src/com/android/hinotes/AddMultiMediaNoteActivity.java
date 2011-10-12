package com.android.hinotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.UUID;
import java.util.Vector;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddMultiMediaNoteActivity extends Activity {
	
	private EditText NoteTitle;
	private EditText NoteBody;
	private TextView NotifyTimeLabel;
	private Button   SelectTagClrBtn;
	private LinearLayout AddPanel;
	
	private SpannableStringBuilder Content;
	private boolean NotInsert;
	
	// Media uri
	private Vector<String> MediaUri;
	/** Database */
	private NoteDbAdapter NotesDb;
	/** One note */
	private OneNote AddOneNote;
	
	private String FolderPath;
	private String CameraFileName;
	private boolean IsCameraCapture;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addmultimedianote);
		
		// Initialize & open database
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();
		// Camera's folder path
		FolderPath = HelperFunctions.MakeCameraFolder();
		// Initialize the note
		AddOneNote = new OneNote(OneNote.MultiMediaNote);
		MediaUri = new Vector<String>();
		// Views
		NoteTitle = (EditText)findViewById(R.id.title_content);
	    Button GalleryBtn = (Button)findViewById(R.id.sys_mediagallery_btn);
	    Button CameraBtn = (Button)findViewById(R.id.camera_btn);
	    Button FaceBtn = (Button)findViewById(R.id.face_btn);
	    Button VoiceBtn = (Button)findViewById(R.id.voice_btn);
	    Button VideoBtn = (Button)findViewById(R.id.video_btn);
	    
	    NoteBody = (EditText)findViewById(R.id.add_body_content);
		SelectTagClrBtn = (Button)findViewById(R.id.selnoteclr);
		NotifyTimeLabel = (TextView)findViewById(R.id.notifytime_text);
		AddPanel = (LinearLayout)findViewById(R.id.addnote_panel);
		
		// Randomly select color
    	SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
    	AddPanel.setBackgroundDrawable(new BitmapDrawable(HelperFunctions.CreateTitleBarBg(NotePadPlus.ScreenWidthDip, NotePadPlus.ScreenHeightDip, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx], NotePadPlus.TagClr[AddOneNote.DrawableResIdx]))); 

    	// Select tag color
    	SelectTagClrBtn.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	Intent intent = new Intent();
				intent.setClass(AddMultiMediaNoteActivity.this, SetItemClrActivity.class);
				intent.putExtra(SetItemClrActivity.Key_ClrType, SetItemClrActivity.Val_ItemType_Tag);
				startActivityForResult(intent, ProjectConst.ACTIVITY_SET_TAGCLR);				
    		}
    	});
    	
    	// Set button click listener
    	// System gallery picture picker
	    GalleryBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				   StartPickGallery();				
			}
	    	
	    });
	    // Camera picture picker
	    CameraBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				   StartPickCamera();	
			}
	    	
	    });
	    // add emotion face
	    FaceBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddMultiMediaNoteActivity.this, SelFaceActivity.class);
				startActivityForResult(intent, ProjectConst.ACTIVITY_SEL_FACE);
				
			}
	    	
	    });
	    // add video picker
	    VideoBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				   StartPickVideo();
			}
	    	
	    });
	    // add voice picker
	    VoiceBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				   StartPickVoice();
			}
	    	
	    });
	    //
	    // Set edit text view listener	    
	    NoteBody.addTextChangedListener(new TextWatcher(){  	  
	        @Override  
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}  
	  
	        @Override  
	        public void onTextChanged(CharSequence s, int start, int before, int count) { 
	        	   Log.d("log","in onTextChanged");
	        	   if( !NotInsert )
	        	       Content.replace(start, start+before, s.subSequence(start, start+count).toString());
	        	   else
	        		   NotInsert = false;
	        }

			@Override
			public void afterTextChanged(Editable arg0) {}     
	    });
	    

        
	    NoteTitle.addTextChangedListener(new TextWatcher(){  	  
	        @Override  
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}  
	  
	        @Override  
	        public void onTextChanged(CharSequence s, int start, int before, int count) {  
	        	   AddOneNote.NoteTitle = s.toString();
	        }

			@Override
			public void afterTextChanged(Editable arg0) {}     
	    });
	    
        Content = new SpannableStringBuilder(ProjectConst.EmptyStr);
         
	}
	
	/** When the activity is destroyed, close database*/
    @Override
    protected void onDestroy(){
    	if( NotesDb != null )
    		NotesDb.close();    
    	super.onDestroy();
    }

	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if (requestCode == ProjectConst.ACTIVITY_GET_PICTURE && resultCode == RESULT_OK) {  
        	IsCameraCapture = false;
            Uri uri = data.getData();  
            Intent SelImgIntent = new Intent(this, SelImgActivity.class);
            SelImgIntent.putExtra(SelImgActivity.Key_PicUri, uri);
            startActivityForResult(SelImgIntent, ProjectConst.ACTIVITY_EDIT_PIC);
            			
        } else if( requestCode == ProjectConst.ACTIVITY_EDIT_PIC ) {
        	if( resultCode == RESULT_OK ){
        		Uri PicUri = data.getParcelableExtra(SelImgActivity.Key_PicUri);  
        		int TurnAngle = data.getIntExtra(SelImgActivity.Key_PicData, ProjectConst.Zero);
        		Drawable FinalBitmap = null;
    			try {
    				Bitmap Picture = HelperFunctions.DecodeBitmapFromUri(this, PicUri, (int)(ProjectConst.PictureWidth*NotePadPlus.ScreenDensity), (int)(ProjectConst.PictureHeight*NotePadPlus.ScreenDensity));
    				if( ProjectConst.Zero != 0 )
    				{
    				    Matrix TransMatrix=new Matrix();   
    				    TransMatrix.postScale(1.0f,1.0f);   
    				    TransMatrix.postRotate(TurnAngle);      
    				    FinalBitmap = new BitmapDrawable(Bitmap.createBitmap(Picture,0,0,Picture.getWidth(),Picture.getHeight(),TransMatrix,true));
    				} else
    					FinalBitmap = new BitmapDrawable(Picture);
    				
    			} catch (FileNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				Log.d(ProjectConst.TAG, PicUri.toString()+" doesn't find"); 
    			}
      
    			
    			if( FinalBitmap != null )
    				InsertImg(ProjectConst.ImgTagFmt, PicUri.toString(), FinalBitmap);
    			else
    				Toast.makeText(this, R.string.multimedianote_loadbmp_err, Toast.LENGTH_SHORT).show();

        	} else {
        		if( IsCameraCapture )
        			StartPickCamera();
        		else
				    StartPickGallery();				
        	}
        } else if( requestCode == ProjectConst.ACTIVITY_CAMERA_CAPTURE && resultCode == RESULT_OK ) {
        	    IsCameraCapture = true;
                Intent SelImgIntent = new Intent(this, SelImgActivity.class);
                SelImgIntent.putExtra(SelImgActivity.Key_PicUri, Uri.fromFile(new File(FolderPath, CameraFileName)));
                startActivityForResult(SelImgIntent, ProjectConst.ACTIVITY_EDIT_PIC);
        } else if( requestCode == ProjectConst.ACTIVITY_GET_VIDEO && resultCode == RESULT_OK ) {   
                Uri UriVideo = data.getData();
        		Drawable FinalBitmap = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.video_icon));
    			
        		InsertImg(ProjectConst.VideoTagFmt, UriVideo.toString(), FinalBitmap);
        } else if( requestCode == ProjectConst.ACTIVITY_GET_AUDIO && resultCode == RESULT_OK ) {     
                Uri VoiceUri = data.getData();
                Drawable FinalBitmap = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.record_icon));
    			
        		InsertImg(ProjectConst.AudioTagFmt, VoiceUri.toString(), FinalBitmap);       
        } else if( requestCode == ProjectConst.ACTIVITY_SEL_FACE && resultCode == RESULT_OK ) {
     	    Bundle SelIdxData = data.getExtras();
    	    int FaceId = SelIdxData.getInt(SelFaceActivity.KEY_FACE_ID);
    	    Drawable FinalBitmap = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), SelFaceActivity.Faces[FaceId]));
			
    		InsertImg(ProjectConst.FaceTagFmt, Integer.toString(FaceId), FinalBitmap);
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
	    } else if ( requestCode == ProjectConst.ACTIVITY_SET_TAGCLR && resultCode == RESULT_OK) {
	    	    Bundle SelIdxData = data.getExtras();
	    	    AddOneNote.DrawableResIdx = SelIdxData.getInt(OneNote.KEY_DRAWABLE_ID);
	    	    SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
	    	    AddPanel.setBackgroundDrawable(new BitmapDrawable(HelperFunctions.CreateTitleBarBg(NotePadPlus.ScreenWidthDip, NotePadPlus.ScreenHeightDip, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx], NotePadPlus.TagClr[AddOneNote.DrawableResIdx])));
	    } else if( requestCode == ProjectConst.ACTIVITY_SET_PWD && resultCode == RESULT_OK )
	    	    AddOneNote.Password = data.getStringExtra(OneNote.KEY_PWD);   
         
        super.onActivityResult(requestCode, resultCode, data);  
    }  
	
	@Override 
	public void onBackPressed(){
		// Do title check
   	    if( AddOneNote.NoteTitle.length() == 0 )
   	    {
   		    // Title is empty, we prompt it to user and then return
   		    showDialog(ProjectConst.Check_NoteTitle_Dlg);
   		    return;
   	    }
   	 
   	    // Save it to file
   	    AddOneNote.NoteBody = Content.toString();
   	    // Get a random file name
   	    AddOneNote.NoteFilePath = UUID.randomUUID().toString()+ ProjectConst.NoteFileExt;
   	    HelperFunctions.WriteTextFile(this, AddOneNote.NoteBody, AddOneNote.NoteFilePath);
   	    // Add database record
   	    NotesDb.CreateOneNote(AddOneNote);   
   	    // If user choose to use notify time, add the alarm
   	    if( AddOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
   	        Alarms.AddOneAlarm(this);
   	    // Refresh widget note list
   	    HelperFunctions.RefreshWidgetNoteList(this, NotesDb.GetAllNotes());
   	    // Return to main activity
        // Set return code(unable to return row id)
	    Intent ReturnBackData = new Intent();
	    ReturnBackData.putExtra(OneNote.KEY_TITLE, AddOneNote.NoteTitle);
	    ReturnBackData.putExtra(OneNote.KEY_PWD, AddOneNote.Password);
	    ReturnBackData.putExtra(OneNote.KEY_ROWID, NotesDb.GetOneNoteRowId(AddOneNote.NoteFilePath));
	    ReturnBackData.putExtra(OneNote.KEY_DRAWABLE_ID, AddOneNote.DrawableResIdx);
	    ReturnBackData.putExtra(OneNote.KEY_NOTETYPE, AddOneNote.NoteType);
	    setResult(RESULT_OK, ReturnBackData);
   	    finish(); 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add(Menu.NONE, ProjectConst.ITEM0, 1, "放弃").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(Menu.NONE, ProjectConst.ITEM1, 2, "提醒").setIcon(R.drawable.ic_menu_reminder);
		menu.add(Menu.NONE, ProjectConst.ITEM2, 3, "锁定").setIcon(R.drawable.ic_menu_lock);
		menu.add(Menu.NONE, ProjectConst.ITEM3, 4, "分享").setIcon(android.R.drawable.ic_menu_share);
		menu.add(Menu.NONE, ProjectConst.ITEM4, 5, "媒体").setIcon(android.R.drawable.ic_menu_view);
		//menu.add(Menu.NONE, ITEM1, 2, "图片").setIcon(android.R.drawable.ic_menu_gallery);
		//menu.add(Menu.NONE, ITEM2, 3, "录音").setIcon(android.R.drawable.ic_menu_mylocation);
        return true;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {  
           switch(item.getItemId()) 
           {  
              case ProjectConst.ITEM0:
            	   setResult(RESULT_CANCELED);
	               finish();
	               break;
              case ProjectConst.ITEM1:
            	   StartNotifyActivity();
            	   break;
              case ProjectConst.ITEM2:
            	   Intent PwdDlgIntent = new Intent(this, PwdDlgActivity.class);
 			       startActivityForResult(PwdDlgIntent, ProjectConst.ACTIVITY_SET_PWD);
            	   break;
              case ProjectConst.ITEM3:
           	       showDialog(ProjectConst.ShareBy_Dlg);
            	   break;
              case ProjectConst.ITEM4:
            	   showDialog(ProjectConst.MediaView_Dlg);
            	   break;
            
           }
           return false;
	}
	
    @Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		   case ProjectConst.Check_NoteTitle_Dlg:
			    return HelperFunctions.BuildAltertDialog(this, R.string.prompt_title, R.string.notetitle_empty_tip);
		   case ProjectConst.Set_NotifyDate_Dlg:
			    return HelperFunctions.BuildAltertDialog(this, R.string.prompt_title, R.string.notifydate_expire_tip);
		   case ProjectConst.ShareBy_Dlg:
			    String Body = ParseMultiMediaNoteShareBy(Content.toString(), ProjectConst.Prefix, ProjectConst.Suffix, ProjectConst.FaceTag);
			    return HelperFunctions.BuildMediaShareByDlg(this, R.string.shareby_title, AddOneNote.NoteTitle, Body, MediaUri);
		   case ProjectConst.MediaView_Dlg:
			    return BuildMediaViewDlg(this, R.string.meidaview_title, R.array.mediatype);

		}
		return null;
	}
    
 
	private Dialog BuildMediaViewDlg(Context AppContext, int Title, int Items)
	{
        Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(R.drawable.ic_dialog_menu_generic);  
        builder.setTitle(Title);  
        BaseAdapter adapter = new CommonListItemAdapter(new int[]{R.drawable.common_choose_picture, R.drawable.common_choose_record, R.drawable.common_take_vidicon},Items, AppContext);  
        DialogInterface.OnClickListener listener =   
            new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialogInterface, int which) {  
               	            switch(which)
               	            {
               	               case 0:
               	            	    StartShowMediaListActivity(ProjectConst.ImgPrefix, ShowImgListActivity.class);
               	            	    break; 
               	               case 1:
               	            	    StartShowMediaListActivity(ProjectConst.AudioPrefix, ShowAudioListActivity.class);
               	            	    break;
               	               case 2:
              	            	    StartShowMediaListActivity(ProjectConst.VideoPrefix, ShowVideoListActivity.class);
               	            	    break;
               	            }
                }  
            };  
        builder.setAdapter(adapter, listener);  
        return builder.create();  
	}
	
    private void InsertImg(String StdTag, String UriStr, Drawable FinalBitmap)
    {
		NotInsert = true;
		FinalBitmap.setBounds(0, 0, FinalBitmap.getIntrinsicWidth(), FinalBitmap.getIntrinsicHeight());
		int CursorPos = NoteBody.getSelectionStart();
		if( CursorPos < 0 )
			CursorPos = 0;
		String Tag = String.format(StdTag, UriStr);
        Content.insert(CursorPos, Tag);
        ImageSpan ImgSpan = new ImageSpan(FinalBitmap, ImageSpan.ALIGN_BASELINE);
        Content.setSpan(ImgSpan, CursorPos, CursorPos+Tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        NoteBody.setText(Content);
        NoteBody.setSelection(CursorPos+Tag.length());

    }
    
    private void StartShowMediaListActivity(String Prefix, Class<?> ShowActivity)
    {
   	    ParseSpecificMedia(Content.toString(), Prefix, ProjectConst.Suffix);
   	    Intent intent = new Intent(this, ShowActivity);
   	    int Count = MediaUri.size();
   	    intent.putExtra(ProjectConst.Key_Count, Count);
   	    for( int i = 0; i < Count; ++i )
   	    	 intent.putExtra(ProjectConst.Key_Uri+Integer.toString(i), MediaUri.get(i));
   	    startActivity(intent);
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
    
    private void StartPickGallery()
    {
		Intent intent = new Intent();  
		intent.setType("image/*");  
        intent.setAction(Intent.ACTION_GET_CONTENT);     
        startActivityForResult(intent, ProjectConst.ACTIVITY_GET_PICTURE);  
    }
    
    private void StartPickVideo() 
    {    
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);    
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);    
        startActivityForResult(intent, ProjectConst.ACTIVITY_GET_VIDEO);    
    } 
    
    private void StartPickVoice() 
    {    
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);    
        intent.setType("audio/amr");    
        startActivityForResult(intent, ProjectConst.ACTIVITY_GET_AUDIO);    
    }    

    
    private void StartPickCamera()
    {
		CameraFileName = DateFormat.format("yyyy-MM-dd_hh-mm-ss", Calendar.getInstance())+".jpg";;
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);     
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(FolderPath, CameraFileName)));     
        startActivityForResult(intent, ProjectConst.ACTIVITY_CAMERA_CAPTURE); 
    }
    
    public void StartPhotoZoom(Uri uri) {    
        Intent intent = new Intent("com.android.camera.action.CROP");    
        intent.setDataAndType(uri, "image/*");    
        intent.putExtra("crop", "true");    
        // aspectX aspectY 是宽高的比例    
        intent.putExtra("aspectX", 1);    
        intent.putExtra("aspectY", 1);    
        // outputX outputY 是裁剪图片宽高    
        intent.putExtra("outputX", 64);    
        intent.putExtra("outputY", 64);    
        intent.putExtra("return-data", true);    
        startActivityForResult(intent, 1);    
    }  
    
	
	public String ParseMultiMediaNoteShareBy(String NoteBody, String Prefix, String Suffix, String Ignore)
	{
		// Clear result set
		MediaUri.clear();
        // Parse 
		String Result = ProjectConst.EmptyStr;
		int DefPos = Prefix.length();
		int SuffixLen = Suffix.length();
		int Start = ProjectConst.Zero; 
		int Pos = ProjectConst.NegativeOne;
		while( (Pos=NoteBody.indexOf(Prefix, Start)) != ProjectConst.NegativeOne )
		{
			int EndPos = NoteBody.indexOf(Suffix, Pos+1);
			String Pic = NoteBody.substring(Pos, EndPos);
			String Type = Pic.substring(DefPos, DefPos+ProjectConst.MediaTagLen);
			if( !Type.equals(Ignore) )
			{
			    MediaUri.add(Pic.substring(DefPos+ProjectConst.MediaTagLen+1));
			    Result = Result + NoteBody.substring(Start, Pos);
			}
			Start = EndPos+SuffixLen;
		}
		Result = Result + NoteBody.substring(Start);
		return Result;
	}
	
	public void ParseSpecificMedia(String NoteBody, String Prefix, String Suffix)
	{
		// Clear result set
		MediaUri.clear();
        // Parse 
		int DefPos = Prefix.length();
		int SuffixLen = Suffix.length();
		int Start = ProjectConst.Zero; 
		int Pos = ProjectConst.NegativeOne;
		while( (Pos=NoteBody.indexOf(Prefix, Start)) != ProjectConst.NegativeOne )
		{
			int EndPos = NoteBody.indexOf(Suffix, Pos+1);
			String Pic = NoteBody.substring(Pos, EndPos);
		    MediaUri.add(Pic.substring(DefPos));
			Start = EndPos+SuffixLen;
		}

	}
}
