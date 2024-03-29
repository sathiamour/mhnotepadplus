package com.android.hinotes;

import java.util.Calendar;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class NoteDbAdapter {

	// Database class
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
    // Note database table create sql
	private static final String DATABASE_CREATE = "create table diary (_id integer primary key autoincrement, "
			+ "title text not null, path text not null, created_time text not null, "
			+ "updated_time text not null," 
			+ "notify_time text not null, use_notifytime text not null, "
			+ "drawableid integer not null,"
			+ "ringmusic text not null,notifydura integer not null, "
			+ "notifymethod integer not null, "
			+ "pwd text, rank integer not null, widgetid integer not null," 
			+ "leftx integer not null, lefty integer not null,"
			+ "notetype integer not null)";
    // Database name & table name & database version
	private static final String DATABASE_NAME = "database";
	private static final String DATABASE_TABLE = "diary";
	private static final int DATABASE_VERSION = 24;
	// Order by options
	private static String OrderBy;
	public static final String OrderByCreatedTime = "_id desc";
	public static final String OrderByUpdatedTime = "updated_time desc";
	public static final String OrderByTagClr = "drawableid";
	public static final String OrderByTitle = "title";
	public static final String OrderByRank = "rank  desc";
	public static final String[] OrderByArray={OrderByCreatedTime,OrderByUpdatedTime,OrderByTitle,OrderByTagClr, OrderByRank};
	
    // Database context(application's context)
	private final Context mCtx;
	

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS diary");
			onCreate(db);
		}
	}

	public NoteDbAdapter(Context Ctx) {
		this.mCtx = Ctx;
		
	}

	public NoteDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();

		// Read order by
		SharedPreferences PrefSettings = PreferenceManager.getDefaultSharedPreferences(mCtx);   
		String OrderByIdx = PrefSettings.getString(AppSetting.Key_PrefOrderBy, AppSetting.OrderByUpdatedTime);
		OrderBy = OrderByArray[Integer.parseInt(OrderByIdx)];
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long CreateOneNote(OneNote Note) {
		ContentValues InitialValues = new ContentValues();
		InitialValues.put(OneNote.KEY_TITLE, Note.NoteTitle);
		InitialValues.put(OneNote.KEY_PATH, Note.NoteFilePath);
		InitialValues.put(OneNote.KEY_CREATED, HelperFunctions.FormatCalendar2ReadableStr2(Calendar.getInstance()));
		InitialValues.put(OneNote.KEY_UPDATED, HelperFunctions.Calendar2String(Calendar.getInstance()));
		InitialValues.put(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(Note.NotifyTime));
		InitialValues.put(OneNote.KEY_USE_NOTIFYTIME, Note.Use_NotifyTime);
		InitialValues.put(OneNote.KEY_DRAWABLE_ID, Note.DrawableResIdx);
		InitialValues.put(OneNote.KEY_RINGMUSIC, Note.RingMusic);
		InitialValues.put(OneNote.KEY_NOTIFYDURA, Note.NotifyDura);
		InitialValues.put(OneNote.KEY_NOTIFYMETHOD, Note.NotifyMethod);
		InitialValues.put(OneNote.KEY_PWD, ProjectConst.EmptyStr);
		InitialValues.put(OneNote.KEY_RANK, ProjectConst.Zero);
		InitialValues.put(OneNote.KEY_WIDGETID, ProjectConst.Zero);
		InitialValues.put(OneNote.KEY_LEFTX, ProjectConst.NegativeOne);
		InitialValues.put(OneNote.KEY_LEFTY, ProjectConst.NegativeOne);
		InitialValues.put(OneNote.KEY_NOTETYPE, Note.NoteType);
		
		return mDb.insert(DATABASE_TABLE, null, InitialValues);
	}

	public boolean DeleteOneNote(long RowId) {

		return mDb.delete(DATABASE_TABLE, OneNote.KEY_ROWID + "=" + RowId, null) > 0;
	}

	public Cursor GetAllNotes() {
        /** Order by created date descending order */
		return mDb.query(DATABASE_TABLE, 
				         new String[] { OneNote.KEY_ROWID, OneNote.KEY_TITLE, OneNote.KEY_PATH, OneNote.KEY_UPDATED,
				                        OneNote.KEY_CREATED, OneNote.KEY_NOTIFYTIME, OneNote.KEY_USE_NOTIFYTIME,
				                        OneNote.KEY_DRAWABLE_ID, OneNote.KEY_RINGMUSIC,
				                        OneNote.KEY_NOTIFYDURA, OneNote.KEY_NOTIFYMETHOD, OneNote.KEY_PWD, 
				                        OneNote.KEY_RANK, OneNote.KEY_WIDGETID,
				                        OneNote.KEY_LEFTX, OneNote.KEY_LEFTY, OneNote.KEY_NOTETYPE}, 
				         null, null, null, null, OrderBy);
	}

	public Cursor GetNotesByCondition(String Condition)
	{
		return mDb.query(DATABASE_TABLE, 
		         new String[] { OneNote.KEY_ROWID, OneNote.KEY_TITLE, OneNote.KEY_PATH, OneNote.KEY_UPDATED,
		                        OneNote.KEY_CREATED, 
		                        OneNote.KEY_NOTIFYTIME, OneNote.KEY_USE_NOTIFYTIME,
		                        OneNote.KEY_DRAWABLE_ID, OneNote.KEY_RINGMUSIC,
		                        OneNote.KEY_NOTIFYDURA, OneNote.KEY_NOTIFYMETHOD, OneNote.KEY_PWD, 
		                        OneNote.KEY_RANK, OneNote.KEY_WIDGETID,
		                        OneNote.KEY_LEFTX, OneNote.KEY_LEFTY, OneNote.KEY_NOTETYPE}, 
		                        Condition, null, null, null, OrderBy);
	}

	public Cursor GetNotesByConditionByOrder(String Condition, String UserOrderBy){
		return mDb.query(DATABASE_TABLE, 
				         new String[] { OneNote.KEY_ROWID, OneNote.KEY_TITLE, OneNote.KEY_PATH, 
				                        OneNote.KEY_CREATED, 
				                        OneNote.KEY_NOTIFYTIME, OneNote.KEY_USE_NOTIFYTIME,
				                        OneNote.KEY_DRAWABLE_ID, OneNote.KEY_RINGMUSIC,
				                        OneNote.KEY_NOTIFYDURA, OneNote.KEY_NOTIFYMETHOD,
				                        OneNote.KEY_PWD, OneNote.KEY_NOTETYPE},
				                        Condition, null, null, null, UserOrderBy);
	}
	
	public Cursor GetWidgetData(){
		return mDb.query(DATABASE_TABLE, 
		         new String[] { OneNote.KEY_ROWID, OneNote.KEY_TITLE, 
		                        OneNote.KEY_DRAWABLE_ID,
		                        OneNote.KEY_WIDGETID, OneNote.KEY_PWD, OneNote.KEY_NOTETYPE},
		                        OneNote.KEY_WIDGETID+"!=0", null, null, null, null);
	}
	
	public Cursor GetOneNote(int RowId) throws SQLException {

		Cursor mCursor = mDb.query(true, DATABASE_TABLE, 
				                         new String[] { OneNote.KEY_ROWID, OneNote.KEY_TITLE, OneNote.KEY_PATH, 
                                                        OneNote.KEY_CREATED, OneNote.KEY_NOTIFYTIME, OneNote.KEY_USE_NOTIFYTIME,
                                                        OneNote.KEY_DRAWABLE_ID,
                                                        OneNote.KEY_RINGMUSIC, OneNote.KEY_NOTIFYDURA, OneNote.KEY_NOTIFYMETHOD,
                                                        OneNote.KEY_PWD, OneNote.KEY_RANK, OneNote.KEY_WIDGETID, OneNote.KEY_NOTETYPE}, 
				                         OneNote.KEY_ROWID + "=" + RowId, null, null, null, null, null);
		if( mCursor != null )
			mCursor.moveToFirst();
		
		return mCursor;
	}

	public int GetOneNoteRowId(String Id)throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {OneNote.KEY_ROWID}, 
				                   OneNote.KEY_PATH + "='" + Id+"'", null, null, null, null, null);
		if( mCursor != null )
			mCursor.moveToFirst();
		
		return mCursor.getInt(mCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID));
	}
    
	public Cursor GetNumGroupByClr() throws SQLException 
	{

		Cursor mCursor = mDb.rawQuery("select "+OneNote.KEY_DRAWABLE_ID+", count(*) "+OneNote.KEY_NUM+" from "+ DATABASE_TABLE+ 
				                      " group by "+OneNote.KEY_DRAWABLE_ID, null);
		

		return mCursor;
	}
 
    public boolean UpdateOneNote(OneNote Note) {
		ContentValues UpdateArgs = new ContentValues();
		UpdateArgs.put(OneNote.KEY_TITLE, Note.NoteTitle);
		UpdateArgs.put(OneNote.KEY_PATH, Note.NoteFilePath);
		UpdateArgs.put(OneNote.KEY_UPDATED, HelperFunctions.Calendar2String(Calendar.getInstance()));
		UpdateArgs.put(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(Note.NotifyTime));
		UpdateArgs.put(OneNote.KEY_USE_NOTIFYTIME, Note.Use_NotifyTime);
		UpdateArgs.put(OneNote.KEY_DRAWABLE_ID, Note.DrawableResIdx);
		UpdateArgs.put(OneNote.KEY_NOTIFYDURA, Note.NotifyDura);
		UpdateArgs.put(OneNote.KEY_RINGMUSIC, Note.RingMusic);
		UpdateArgs.put(OneNote.KEY_NOTIFYMETHOD, Note.NotifyMethod);
		

		return mDb.update(DATABASE_TABLE, UpdateArgs, OneNote.KEY_ROWID + "=" + Note.NoteRowId, null) > 0;
	}
	
	public boolean UpdateNoteTagClr(int RowId, int Idx){
		ContentValues args = new ContentValues();
		args.put(OneNote.KEY_DRAWABLE_ID, Idx);
		
		return mDb.update(DATABASE_TABLE, args, OneNote.KEY_ROWID + "=" + RowId, null) > 0;
	}
	
	public boolean UpdateNoteNotifyRingTime(int RowId, Calendar NotifyTime){

		ContentValues Content = new ContentValues();
		Content.put(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(NotifyTime));
		return mDb.update(DATABASE_TABLE, Content, OneNote.KEY_ROWID + "=" + RowId, null) > 0;
	}
	
	public boolean StopNoteNotify(int RowId){
		ContentValues Content = new ContentValues();
		Content.put(OneNote.KEY_USE_NOTIFYTIME, ProjectConst.No);
		
		return mDb.update(DATABASE_TABLE, Content, OneNote.KEY_ROWID + "=" + RowId, null) > 0;
	}
	
	public boolean SetNotePwd(int RowId, String NewPwd){
		ContentValues Content = new ContentValues();
		Content.put(OneNote.KEY_PWD, NewPwd);
		
		return mDb.update(DATABASE_TABLE, Content, OneNote.KEY_ROWID + "=" + RowId, null) > 0;
	}
	
	public boolean SetNoteRank(int RowId, int Rank){
		ContentValues Content = new ContentValues();
		Content.put(OneNote.KEY_RANK, Rank);
		
		return mDb.update(DATABASE_TABLE, Content, OneNote.KEY_ROWID + "=" + RowId, null) > 0;
	}
	
	public boolean SetNoteWidgetID(int RowId, int Id){
		ContentValues Content = new ContentValues();
		Content.put(OneNote.KEY_WIDGETID, Id);
		
		return mDb.update(DATABASE_TABLE, Content, OneNote.KEY_ROWID + "=" + RowId, null) > 0;
	}
	
	public boolean SetNotePos(int RowId, int X, int Y)
	{
		ContentValues Content = new ContentValues();
		Content.put(OneNote.KEY_LEFTX, X);
		Content.put(OneNote.KEY_LEFTY, Y);
		
		return mDb.update(DATABASE_TABLE, Content, OneNote.KEY_ROWID + "=" + RowId, null) > 0;
	}
	
	public boolean ClearNoteWidgetID(int WidgetId){
		ContentValues Content = new ContentValues();
		Content.put(OneNote.KEY_WIDGETID, ProjectConst.Zero);
		
		return mDb.update(DATABASE_TABLE, Content, OneNote.KEY_WIDGETID + "=" + WidgetId, null) > 0;
	}
	public static void SetOrderBy(String Condition){
		OrderBy = Condition;
	}
}