package com.android.notepadplus;

import java.util.Calendar;
import java.util.Random;

import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable {

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
	

    /** Ring notify setting */	
	public static final String[] RingTypeStr = {
        "ÿ����־����һ��","ÿ5��������һ��",
        "ÿ10��������һ��","ÿ15��������һ��",
        "ÿ20��������һ��","ÿ25��������һ��",
        "ÿ30��������һ��"};
    public static final int[] NotifyDuraTime = {0,5,10,15,20,25,30};
    public static final String[] NotifyMethodTypeStr = {
	    "��Ϣ������", "�𶯺���Ϣ������", "�������Ϣ������", "�𶯡��������Ϣ������"
    };
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
	
	public Note(){
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
	
	public void GenerateImgIdx(){
		   Random Rand = new Random();
 	       TagImgIdx = Rand.nextInt(NotePadPlus.ClrNum);
 	       ItemBgIdx = TagImgIdx;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub

	}

}
