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
	public static final String KEY_NOTIFYTIME = "notify_time";
	public static final String KEY_USE_NOTIFYTIME = "use_notifytime";
	public static final String KEY_TAGIMG_ID = "tagimg_id";
	public static final String KEY_BGCLR = "bgclr";
	public static final String KEY_NOTIFYDURA = "notifydura";
	public static final String KEY_NOTIFYMETHOD = "notifymethod";
	public static final String KEY_RINGMUSIC = "ringmusic";
	public static final String KEY_PWD = "pwd";
	public static final String KEY_RANK = "rank";
	public static final String KEY_WIDGETID = "widgetid";
	public static final String KEY_INDEX = "index";
	
	/** Date parameter key */
	//public static final String KEY_YEAR = "Year";
	//public static final String KEY_MONTH = "Month";
	//public static final String KEY_DAY = "Day";
	//public static final String KEY_HOUR = "Hour";
	//public static final String KEY_MINUTE = "Minute";
	
	
	public static final String[] RingTypeStr = {
        "ֻ����һ��","ÿ5��������һ��",
        "ÿ10��������һ��", "ÿ20��������һ��", "ÿ30��������һ��",
        "ÿ������һ��", "ÿ������һ��"};
    public static final int[] NotifyDuraTime = {0,5,10,20,30,1440,};
    private static int NotifyDuraOnce = 0;
    public static final String[] NotifyMethodTypeStr = {
	    "��Ϣ������", "�񶯺���Ϣ������", "�������Ϣ������", "�񶯡��������Ϣ������"
    };
    private static final String Vibration = "��";
    private static final String Ring="����";
    public static final String InvalidateNotifyTime = ProjectConst.InvalidateDate;
    public static final String SilentMusicTitle = "Silent";

	
	public String NoteTitle;
	public String NoteBody;
	public String NoteFilePath;
	public Calendar NotifyTime;
	public String Use_NotifyTime;
	public String RingMusic;
	public String Password;
	public int NoteRowId;
	public int TagImgIdx;
	public int ItemBgIdx;
	public int NotifyDura;
	public int NotifyMethod;
	public int WidgetId;
	
	public OneNote(){
		   GenerateImgIdx();
		   
		   NotifyTime = Calendar.getInstance();
		   Use_NotifyTime = ProjectConst.No;
		   NoteBody = ProjectConst.EmptyStr;
		   NoteTitle = ProjectConst.EmptyStr;
		   RingMusic = ProjectConst.EmptyStr;
		   NotifyDura = 0;
		   NotifyMethod = 0;
		   WidgetId = 0;
	}
	
	public OneNote(Cursor DbNote){
		   NoteRowId = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_ROWID));
		   NoteTitle = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_TITLE));
		   NoteFilePath = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_PATH));
		   NotifyTime = HelperFunctions.String2Calenar(DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_NOTIFYTIME)));
		   Use_NotifyTime = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_USE_NOTIFYTIME));
		   TagImgIdx = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_TAGIMG_ID));
		   ItemBgIdx = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_BGCLR));
		   NotifyDura = DbNote.getInt(DbNote.getColumnIndexOrThrow(OneNote.KEY_NOTIFYDURA));
		   NotifyMethod = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_NOTIFYMETHOD));
		   RingMusic = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_RINGMUSIC));
		   Password = DbNote.getString(DbNote.getColumnIndexOrThrow(KEY_PWD));
		   WidgetId = DbNote.getInt(DbNote.getColumnIndexOrThrow(KEY_WIDGETID));
	}
	
	public OneNote(Bundle Parameters)
	{
		   NoteTitle = Parameters.getString(OneNote.KEY_TITLE);
		   NoteFilePath = Parameters.getString(OneNote.KEY_PATH);
		   NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
		   NotifyTime = HelperFunctions.String2Calenar(Parameters.getString(OneNote.KEY_NOTIFYTIME));
		   Use_NotifyTime = Parameters.getString(OneNote.KEY_USE_NOTIFYTIME);
		   TagImgIdx = Parameters.getInt(OneNote.KEY_TAGIMG_ID); 
		   ItemBgIdx = Parameters.getInt(OneNote.KEY_BGCLR); 
		   NotifyDura = Parameters.getInt(OneNote.KEY_NOTIFYDURA);
		   NotifyMethod = Parameters.getInt(OneNote.KEY_NOTIFYMETHOD);
		   RingMusic = Parameters.getString(OneNote.KEY_RINGMUSIC);
		   Password = Parameters.getString(OneNote.KEY_PWD);
		   WidgetId = Parameters.getInt(OneNote.KEY_WIDGETID);
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