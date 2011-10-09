package com.android.hinotes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;
import android.widget.Toast;

public class HelperFunctions{
	
	/** Max length of note title to show in widget */
	public static final int MaxLenOfTitleInListView = 10;
	private static final String TitlePostfix = "...";
	
	// Helper function(String to date)
	public static Calendar String2Calenar(String Time){
		Calendar Result = Calendar.getInstance();
		 try {
		    SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);   
		    Date TmpDate = DateFormat.parse(Time);
		    Result.setTime(TmpDate);
		 }catch(Exception e){
			e.printStackTrace();
		 }
		 
		 return Result;
	}
	
	// Helper function(Date to string)
	public static String Calendar2String(Calendar Time){
		SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String Result = DateFormat.format(Time.getTime());
		return Result;
	}
	
	// Helper function(Format date to readable string like xxxx年xx月xx日xx时xx分)
	public static String FormatCalendar2ReadableStr(Calendar Time){
		String TimeStr = Time.get(Calendar.YEAR) + "年" +
		                 (Time.get(Calendar.MONTH)+1) + "月" + 
		                  Time.get(Calendar.DAY_OF_MONTH) + "日" +
		                  Time.get(Calendar.HOUR_OF_DAY) + "时" +
		                  Time.get(Calendar.MINUTE) + "分";
	   return TimeStr;
	}
	
	public static String FormatCalendar2ReadableStr2(Calendar Time){
		String TimeStr = Time.get(Calendar.YEAR) + "-" +
		                 (Time.get(Calendar.MONTH)+1) + "-" + 
		                  Time.get(Calendar.DAY_OF_MONTH) + " " +
		                  Time.get(Calendar.HOUR_OF_DAY) + ":" +
		                  Time.get(Calendar.MINUTE);
	   return TimeStr;
	}
	// Helper function(Format date to readable string like xxxx年xx月xx日)
	public static String FormatCalendar2ReadablePrefixStr(Calendar Time){
		   String TimeStr = Time.get(Calendar.YEAR) + "年" +
                            (Time.get(Calendar.MONTH)+1) + "月" + 
                            Time.get(Calendar.DAY_OF_MONTH) + "日";
           return TimeStr;
    }
	
	public static String FormatCalendar2ReadableSuffixStr(Calendar Time){
		   String TimeStr = Time.get(Calendar.HOUR_OF_DAY)+"时"+Time.get(Calendar.MINUTE)+"分";
           return TimeStr;
    }
	
	// Compare 2 calendars, just Year & Month & Day
	public static int CmpDatePrefix(Calendar Date1, Calendar Date2){
		   if( Date1.get(Calendar.YEAR) > Date2.get(Calendar.YEAR) ) return 1;
		   else if( Date1.get(Calendar.YEAR) < Date2.get(Calendar.YEAR) ) return -1;
		   else if( Date1.get(Calendar.MONTH) > Date2.get(Calendar.MONTH) ) return 1;
		   else if( Date1.get(Calendar.MONTH) < Date2.get(Calendar.MONTH) ) return -1;
		   else if( Date1.get(Calendar.DAY_OF_MONTH) > Date2.get(Calendar.DAY_OF_MONTH) ) return 1;
		   else if( Date1.get(Calendar.DAY_OF_MONTH) < Date2.get(Calendar.DAY_OF_MONTH) ) return -1;
		   return 0;
	}
	
	public static int CmpDatePrefix2(Calendar Date1, Calendar Date2){
		   if( Date1.get(Calendar.YEAR) > Date2.get(Calendar.YEAR) ) return 1;
		   else if( Date1.get(Calendar.YEAR) < Date2.get(Calendar.YEAR) ) return -1;
		   else if( Date1.get(Calendar.MONTH) > Date2.get(Calendar.MONTH) ) return 1;
		   else if( Date1.get(Calendar.MONTH) < Date2.get(Calendar.MONTH) ) return -1;
		   else if( Date1.get(Calendar.DAY_OF_MONTH) > Date2.get(Calendar.DAY_OF_MONTH) ) return 1;
		   else if( Date1.get(Calendar.DAY_OF_MONTH) < Date2.get(Calendar.DAY_OF_MONTH) ) return -1;
		   else if( Date1.get(Calendar.HOUR_OF_DAY) > Date2.get(Calendar.HOUR_OF_DAY) ) return 1;
		   else if( Date1.get(Calendar.HOUR_OF_DAY) < Date2.get(Calendar.HOUR_OF_DAY) ) return -1;
		   else if( Date1.get(Calendar.MINUTE) > Date2.get(Calendar.MINUTE) ) return 1;
		   else if( Date1.get(Calendar.MINUTE) < Date2.get(Calendar.MINUTE) ) return -1;
		   
		   return 0;
	}
	// Build a about dialog 
	public static Dialog BuildAltertDialog(Context context, int Title, int Message)
	{
   	    AlertDialog.Builder builder = new AlertDialog.Builder(context);
   	    builder.setIcon(R.drawable.alert_dialog_icon);
   	    builder.setTitle(Title);
   	    builder.setMessage(Message);
   	    builder.setPositiveButton(R.string.confirm,
   	         new DialogInterface.OnClickListener(){
   		         public void onClick(DialogInterface dialog, int whichButton){
   		         }      
   		     }
   	    );
   	    
   	    return builder.create();
    }
	
	// Do title check
	public static String TitleCheck(String Title){
		    // Do check the max length is MaxLenOfTitleInListView 
            if( Title.length() > MaxLenOfTitleInListView )
       	        Title = Title.substring(0, MaxLenOfTitleInListView) + TitlePostfix; 
            
            return Title;
	}
	
	// Check whether target folder exists, if not, create it 
	public static boolean IsExistDir(String Path) {   
        File TargetDir = new File(Path);  

        if( TargetDir.exists() )
            return true;  
        else {  
            if( TargetDir.mkdirs() ) 
                return true;
            else {
            	Log.d(ProjectConst.TAG, "Folder "+Path+"　create　error");
                return false;
            }
        } 
    }
	
    public static int ScreenOrient(Context context)
    {   
 	     DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();  
         Log.d(ProjectConst.TAG,"the height & width is "+dm.heightPixels+" width is "+dm.widthPixels);
 	     int landscape = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;//横屏静态常量   
 	     int portrait = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;//竖屏常量   
  	     return dm.widthPixels<dm.heightPixels?portrait:landscape;//判断
    }  
    
	// Refresh widget note list
	public static void RefreshWidgetNoteList(Context ActivityContext, Cursor Notes)
	{     
		  Log.d(ProjectConst.TAG,"In RefreshWidgetNoteList to refresh main widget. The AppProviderId is "+NotePadWidgetProvider.AppProviderId);
		  // Just return, if haven't get our widget provider's id
		  // if( NotePadWidgetProvider.AppProviderId == 0 ) return;
		  
		  
		  AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ActivityContext);
		  RemoteViews widgetViews = new RemoteViews(ActivityContext.getPackageName(), R.layout.widgetview4x2);
		  int Slot = 0;
		  if( ScreenOrient(ActivityContext) == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT )
		  {
		  	  Slot = NotePadWidgetProvider.Widget_Show_Portrait_Slot;
		  	  Log.d(ProjectConst.TAG,"it is portrait");
		  } else
		   	  Slot = NotePadWidgetProvider.Widget_Show_Landscape_Slot;

	      int NoteTitleViews[] = {R.id.note1, R.id.note2, R.id.note3, R.id.note4, R.id.note5};
		  int count = Notes.getCount();
	      for( int i = 0 ;i < Slot; ++i )
	      {
	    	   if( i < count )
	    	   {
	    		   Notes.moveToPosition(i);
	               String TmpTitle = Notes.getString(Notes.getColumnIndexOrThrow(OneNote.KEY_TITLE));
	               String Title = Integer.toString(i+1)+". "+ TmpTitle;//HelperFunctions.TitleCheck(TmpTitle);
	           
	               widgetViews.setTextViewText(NoteTitleViews[i], Title);
	           } else
	    		   widgetViews.setTextViewText(NoteTitleViews[i], ProjectConst.EmptyStr);
	      }
	      
	      // Add pending intent to main activity
	      Intent showAllNotes = new Intent(ActivityContext, NotePadPlus.class);
	      showAllNotes.setAction(ProjectConst.WIDGET4x2_SHOWALL_ACTION);
		  PendingIntent showAllNotesPendingIntent = PendingIntent.getActivity(ActivityContext, 0, showAllNotes, 0);
		  widgetViews.setOnClickPendingIntent(R.id.widgetboard, showAllNotesPendingIntent);

    	  appWidgetManager.updateAppWidget(new ComponentName(ActivityContext, NotePadWidgetProvider.class), widgetViews);		
	}
 
    public static void Refresh1x1Widget(Context AppContext, int AppWidgetId, String Title, int RowId, int ClrId, boolean IsLocked, int Type)
    {        
		   // Create remote views
		   RemoteViews remoteViews = new RemoteViews(AppContext.getPackageName(), R.layout.widgetview1x1);
	       remoteViews.setTextViewText(android.R.id.text1, Title);
	       remoteViews.setImageViewBitmap(android.R.id.background, HelperFunctions.GetAlpha1x1Bg(AppContext, R.drawable.bg_note_1x1, 75, 75, ClrId));
		
	       AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(AppContext); 
	       
	       Intent ActivityIntent = null;
		    if( IsLocked ){
		    	ActivityIntent = new Intent(AppContext, NotificationPwdDlgActivity.class);
		    	remoteViews.setViewVisibility(R.id.widget1x1_lock, View.VISIBLE);
		    } else {
				if( Type == OneNote.ListNote )
					ActivityIntent = new Intent(AppContext, EditCheckListNoteActivity.class);
		        else if( Type == OneNote.TextNote )
		        	ActivityIntent = new Intent(AppContext, EditNoteActivity.class);
		    	remoteViews.setViewVisibility(R.id.widget1x1_lock, View.GONE);
		    }
		    ActivityIntent.putExtra(OneNote.KEY_ROWID, RowId);
		    ActivityIntent.putExtra(ProjectConst.KEY_SOURCE, ProjectConst.WIDGET1x1_EDIT_ACTION);
			PendingIntent EditNotePendingIntent = PendingIntent.getActivity(AppContext, AppWidgetId, ActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(android.R.id.background, EditNotePendingIntent);
			
	        appWidgetManager.updateAppWidget(AppWidgetId, remoteViews);		   
    }
    
    public static Bitmap DecodeBitmapFromUri(Context AppContext, Uri uri, int DefinedW, int DefinedH) throws FileNotFoundException{  
        ContentResolver cr = AppContext.getContentResolver();  
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        Bitmap Target = null;
        try {
		    BitmapFactory.decodeStream(cr.openInputStream(uri), null,opts); 
        }catch (OutOfMemoryError err) {
			return Target;
		} 
        
        opts.inSampleSize = computeSampleSize(opts, -1, DefinedW*DefinedH);		
        opts.inJustDecodeBounds = false;
        
        try {
        	Target = BitmapFactory.decodeStream(cr.openInputStream(uri), null, opts); 
        } catch (OutOfMemoryError err) {
        	return Target;
        }
	 
	    return Target;
    }  
    
	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
	    int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

	    int roundedSize;
	    if (initialSize <= 8) {
	        roundedSize = 1;
	        while (roundedSize < initialSize) {
	            roundedSize <<= 1;
	        }
	    } else {
	        roundedSize = (initialSize + 7) / 8 * 8;
	    }

	    return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
	    double w = options.outWidth;
	    double h = options.outHeight;

	    int lowerBound = (maxNumOfPixels == -1) ? 1 :
	            (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
	    int upperBound = (minSideLength == -1) ? 128 :
	            (int) Math.min(Math.floor(w / minSideLength),
	            Math.floor(h / minSideLength));

	    if (upperBound < lowerBound) {
	        // return the larger one when there is no overlapping zone.
	        return lowerBound;
	    }

	    if ((maxNumOfPixels == -1) &&
	            (minSideLength == -1)) {
	        return 1;
	    } else if (minSideLength == -1) {
	        return lowerBound;
	    } else {
	        return upperBound;
	    }
	}
	public static Bitmap GetAlpha1x1Bg(Context AppCtx, int DrawableId, int W, int H, int BgClrId){
	    InputStream is = AppCtx.getResources().openRawResource(DrawableId);
	    Bitmap Src = BitmapFactory.decodeStream(is);
	    Bitmap AlphaBg = Src.extractAlpha();
	    Paint p = new Paint();
        p.setColor(NotePadPlus.ItemBgClr[BgClrId]);
        Bitmap Bg = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(Bg);
        canvas.drawBitmap(AlphaBg, 0, 0, p);
        return Bg;
	}
	
	public static Bitmap CreateTitleBarBg(int Width, int Height, int StartClr, int EndClr){
		Shader mShader = new LinearGradient(0, 0, Width, Height, new int[] {StartClr, EndClr}, null, Shader.TileMode.MIRROR);
	    Bitmap Bg = Bitmap.createBitmap(Width, Height, Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(Bg);
		Paint p = new Paint();
        //p.setColor(Color.RED);
        p.setShader(mShader);
        canvas.drawPaint(p);
       
        return Bg;
	    
	}
	
	public static String ComposeSharedItems(ArrayList<String> Items, int Start, int End)
	{
		String Result = ProjectConst.EmptyStr;
		for( int i = Start; i <= End; ++i )
			 Result += Integer.toString(i)+"."+Items.get(i)+"\n";
		return Result;
	}
	
	
	public static Dialog BuildTextPlainShareByDlg(final Context AppContext, int Title, final String SharedTitle, final String SharedBody)
	{
        Builder builder = new AlertDialog.Builder(AppContext);  
        builder.setIcon(R.drawable.ic_dialog_menu_generic);  
        builder.setTitle(Title);  
        final BaseAdapter adapter = new ShareByListItemAdapter(AppContext, Intent.ACTION_SEND, "text/plain");  
        DialogInterface.OnClickListener listener =   
            new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialogInterface, int which) { 
                	ShareByListItemAdapter Adapter = (ShareByListItemAdapter)adapter;
                	Intent intent = new Intent();
                	
                    intent.setAction(Intent.ACTION_SEND);
                	intent.setComponent(new ComponentName(Adapter.Apps.get(which).activityInfo.packageName, Adapter.Apps.get(which).activityInfo.name));
                    intent.putExtra(Intent.EXTRA_SUBJECT, SharedTitle);
                    intent.putExtra(Intent.EXTRA_TEXT, SharedBody);
                    intent.setType("text/plain");
                    
                    AppContext.startActivity(intent); 
                }  
            };  
        builder.setAdapter(adapter, listener);  
        return builder.create();  
	}
	
	public static Dialog BuildMediaShareByDlg(final Context AppContext, int Title, final String SharedTitle, final String SharedBody, final Vector<String> MultiMediaUri)
	{
        Builder builder = new AlertDialog.Builder(AppContext);  
        builder.setIcon(R.drawable.ic_dialog_menu_generic);  
        builder.setTitle(Title);  
        final BaseAdapter adapter = new ShareByListItemAdapter(AppContext, Intent.ACTION_SEND_MULTIPLE, "image/*");  
        DialogInterface.OnClickListener listener =   
            new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialogInterface, int which) { 
                	ShareByListItemAdapter Adapter = (ShareByListItemAdapter)adapter;
                	Intent intent = new Intent();
                	
                	int Count = MultiMediaUri.size();
                	 
                    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                	intent.setComponent(new ComponentName(Adapter.Apps.get(which).activityInfo.packageName, Adapter.Apps.get(which).activityInfo.name));
                    intent.putExtra(Intent.EXTRA_SUBJECT, SharedTitle);               
                    intent.putExtra(Intent.EXTRA_TEXT, SharedBody);
                    ArrayList<Uri> AttachList = new ArrayList<Uri>();
                    for( int i = 0; i < Count; ++i )
                    	 AttachList.add(Uri.parse(MultiMediaUri.get(i)));
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, AttachList);
                    intent.setType("image/*");
                    
                    AppContext.startActivity(intent); 
                }  
            };  
        builder.setAdapter(adapter, listener);  
        return builder.create();  
	}

	public static String MakeCameraFolder()
	{
	    String SDFolder = Environment.getExternalStorageDirectory()+"/dcim/Camera";
	    if( !IsExistDir(SDFolder) )
	    	return ProjectConst.EmptyStr;
	    return SDFolder;
	}
	
 
	public static String WriteFile2SD(Context AppContext, String FileName, String Content)
	{
		String Info = AppContext.getString(R.string.opt_sdfile_err_prompt);
		if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {   
            FileOutputStream fileOutputStream = null;   
            OutputStreamWriter osWriter = null; 
            try {   
            	    String SDFolder = Environment.getExternalStorageDirectory()+"/"+ProjectConst.AppName;
            	    if( !IsExistDir(SDFolder) )
            	    	return Info;
                    File file = new File(SDFolder,FileName);   
                    fileOutputStream = new FileOutputStream(file);   
        		    osWriter = new OutputStreamWriter(fileOutputStream);       
        		    osWriter.write(Content);
        		    osWriter.flush(); 
        			String StrFormat = AppContext.getResources().getString(R.string.opt_sdfile_success_prompt);	    
        			Info = String.format(StrFormat, SDFolder+"/"+FileName);
        		    return Info;
            } catch (Exception e) {   
                    // TODO Auto-generated catch block   
                    //Log.i(TAGSTRING, e.toString());   
                    //info = R.string.infor;
            	    return Info;
            } finally {   
                    try {   
                    	  if( osWriter != null )
                    		  osWriter.close();
                          if( fileOutputStream != null )    
                              fileOutputStream.close();   
                           
                    } catch ( IOException e) {   
                          //Log.i(TAGSTRING, e.toString());   
                          //info = R.string.infor; 
                	      return Info;
                    }   
           }   
       } else {
    	   Info = AppContext.getString(R.string.sd_fail_prompt);
    	   return Info;
       }
    }
	
	// Read note
	public static String ReadTextFile(Context context, String path){
		FileInputStream fileInStream = null;
		InputStreamReader isReader = null;
		char inputBuf[] = new char[255];
		String Content = ProjectConst.EmptyStr;
		int ReadCount=0;
		try{
			fileInStream = context.openFileInput(path);  
			isReader = new InputStreamReader(fileInStream);
            while( (ReadCount=isReader.read(inputBuf)) != -1 )
            	   Content += new String(inputBuf, 0, ReadCount);
        } catch (Exception e) {      
            e.printStackTrace();
            Toast.makeText(context, R.string.readnotefile_fail_err,Toast.LENGTH_SHORT).show();
        } finally {
            try {
        	   fileInStream.close();
        	   isReader.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
        Content.trim();
		return Content;
	}
	
    private static String SplitItem(String Item)
    {
        return Item.substring(Item.indexOf(".")+1);	
    }
    
	public static ArrayList<String> ReadCheckListFile(Context context, String Path){
		FileInputStream fileInStream = null;
		InputStreamReader InStream = null;
		BufferedReader BufReader = null;
        ArrayList<String> Items = new ArrayList<String>();
        Items.add(context.getString(R.string.checklist_add));
		try {
			  fileInStream = context.openFileInput(Path);
		      InStream =new InputStreamReader(fileInStream, "UTF-8");
	          BufReader = new BufferedReader(InStream);
	          String Item = ProjectConst.EmptyStr;

              while( (Item=BufReader.readLine())!=null ) 
              {
        	         Items.add(SplitItem(Item.trim()));
	          }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
				BufReader.close();
		        InStream.close();
		        fileInStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Items.add(context.getString(R.string.checklist_add));
        return Items;
	}
	
	// Write text note
	public static void WriteTextFile(Context context, String Data, String Path){   
		FileOutputStream fileOutStream = null;     
		OutputStreamWriter osWriter = null;             
		try {
			fileOutStream = context.openFileOutput(Path, Context.MODE_WORLD_READABLE);
		    osWriter = new OutputStreamWriter(fileOutStream);       
		    osWriter.write(Data);
		    osWriter.flush(); 
		} catch (Exception e) {        
			e.printStackTrace();             
			Toast.makeText(context, R.string.notenotsave_tip, Toast.LENGTH_SHORT).show();                   
        } finally {
        	try{
		       osWriter.close();
		       fileOutStream.close();
        	} catch (IOException e) {
        		e.printStackTrace();
            }
        }
	}
	
	// Write check list note
	public static void WriteCheckListFile(Context context, ArrayList<String> Items, int Start, int End, String Path){   
		FileOutputStream fileOutStream;
		OutputStreamWriter osWriter;
		try {
			fileOutStream = context.openFileOutput(Path, Context.MODE_WORLD_READABLE);
			osWriter = new OutputStreamWriter(fileOutStream);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}; 
		
        try {
		      for( int i = Start; i <= End; ++i )
		      {
			       String Data = Integer.toString(i)+"."+Items.get(i)+"\n";
	               osWriter.write(Data);
	          }
		      osWriter.flush();
        } catch (Exception e) {        
	          e.printStackTrace();             
	          Toast.makeText(context, R.string.notenotsave_tip, Toast.LENGTH_SHORT).show();                   
        } finally {
	          try{
                   osWriter.close();
                   fileOutStream.close();
	          } catch (IOException e) {
		           e.printStackTrace();
              }
        }
	}
	
	// Save bitmap to jpeg
	public static boolean SaveBmpPicture(Bitmap Pic, String Path)
	{
	    FileOutputStream  BmpFileOutPutStream = null; 
	    try { 
		     BmpFileOutPutStream = new FileOutputStream(Path);
		} catch (FileNotFoundException e) { 
		     // TODO Auto-generated catch block 
			 e.printStackTrace(); 
			 return false;
	    } 
		Pic.compress(Bitmap.CompressFormat.JPEG, 100, BmpFileOutPutStream);
		try { 
		      BmpFileOutPutStream.flush(); 
		      BmpFileOutPutStream.close(); 
		} catch (IOException e) { 
		      // TODO Auto-generated catch block 
		      e.printStackTrace(); 
		      return false;
		} 
		
		return true;
		
	}
}