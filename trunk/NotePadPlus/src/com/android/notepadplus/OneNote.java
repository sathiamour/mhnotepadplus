package com.android.notepadplus;

import java.util.Calendar;
import java.util.Random;

import android.database.Cursor;
import android.os.Bundle;

public class OneNote {

	/** Note's database column name */
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CREATED = "created_time";
	public static final String KEY_UPDATED = "updated_time";
	public static final String KEY_PATH = "path";
	public static final String KEY_ENDTIME = "end_time";
	public static final String KEY_NOTIFYTIME = "notify_time";
	public static final String KEY_USE_ENDTIME = "use_endtime";
	public static final String KEY_USE_NOTIFYTIME = "use_notifytime";
	public static final String KEY_DELNOTE_EXP = "delnoteexp";
	public static final String KEY_TAGIMG_ID = "tagimg_id";
	public static final String KEY_BGCLR = "bgclr";
	public static final String KEY_NOTIFYDURA = "notifydura";
	public static final String KEY_NOTIFYMETHOD = "notifymethod";
	public static final String KEY_RINGMUSIC = "ringmusic";
	public static final String KEY_NOTIFY_RINGTIME = "notify_ringtime";
	public static final String KEY_PWD = "pwd";
	
	/** Date parameter key */
	//public static final String KEY_YEAR = "Year";
	//public static final String KEY_MONTH = "Month";
	//public static final String KEY_DAY = "Day";
	//public static final String KEY_HOUR = "Hour";
	//public static final String KEY_MINUTE = "Minute";
	
	
	public static final String[] RingTypeStr = {
        "只提醒一次","每5分钟提醒一次",
        "每10分钟提醒一次","每15分钟提醒一次",
        "每20分钟提醒一次","每25分钟提醒一次",
        "每30分钟提醒一次"};
    public static final int[] NotifyDuraTime = {0,5,10,15,20,25,30};
    private static int NotifyDuraOnce = 0;
    public static final String[] NotifyMethodTypeStr = {
	    "消息栏提醒", "振动和消息栏提醒", "响铃和消息栏提醒", "振动、响铃和消息栏提醒"
    };
    private static final String Vibration = "振动";
    private static final String Ring="响铃";
    public static final String InvalidateNotifyTime = ProjectConst.InvalidateDate;
    public static final String SilentMusicTitle = "Silent";

	
	public String NoteTitle;
	public String NoteBody;
	public String NoteFilePath;
	public Calendar EndTime;
	public Calendar NotifyTime;
	public String Use_EndTime;
	public String Use_NotifyTime;
	public String DelNoteExp;
	public String RingMusic;
	public int NoteRowId;
	public int TagImgIdx;
	public int ItemBgIdx;
	public int NotifyDura;
	public int NotifyMethod;
	
	public OneNote(){
		   GenerateImgIdx();
		   
		   EndTime = Calendar.getInstance();
		   NotifyTime = Calendar.getInstance();
		   Use_EndTime = ProjectConst.No; 
		   Use_NotifyTime = ProjectConst.No;
		   DelNoteExp = ProjectConst.No;
		   RingMusic = ProjectConst.EmptyStr;
		   NotifyDura = 0;
		   NotifyMethod = 0;
	}
	
	public OneNote(Cursor DbNote){
		   NoteRowId = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_ROWID));
		   NoteTitle = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_TITLE));
		   NoteFilePath = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_PATH));
		   EndTime = HelperFunctions.String2Calenar(DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_ENDTIME)));
		   Use_EndTime = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_USE_ENDTIME));
		   NotifyTime = HelperFunctions.String2Calenar(DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_NOTIFYTIME)));
		   Use_NotifyTime = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_USE_NOTIFYTIME));
		   DelNoteExp = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_DELNOTE_EXP));
		   TagImgIdx = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_TAGIMG_ID));
		   ItemBgIdx = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_BGCLR));
		   NotifyDura = DbNote.getInt(DbNote.getColumnIndexOrThrow(OneNote.KEY_NOTIFYDURA));
		   NotifyMethod = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_NOTIFYMETHOD));
		   RingMusic = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_RINGMUSIC));
	}
	
	public OneNote(Bundle Parameters)
	{
		   NoteTitle = Parameters.getString(OneNote.KEY_TITLE);
		   NoteFilePath = Parameters.getString(OneNote.KEY_PATH);
		   NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
		   EndTime = HelperFunctions.String2Calenar(Parameters.getString(OneNote.KEY_ENDTIME));
		   NotifyTime = HelperFunctions.String2Calenar(Parameters.getString(OneNote.KEY_NOTIFYTIME));
		   Use_EndTime = Parameters.getString(OneNote.KEY_USE_ENDTIME);
		   Use_NotifyTime = Parameters.getString(OneNote.KEY_USE_NOTIFYTIME);
		   DelNoteExp = Parameters.getString(OneNote.KEY_DELNOTE_EXP);
		   TagImgIdx = Parameters.getInt(OneNote.KEY_TAGIMG_ID); 
		   ItemBgIdx = Parameters.getInt(OneNote.KEY_BGCLR); 
		   NotifyDura = Parameters.getInt(OneNote.KEY_NOTIFYDURA);
		   NotifyMethod = Parameters.getInt(OneNote.KEY_NOTIFYMETHOD);
		   RingMusic = Parameters.getString(OneNote.KEY_RINGMUSIC);
	}
	
	public void GenerateImgIdx(){
		   Random Rand = new Random();
 	       TagImgIdx = Rand.nextInt(NotePadPlus.ClrNum);
 	       ItemBgIdx = TagImgIdx;
	}
	
	public static boolean IsVibrate(int MethodIdx){
		   return (NotifyMethodTypeStr[MethodIdx].indexOf(Vibration) != -1);
	}
	
	public static boolean IsRing(int MethodIdx){
		   return (NotifyMethodTypeStr[MethodIdx].indexOf(Ring) != -1);
	}
	
	public static boolean IsRepeat(int NotifyDuraIdx){
		   return (NotifyDuraIdx != NotifyDuraOnce);
	}

	public static int GetNotifyDura(int NotifyDuraIdx){
		   return NotifyDuraTime[NotifyDuraIdx];
	}
}