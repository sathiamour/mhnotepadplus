package com.android.notepadplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.view.View;
import android.content.Context;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class HelperFunctions{
	
	/** Max lenght of note title to show in widget */
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
	
	// Check wether target floder exists, if not, create it 
	public static boolean IsExistDir(String Path) {   
        File TargetDir = new File(Path);  

        if( TargetDir.exists() )
            return true;  
        else {  
            if( TargetDir.mkdirs() ) 
                return true;
            else {
            	Log.v("EagleTag","file　create　error");
                return false;
            }
        } 
    }
	
    public static int ScreenOrient(Context context)
    {   
 	     DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();  

 	     int landscape = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;//横屏静态常量   
 	     int portrait = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;//竖屏常量   
  	     return dm.widthPixels<dm.heightPixels?portrait:landscape;//判断
    }  
    
	// Refresh widget note list
	public static void RefreshWidgetNoteList(Context ActivityContext, Cursor Notes)
	{     
		  // Just return, if haven't get our widget provider's id
		  if( NotePadWidgetProvider.AppProviderId == 0 ) return;
		  
		  Log.d("log","RefreshWidgetNoteList"+NotePadWidgetProvider.AppProviderId);
		  AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ActivityContext);  
		  RemoteViews widgetViews;
		  int Slot = 0;
		  if( ScreenOrient(ActivityContext) == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ){
		  	  Slot = NotePadWidgetProvider.Widget_Show_Portrait_Slot;
		   	  widgetViews = new RemoteViews(ActivityContext.getPackageName(), R.layout.widgetmain);
		   	  //appWidgetManager.getAppWidgetInfo(NotePadWidgetProvider.AppProviderId).minWidth = NotePadWidgetProvider.Portrait_MinWidth;
		  } else {
			  Log.d("log","in Landscape");
		   	  Slot = NotePadWidgetProvider.Widget_Show_Landscape_Slot;
		   	  widgetViews = new RemoteViews(ActivityContext.getPackageName(), R.layout.widgetmain);
		   	  //appWidgetManager.getAppWidgetInfo(NotePadWidgetProvider.AppProviderId).minWidth = NotePadWidgetProvider.Landscape_MinWidth;
		  }

	      int NoteTitleViews[] = {R.id.note1, R.id.note2, R.id.note3, R.id.note4, R.id.note5};
	      int NoteClockViews[] = {R.id.clock1, R.id.clock2, R.id.clock3, R.id.clock4, R.id.clock5};
		  int count = Notes.getCount();
	      for( int i = 0 ;i < Slot; ++i )
	      {
	    	   if( i < count )
	    	   {
	    		   Notes.moveToPosition(i);
	               String TmpTitle = Notes.getString(Notes.getColumnIndexOrThrow(OneNote.KEY_TITLE));
	               String Title = Integer.toString(i+1)+". "+ HelperFunctions.TitleCheck(TmpTitle);
	               String WetherUseNotifyTime = Notes.getString(Notes.getColumnIndexOrThrow(OneNote.KEY_USE_NOTIFYTIME));
	               if( WetherUseNotifyTime.equals(ProjectConst.Yes) )
	        	       widgetViews.setViewVisibility(NoteClockViews[i], View.VISIBLE);
	               else 
	        	       widgetViews.setViewVisibility(NoteClockViews[i], View.INVISIBLE);
	           
	               widgetViews.setTextViewText(NoteTitleViews[i], Title);
	           } else {
	    		   widgetViews.setTextViewText(NoteTitleViews[i], ProjectConst.EmptyStr);
	    		   widgetViews.setViewVisibility(NoteClockViews[i], View.INVISIBLE);
	    	   }
	      }
	      
    	  appWidgetManager.updateAppWidget(new ComponentName(ActivityContext, NotePadWidgetProvider.class), widgetViews);		
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
	
	// Write note
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
}