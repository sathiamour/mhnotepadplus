package com.android.hinotes;

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
	public static final String KEY_NOTIFYTIME = "notify_time";
	public static final String KEY_USE_NOTIFYTIME = "use_notifytime";
	public static final String KEY_DRAWABLE_ID = "drawableid";
	public static final String KEY_NOTIFYDURA = "notifydura";
	public static final String KEY_NOTIFYMETHOD = "notifymethod";
	public static final String KEY_RINGMUSIC = "ringmusic";
	public static final String KEY_PWD = "pwd";
	public static final String KEY_RANK = "rank";
	public static final String KEY_WIDGETID = "widgetid";
	public static final String KEY_LEFTX = "leftx";
	public static final String KEY_LEFTY = "lefty";
	public static final String KEY_INDEX = "index";
	public static final String KEY_NUM = "num";
	public static final String KEY_NOTETYPE = "notetype";
	
	/** Date parameter key */
	//public static final String KEY_YEAR = "Year";
	//public static final String KEY_MONTH = "Month";
	//public static final String KEY_DAY = "Day";
	//public static final String KEY_HOUR = "Hour";
	//public static final String KEY_MINUTE = "Minute";
	
	
	public static final String[] RingTypeStr = {
        "只提醒一次","每5分钟提醒一次",
        "每10分钟提醒一次", "每20分钟提醒一次", "每30分钟提醒一次",
        "每天提醒一次", "每周提醒一次"};
    public static final int[] NotifyDuraTime = {0,5,10,20,30,1440};
    private static int NotifyDuraOnce = 0;
    public static final String[] NotifyMethodTypeStr = {
	    "消息栏提醒", "振动和消息栏提醒", "响铃和消息栏提醒", "振动、响铃和消息栏提醒"
    };
    private static final String Vibration = "振动";
    private static final String Ring="响铃";
    public static final String InvalidateNotifyTime = ProjectConst.InvalidateDate;
    public static final String SilentMusicTitle = "Silent";
    // Note type
    public static final int TextNote = 0;
    public static final int ListNote = 1;
    public static final int MultiMediaNote = 2;
    public static final int ScrawlNote = 3;
	
	public String NoteTitle;
	public String NoteBody;
	public String NoteFilePath;
	public Calendar NotifyTime;
	public String Use_NotifyTime;
	public String RingMusic;
	public String Password;
	public int NoteRowId;
	public int DrawableResIdx;
	public int NotifyDura;
	public int NotifyMethod;
	public int WidgetId;
	public int PosIdx;
	public int NoteType;
	
	public OneNote(int Type){
		   GenerateDrawableIdx();
		   
		   NotifyTime = Calendar.getInstance();
		   Use_NotifyTime = ProjectConst.No;
		   NoteBody = ProjectConst.EmptyStr;
		   NoteTitle = ProjectConst.EmptyStr;
		   RingMusic = ProjectConst.EmptyStr;
		   Password = ProjectConst.EmptyStr;
		   PosIdx = ProjectConst.NegativeOne;
		   NotifyDura = ProjectConst.Zero;
		   NotifyMethod = 3;
		   WidgetId = ProjectConst.Zero;
		   NoteType = Type;
	}
	
	public OneNote(Cursor DbNote)
	{
		   InitializatonFromDb(DbNote);
	}
	
	public OneNote(Bundle Parameters)
	{
		   NoteTitle = Parameters.getString(OneNote.KEY_TITLE);
		   NoteFilePath = Parameters.getString(OneNote.KEY_PATH);
		   NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
		   NotifyTime = HelperFunctions.String2Calenar(Parameters.getString(OneNote.KEY_NOTIFYTIME));
		   Use_NotifyTime = Parameters.getString(OneNote.KEY_USE_NOTIFYTIME);
		   DrawableResIdx = Parameters.getInt(OneNote.KEY_DRAWABLE_ID);  
		   NotifyDura = Parameters.getInt(OneNote.KEY_NOTIFYDURA);
		   NotifyMethod = Parameters.getInt(OneNote.KEY_NOTIFYMETHOD);
		   RingMusic = Parameters.getString(OneNote.KEY_RINGMUSIC);
		   Password = Parameters.getString(OneNote.KEY_PWD);
		   WidgetId = Parameters.getInt(OneNote.KEY_WIDGETID);
	}
	
	public void InitializatonFromDb(Cursor DbNote){
		   NoteRowId = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_ROWID));
		   NoteTitle = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_TITLE));
		   NoteFilePath = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_PATH));
		   NotifyTime = HelperFunctions.String2Calenar(DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_NOTIFYTIME)));
		   Use_NotifyTime = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_USE_NOTIFYTIME));
		   DrawableResIdx = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_DRAWABLE_ID));
		   NotifyDura = DbNote.getInt(DbNote.getColumnIndexOrThrow(OneNote.KEY_NOTIFYDURA));
		   NotifyMethod = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_NOTIFYMETHOD));
		   RingMusic = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_RINGMUSIC));
		   Password = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_PWD));
		   WidgetId = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_WIDGETID));
	}
	
	public void GenerateDrawableIdx(){
		   Random Rand = new Random();
		   DrawableResIdx = Rand.nextInt(NotePadPlus.ClrNum);
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