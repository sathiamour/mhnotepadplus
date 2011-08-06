package com.android.notepadplus;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.android.notepadplus.NoteUserDefViewActivity.MyOnTouchListener;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;

public class NotePadPlus extends Activity {

	/** Database helper */
	private NoteDbAdapter NotesDb;
	private Cursor NotesCursor;

	/** Note tag color */
	public static final int[] TagClr = {0xffd51a1a, 0xffffcc00, 0xff0024ff, 0xff0cd206, 0xffb806d2, 0xff848284, 0xfff9fba9, 0xff1c1c1c, 0xff473a09}; 
		    //0xff6596cf, 0xfff58f7f, 0xffbaa131, 0xff4fb248, 0xffa052a0, 0xfff15b22, 0xff90a8d7, 0xffe28f25,	0xffdb2636};
	public static final int[] ItemBgClr = {0xfffde1e1, 0xfff9f983, 0xffaceefb, 0xffb9fba9, 0xfff895fb, 0xffe2e1e1, 0xffffffff, 0xff929191, 0xff999173};
		    //0xffb3daf4, 0xfff9bebe, 0xffdcde4c,	0xff96c93d, 0xff9a6daf, 0xfff78e1e, 0xffd6d6ec, 0xffe1e31a, 0xffc94730};
	public static final int ClrNum = ItemBgClr.length;

	/** Views */
	private ListView NoteList;
	private LayoutAnimationController ListAnimController;
	private GridView NoteGrid;
	private LinearLayout Main;
	private MyRelativeLayout MainLayout;
	private ArrayList<NotePos> Postions;
	
	private int EdgeWidth = 0;
	private static final int ScreenPadding = 15;
	private static final int SpacePadding = 10;
	public  static int ScreenWidth;
	public  static int ScreenHeight;

	// Application settings
	public static AppSetting SysSettings;

	/** Note's index */
	private int NoteIndex = ProjectConst.NegativeOne;

	/** Refresh receiver when end date check finish */
	public BroadcastReceiver RefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			   // Refresh list view on GUI
			   RefreshListView();
		}
	};

	/** Refresh receiver when end date check finish */
	public BroadcastReceiver RankReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			   int Index = intent.getIntExtra(OneNote.KEY_INDEX, ProjectConst.NegativeOne);
			   int IsCheck = intent.getBooleanExtra(OneNote.KEY_RANK, false)?ProjectConst.One:ProjectConst.Zero;
			   if( Index != ProjectConst.NegativeOne )
			   {
				   Cursor TmpCursor = NotesCursor;
				   TmpCursor.moveToPosition(Index);
				   NotesDb.SetNoteRank(TmpCursor.getInt(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID)), IsCheck);
				   // Refresh list view on GUI, if needed
				   if( SysSettings.OrderBy.equals(AppSetting.OrderByRank) )
				       RefreshListView();
			   }
		}
	};

	private ProgressDialog ProgressDlg;  
	boolean NotesIsLoaded = false;
	private Thread LoadNotesProgThread = new Thread(){  
        @Override  
        public void run() {    
            // TODO Auto-generated method stub  
            while (!NotesIsLoaded) {  
                try {  
                    sleep(100);  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                }  
            }  
            // Close progress dialog
            ProgressDlg.dismiss();  
        }       
    };  
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Application setting
		SysSettings = new AppSetting(this);
		
		// Note dislpay view
		NoteList = (ListView) findViewById(R.id.nodelist);
		NoteGrid = (GridView) findViewById(R.id.notegrid);
		Main = (LinearLayout)findViewById(R.id.mainview);
		Main.setBackgroundColor(SysSettings.BgClr);

		// Set notes load progress dialog
		ProgressDlg = new ProgressDialog(this);  
		ProgressDlg.setIcon(R.drawable.icon);  
		ProgressDlg.setTitle(R.string.progress_load_title);
		ProgressDlg.setMessage(getString(R.string.progress_load_msg));  
		ProgressDlg.setCancelable(false);  
		ProgressDlg.show(); 
        
        LoadNotesProgThread.start();  


		// Note database
		NotesDb = new NoteDbAdapter(this);

		NotesDb.open();
		
		//NoteDbAdapter.SetOrderBy(NoteDbAdapter.OrderByArray[Integer.parseInt(SysSettings.OrderBy)]);

		// Get screen's widht & height
		DisplayMetrics ScreenMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);
		ScreenHeight = ScreenMetrics.heightPixels;
		ScreenWidth = ScreenMetrics.widthPixels;
		

		// Listview Add listener
		// List item short click listener
		NoteList.setOnItemClickListener(new OnItemClickListener() {
			/** When user clicks a note, show edit activity */
			public void onItemClick(AdapterView<?> Adapater, View ListView, int position, long id) {
				   NoteIndex = position;
				   OnClickEditNote(position);
			}
		});
		// List item long click listener
		NoteList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			/** When user push the item long time, show menu */
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				   menu.setHeaderTitle("日志编辑菜单");
				   menu.add(0, ProjectConst.ITEM0, 1, "编辑当前日志");
				   menu.add(0, ProjectConst.ITEM1, 2, "删除当前日志");
				   menu.add(0, ProjectConst.ITEM2, 3, "增加新日志");
				   menu.add(0, ProjectConst.ITEM3, 4, "修改日志标签颜色");
				   menu.add(0, ProjectConst.ITEM4, 5, "锁定当前日志");
			}
		});

		// GridView
		EdgeWidth = (Math.min(ScreenHeight, ScreenWidth) - ScreenPadding * 2 - SpacePadding * 2) / 3;
		NoteGrid.setPadding(ScreenPadding, ScreenPadding, ScreenPadding, ScreenPadding);
		NoteGrid.setColumnWidth(EdgeWidth);
		NoteGrid.setHorizontalSpacing(SpacePadding);
		NoteGrid.setVerticalSpacing(SpacePadding);
		// Add Listeners
		NoteGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				   NoteIndex = position;
				   OnClickEditNote(position);
			}
		});
		NoteGrid.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			/** When user push the item long time, show menu */
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			       menu.setHeaderTitle("日志编辑菜单");
				   menu.add(0, ProjectConst.ITEM0, 1, "编辑当前日志");
				   menu.add(0, ProjectConst.ITEM1, 2, "删除当前日志");
				   menu.add(0, ProjectConst.ITEM2, 3, "增加新日志");
				   menu.add(0, ProjectConst.ITEM3, 4, "修改日志标签颜色");
				   menu.add(0, ProjectConst.ITEM4, 5, "锁定当前日志");
			}
		});
        
		// Refresh the note list
		RefreshListView();

		// Display animation
		AnimationSet set = new AnimationSet(true);
        set.setInterpolator(new AccelerateInterpolator());
		
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(60);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(60);
		set.addAnimation(animation);

		ListAnimController = new LayoutAnimationController(set, 2.5f);
		ListAnimController.setOrder(LayoutAnimationController.ORDER_NORMAL);
		NoteList.setLayoutAnimation(ListAnimController);
		
		// Notes loading is over
		NotesIsLoaded = true;  
		
		
	}

	/** When the activity is destroyed, close database */
	@Override
	protected void onDestroy() {
		// Close database
		if (NotesDb != null)
			NotesDb.close();
		// Save application settings
		SysSettings.SaveSetting();
		// Destroy
		super.onDestroy();

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(RefreshReceiver);
		unregisterReceiver(RankReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(RefreshReceiver, new IntentFilter(ProjectConst.BROADCAST_REFRESHLIST_ACTION));
		registerReceiver(RankReceiver, new IntentFilter(ProjectConst.BROADCAST_RANKNOTE_ACTION));
	}

	/** Refresh note list from database */
	/**
	 * BE CAREFULLY, we use SimpleAdapter so Pos == id, but it is not the same
	 * with CusorAdapter
	 */
	private void RefreshListView() {
		// Data
		NotesCursor = NotesDb.GetAllNotes();
		int Count = NotesCursor.getCount();
		// Refresh the notes list
		if (Count > 0) {
			// Set view's visible
			
			if (SysSettings.IsListView()) {
				// List view
				setContentView(null);
				setContentView(R.layout.main);
				NoteGrid.setVisibility(View.GONE);
				findViewById(R.id.nonotetip).setVisibility(View.GONE);
				NoteList.setVisibility(View.VISIBLE);
				ShowNoteInListView(Count);   
			} else if( SysSettings.IsGridView() ){
				// Grid view
				setContentView(R.layout.main);
				NoteList.setVisibility(View.GONE);
				findViewById(R.id.nonotetip).setVisibility(View.GONE);
				NoteGrid.setVisibility(View.VISIBLE);
				ShowNoteInGridView(Count);
			} else { 
				Log.d(ProjectConst.TAG," in user def");
				// User definition mode
				NoteGrid.setVisibility(View.GONE);
				NoteList.setVisibility(View.GONE);
				ShowNoteInUserDefView();	
			}
		} else {
			setContentView(R.layout.main);
			findViewById(R.id.nonotetip).setVisibility(View.VISIBLE);
			NoteList.setVisibility(View.GONE);
			NoteGrid.setVisibility(View.GONE);
		}
	}

	private void RefreshListViewByTagClr(int TagId) {
		// Data
		String Condition = OneNote.KEY_DRAWABLE_ID+"="+Integer.toString(TagId);
		NotesCursor = NotesDb.GetNotesByCondition(Condition);
		int Count = NotesCursor.getCount();
		// Refresh the notes list
		if (Count > 0) {
			// Set view's visible
			findViewById(R.id.nonotetip).setVisibility(View.GONE);
			if (SysSettings.IsListView()) {
				NoteGrid.setVisibility(View.GONE);
				NoteList.setVisibility(View.VISIBLE);
				ShowNoteInListView(Count);
			} else {
				NoteList.setVisibility(View.GONE);
				NoteGrid.setVisibility(View.VISIBLE);
				ShowNoteInGridView(Count);
			}
		} else {
			findViewById(R.id.nonotetip).setVisibility(View.VISIBLE);
			NoteList.setVisibility(View.GONE);
			NoteGrid.setVisibility(View.GONE);
		}
	}
	// Show notes in listview
	private void ShowNoteInListView(int Count) {
		NoteList.setAdapter(null);
		ArrayList<HashMap<String, Object>> Notes = new ArrayList<HashMap<String, Object>>();
		int[] BgColor = new int[Count];
		int[] TagColor = new int[Count];
		boolean[] IsLock = new boolean[Count];
		boolean[] IsNotify = new boolean[Count];
		boolean[] IsRank = new boolean[Count];
		NoteItemAdapter ListItemAdapter = new NoteItemAdapter(this, Notes,
                R.layout.listitem, new String[] { "NoteTitle", "Time" }, 
                new int[] { R.id.NoteTitle, R.id.NoteCreatedTime }, BgColor, TagColor, IsLock, IsNotify, IsRank);

        NoteList.setAdapter(ListItemAdapter);
		NoteList.setLayoutAnimation(ListAnimController);
		
		for (int i = 0; i < Count && NotesCursor.moveToNext(); ++i) {
			// Get note's parameter
			String Title = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
			String Time = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_CREATED));
			String Pwd = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_PWD));
			String Use_NotifyTime = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_USE_NOTIFYTIME));
			Calendar NotifyTime = HelperFunctions.String2Calenar(NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_NOTIFYTIME)));
			int Idx = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_DRAWABLE_ID));
			int Rank = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_RANK));
            // Create one item
			HashMap<String, Object> OneNote = new HashMap<String, Object>();
			// Set list item's data
			OneNote.put("NoteTitle", Title);
			OneNote.put("Time", Time);
			Notes.add(OneNote);
			// Set up parameters
			BgColor[i] = ItemBgClr[Idx];
			TagColor[i] = TagClr[Idx];
			IsLock[i] = Pwd.length() > 0 ;
			IsNotify[i] = (Use_NotifyTime.equals(ProjectConst.Yes) && HelperFunctions.CmpDatePrefix2(NotifyTime, Calendar.getInstance()) > 0);
			IsRank[i] = (Rank == 0) ? false: true;
			ListItemAdapter.notifyDataSetChanged();   
		}
	}

	public void ShowNoteInGridView(int Count) {
		ArrayList<HashMap<String, Object>> GridNotes = new ArrayList<HashMap<String, Object>>();
		int[] ItemColor = new int[Count];
		for (int i = 0; i < Count && NotesCursor.moveToNext(); ++i) {
			String Title = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
			int ClrIdx = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_DRAWABLE_ID));

			HashMap<String, Object> OneNote = new HashMap<String, Object>();
			// Item
			OneNote.put("NoteTitle", Title);
			GridNotes.add(OneNote);
			// Set up colors
			ItemColor[i] = ItemBgClr[ClrIdx];
		}

		GridNoteItemAdapter GridNoteAdapter = new GridNoteItemAdapter(this,
				GridNotes, R.layout.gridnoteitem, new String[] { "NoteTitle" },
				new int[] { R.id.GridNoteBody }, ItemColor, EdgeWidth);

		NoteGrid.setAdapter(GridNoteAdapter);
	}

	public void ShowNoteInUserDefView() {
		// Main layout
		MainLayout = new MyRelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);  
        MainLayout.setLayoutParams(layoutParams);
        MainLayout.setBackgroundColor(NotePadPlus.SysSettings.BgClr);
        
        LayoutInflater factory = LayoutInflater.from(this);
        int Count = NotesCursor.getCount();
        Postions = new ArrayList<NotePos>();
        for( int i = 0; i < Count && NotesCursor.moveToNext(); ++i )
    	{
       	     String Title =  NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
      	     int ClrId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_DRAWABLE_ID));
      	     int RowId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID));
      	     int LeftX =  NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_LEFTX));
      	     int LeftY =  NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_LEFTY));
      	     
    		 TextView Text = (TextView)factory.inflate(R.layout.userdefitem, null);
             Text.setBackgroundDrawable(new BitmapDrawable(HelperFunctions.GetAlpha1x1Bg(this, ClrId)));
             Text.setOnTouchListener(new MyOnTouchListener(Text, RowId, i, ScreenWidth, ScreenHeight));
             Text.setText(Title);
             
             Postions.add(new NotePos(RowId, LeftX, LeftY));
             MainLayout.addView(Text);
    	}   
        
        MainLayout.SetChildPos(Postions);
        setContentView(MainLayout);
	}
       
	/** Menu click callback */
	@Override
	public boolean onContextItemSelected(MenuItem Item) {
		AdapterView.AdapterContextMenuInfo Info = (AdapterView.AdapterContextMenuInfo) Item.getMenuInfo();
		NoteIndex = (int) Info.id;
		switch (Item.getItemId()) {
		case ProjectConst.ITEM0:
			 OnClickEditNote(NoteIndex);
			 return true;
		case ProjectConst.ITEM1:
			 showDialog(ProjectConst.DelNote_Prompt_Dlg);
			 return true;
		case ProjectConst.ITEM2:
			 actionClickAddNote();
			 return true;
		case ProjectConst.ITEM3: 
			 OnClickSetTag(NoteIndex);
			 return true;
		
		case ProjectConst.ITEM4: {
			NotesCursor.moveToPosition(NoteIndex);
			String Pwd = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_PWD));

			if( Pwd.length() == 0 ) {
			    Intent PwdDlgIntent = new Intent(NotePadPlus.this, PwdDlgActivity.class);
			    PwdDlgIntent.putExtra(OneNote.KEY_ROWID, NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID)));
			    startActivityForResult(PwdDlgIntent, ProjectConst.ACTIVITY_SET_PWD);
            } else
			    showDialog(ProjectConst.NoteHasLock_Dlg);
			return true;
		}

		}
		return super.onContextItemSelected(Item);
	}

	private Menu MainMenu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add(Menu.NONE, ProjectConst.ITEM0, 1, "添加").setIcon(android.R.drawable.ic_menu_add);
		menu.add(Menu.NONE, ProjectConst.ITEM1, 2, "排序").setIcon(AppSetting.OrderByIcon[Integer.parseInt(SysSettings.OrderBy)]);
		menu.add(Menu.NONE, ProjectConst.ITEM2, 3, "设置").setIcon(android.R.drawable.ic_menu_manage);
		menu.add(Menu.NONE, ProjectConst.ITEM3, 4, "视图").setIcon(android.R.drawable.ic_menu_sort_by_size);
		menu.add(Menu.NONE, ProjectConst.ITEM4, 5, "颜色").setIcon(android.R.drawable.ic_menu_view);
		menu.add(Menu.NONE, ProjectConst.ITEM5, 6, "关于").setIcon(android.R.drawable.ic_menu_info_details);
		// Hold the menu handler
		MainMenu = menu;
        return true;
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {  
           switch(item.getItemId()) 
           {  
              case ProjectConst.ITEM0:
	               actionClickAddNote();
	               break;
              case ProjectConst.ITEM1:
            	   showDialog(ProjectConst.OrderBySel_Dlg);
	               break;
              case ProjectConst.ITEM2:
            	   Intent PrefSetting = new Intent(this, SysSettingActivity.class);
            	   startActivityForResult(PrefSetting, ProjectConst.ACTIVITY_SETTING);
	               break;
              case ProjectConst.ITEM3:
	               showDialog(ProjectConst.ViewStyle_Dlg);
	               break;
              case ProjectConst.ITEM4:
            	   Intent FilterNote = new Intent(this, FilterNoteByTagActivity.class);
            	   startActivityForResult(FilterNote, ProjectConst.ACTIVITY_FILTER);
	               break;
              case ProjectConst.ITEM5:
	               showDialog(ProjectConst.About_Dlg);
	               break;
           }
           return false;
	}
	
	/** Action for menu item */
	private void actionClickAddNote() {

		Intent i = new Intent(this, AddNoteActivity.class);
		startActivityForResult(i, ProjectConst.ACTIVITY_CREATE);
	}

	private void OnClickEditNote(int Pos) {
		if (Pos != -1) {
			Cursor TmpCursor = NotesCursor;
			/**
			 * BE CAREFULLY, we use SimpleAdapter so Pos == id, but it is not
			 * the same with CusorAdapter
			 */
			TmpCursor.moveToPosition(Pos);
			// Check password
			if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).length() > 0 )
			{
				Log.d("log","pwd "+TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)));
                showDialog(ProjectConst.EidtNote_PwdPrompt_Dlg);
			}else
				EditNoteHelper(TmpCursor);
		}	
    }
		
	private void EditNoteHelper(Cursor Note)
	{
			Intent OneNoteData = new Intent(NotePadPlus.this, EditNoteActivity.class);
			OneNoteData.putExtra(EditNoteActivity.KEY_SOURCE, ProjectConst.MAIN_EDIT_ATION);
			OneNoteData.putExtra(OneNote.KEY_ROWID, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_ROWID)));
			OneNoteData.putExtra(OneNote.KEY_TITLE, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_TITLE)));
			OneNoteData.putExtra(OneNote.KEY_PATH, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PATH)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYTIME, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYTIME)));
			OneNoteData.putExtra(OneNote.KEY_USE_NOTIFYTIME, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_USE_NOTIFYTIME)));
			OneNoteData.putExtra(OneNote.KEY_DRAWABLE_ID, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_DRAWABLE_ID)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYDURA, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYDURA)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYMETHOD, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYMETHOD)));
			OneNoteData.putExtra(OneNote.KEY_RINGMUSIC, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_RINGMUSIC)));
		    OneNoteData.putExtra(OneNote.KEY_WIDGETID, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_WIDGETID)));
		    OneNoteData.putExtra(OneNote.KEY_PWD, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PWD)));

			startActivityForResult(OneNoteData, ProjectConst.ACTIVITY_EDIT);
	}

	private void OnClickDelNote(int Pos) {
		// Get path
		Cursor TmpCursor = NotesCursor;
		TmpCursor.moveToPosition(Pos);
		// Check password
		if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).length() > 0 )
            showDialog(ProjectConst.DelNote_PwdPrompt_Dlg);
		else
			DelNoteHelper(TmpCursor);
	}
	
	private void DelNoteHelper(Cursor Note){
		String path = Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PATH));
		int ItemId = Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_ROWID));
		// Delete database record
		NotesDb.DeleteOneNote(ItemId);
		// Refresh list view on GUI
		RefreshListView();
		// Refresh widget view on desktop
		HelperFunctions.RefreshWidgetNoteList(NotePadPlus.this, NotesDb.GetAllNotes());
		// Delete file on local file system(need full path)
		path = getString(R.string.notefile_path_prefix) + path;
		File noteFile = new File(path);
		noteFile.delete();
	};

	private void OnClickSetTag(int Pos) {
		// Get path
		Cursor TmpCursor = NotesCursor;
		TmpCursor.moveToPosition(Pos);
		// Check password
		if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).length() > 0 )
            showDialog(ProjectConst.SetTag_PwdPrompt_Dlg);
		else
			SetTagHelper();
	}
	
	private void SetTagHelper(){
		Intent intent = new Intent();
		intent.setClass(NotePadPlus.this, SetTagClrActivity.class);
		startActivityForResult(intent, ProjectConst.ACTIVITY_SET_TAGCLR);
	}
	
	// Build a dialog to prompt user we want to delete the assigned note
	private Dialog BuildDelPromptDialog(Context AppContext, int Title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(AppContext);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle(Title);
		builder.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Delete the selected note
						OnClickDelNote(NoteIndex);
					}
				});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		return builder.create();
	}
    
	private Dialog BuildEditNotePromptPwdDlg(Context AppContext, int Title, int Msg){
        LayoutInflater factory = LayoutInflater.from(this);
        final View PromptView = factory.inflate(R.layout.promptpwd_dlg, null);
        return new AlertDialog.Builder(NotePadPlus.this)
               .setIcon(R.drawable.alert_dialog_icon)
               .setTitle(Title)
               .setView(PromptView)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
            	      EditText Pwd = (EditText)PromptView.findViewById(R.id.pwd_edit);
            	      Cursor TmpCursor = NotesCursor;
            	      TmpCursor.moveToPosition(NoteIndex);
    			      // Check password
    			      if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).equals(Pwd.getText().toString()) )
    			    	  EditNoteHelper(TmpCursor);
    			      else
    				      showDialog(ProjectConst.PwdErr_Dlg);	
    			      // Clear input
    			      Pwd.setText(ProjectConst.EmptyStr);
              }
              })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
                	  EditText Pwd = (EditText)PromptView.findViewById(R.id.pwd_edit);
                	  // Clear input
    			      Pwd.setText(ProjectConst.EmptyStr);
              }
              })
              .create();
	}
	
	private Dialog BuildDelNotePromptPwdDlg(Context AppContext, int Title, int Msg){
		LayoutInflater factory = LayoutInflater.from(this);
        final View PromptView = factory.inflate(R.layout.promptpwd_dlg, null);
		return new AlertDialog.Builder(NotePadPlus.this)
               .setIcon(R.drawable.alert_dialog_icon)
               .setTitle(Title)
               .setView(PromptView)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
            	      EditText Pwd = (EditText)PromptView.findViewById(R.id.pwd_edit);
            	      Cursor TmpCursor = NotesCursor;
    			      TmpCursor.moveToPosition(NoteIndex);
    			      // Check password
    			      if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).equals(Pwd.getText().toString()) )
    			    	  DelNoteHelper(TmpCursor);
    			      else
    				      showDialog(ProjectConst.PwdErr_Dlg);
    			      // Clear input
    			      Pwd.setText(ProjectConst.EmptyStr);
              }
              })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                	  EditText Pwd = (EditText)PromptView.findViewById(R.id.pwd_edit);
                	  // Clear input
    			      Pwd.setText(ProjectConst.EmptyStr);
              }
              })
              .create();
	}
	
	private Dialog BuildSetTagPromptPwdDlg(Context AppContext, int Title, int Msg){
		LayoutInflater factory = LayoutInflater.from(this);
        final View PromptView = factory.inflate(R.layout.promptpwd_dlg, null);
		return new AlertDialog.Builder(NotePadPlus.this)
               .setIcon(R.drawable.alert_dialog_icon)
               .setTitle(Title)
               .setView(PromptView)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
            	      EditText Pwd = (EditText)PromptView.findViewById(R.id.pwd_edit);
            	      Cursor TmpCursor = NotesCursor;
    			      TmpCursor.moveToPosition(NoteIndex);
    			      // Check password
    			      if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).equals(Pwd.getText().toString()) )
    			    	  SetTagHelper();
    			      else
    				      showDialog(ProjectConst.PwdErr_Dlg);
    			      // Clear input
    			      Pwd.setText(ProjectConst.EmptyStr);
              }
              })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                	  EditText Pwd = (EditText)PromptView.findViewById(R.id.pwd_edit);
                	  // Clear input
    			      Pwd.setText(ProjectConst.EmptyStr);
              }
              })
              .create();
	}
	
	// Note has been locked prompt dialog
	private Dialog BuildNoteHasLockDialog(Context AppContext, int Title, int Msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(AppContext);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle(Title);
		builder.setMessage(Msg);
		builder.setPositiveButton(R.string.changenotepwd_title,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						   Cursor TmpCursor = NotesCursor;
						   TmpCursor.moveToPosition(NoteIndex);
						   Intent ChgPwdDlgIntent = new Intent(NotePadPlus.this, ChgPwdDlgActivity.class);
						   ChgPwdDlgIntent.putExtra(OneNote.KEY_ROWID, TmpCursor.getInt(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID)));
						   startActivityForResult(ChgPwdDlgIntent, ProjectConst.ACTIVITY_CHG_PWD);
					}
				});
		builder.setCancelable(false);
		builder.setNeutralButton(R.string.clearpwd_tip,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						   Cursor TmpCursor = NotesCursor;
						   TmpCursor.moveToPosition(NoteIndex);
						   Intent ClrPwdDlgIntent = new Intent(NotePadPlus.this, ClearPwdDlgActivity.class);
						   ClrPwdDlgIntent.putExtra(OneNote.KEY_ROWID, TmpCursor.getInt(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID)));
						   startActivityForResult(ClrPwdDlgIntent, ProjectConst.ACTIVITY_CLR_PWD);
					}
				});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		return builder.create();
	}
    
	private Dialog BuildSelViewDlg(Context AppContext, int Title, int Items)
	{
        Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(R.drawable.ic_dialog_menu_generic);  
        builder.setTitle(Title);  
        BaseAdapter adapter = new CommonListItemAdapter(new int[]{R.drawable.ic_menu_listview, R.drawable.ic_menu_gridview, R.drawable.ic_menu_userdef},Items, AppContext);  
        DialogInterface.OnClickListener listener =   
            new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialogInterface, int which) {  
                	        //if( which == 2 )
                	        //{
                	        //	Intent NextActivity = new Intent(NotePadPlus.this, NoteUserDefViewActivity.class);
                	        //	startActivityForResult(NextActivity, ProjectConst.ACTIVITY_USERDEF);
                	        //} else {
                	           SysSettings.ViewStyle = AppSetting.VieweStyleVal[which];
                    		   RefreshListView();
                	        //}
                }  
            };  
        builder.setAdapter(adapter, listener);  
        return builder.create();  
	}
	
	private Dialog BuildOrderByDlg(Context AppContext, int Title, int Items)
	{
        Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(R.drawable.ic_dialog_menu_generic);  
        builder.setTitle(Title);  
        BaseAdapter adapter = new CommonListItemAdapter(AppSetting.OrderByIcon, Items, AppContext);  
        DialogInterface.OnClickListener listener =   
            new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialogInterface, int which) { 
                	        MainMenu.getItem(1).setIcon(AppSetting.OrderByIcon[which]);
                	        NoteDbAdapter.SetOrderBy(NoteDbAdapter.OrderByArray[which]);
                	        SysSettings.SetOrderBy(Integer.toString(which));
                    		RefreshListView();
                    		HelperFunctions.RefreshWidgetNoteList(NotePadPlus.this, NotesCursor);
                }  
            };  
        builder.setAdapter(adapter, listener);  
        return builder.create();  
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ProjectConst.DelNote_Prompt_Dlg:
			 return BuildDelPromptDialog(this, R.string.delnote_title);
		case ProjectConst.About_Dlg:
			 return HelperFunctions.BuildAltertDialog(this, R.string.about_title, R.string.about_content);
		case ProjectConst.NoteHasLock_Dlg:
			 return BuildNoteHasLockDialog(this, R.string.note_lock_dlg_title, R.string.notehaslock_msg);
		case ProjectConst.DelNote_PwdPrompt_Dlg:
			 return BuildDelNotePromptPwdDlg(this, R.string.pwdprompt_title, R.string.pwdprompt_tip);
		case ProjectConst.EidtNote_PwdPrompt_Dlg:
			 return BuildEditNotePromptPwdDlg(this, R.string.pwdprompt_title, R.string.pwdprompt_tip);
		case ProjectConst.SetTag_PwdPrompt_Dlg:
			 return BuildSetTagPromptPwdDlg(this, R.string.pwdprompt_title, R.string.pwdprompt_tip);
		case ProjectConst.PwdErr_Dlg:
			 return HelperFunctions.BuildAltertDialog(this, R.string.pwderr_title, R.string.orignalpwd_err_prompt);
		case ProjectConst.ViewStyle_Dlg:
			 return BuildSelViewDlg(this, R.string.viewstyle_title, R.array.noteviewstyle);
		case ProjectConst.OrderBySel_Dlg:
			 return BuildOrderByDlg(this, R.string.orderby_title, R.array.orderby);
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == ProjectConst.ACTIVITY_SET_TAGCLR) {
			if (resultCode == RESULT_OK) {
				Bundle SelIdxData = intent.getExtras();
				int Idx = SelIdxData.getInt(OneNote.KEY_DRAWABLE_ID);
				Cursor TmpCursor = NotesCursor;
				TmpCursor.moveToPosition(NoteIndex);
				NotesDb.UpdateNoteTagClr(TmpCursor.getInt(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID)), Idx);
				RefreshListView();	
			}
		}else if( requestCode == ProjectConst.ACTIVITY_SET_PWD ||  requestCode == ProjectConst.ACTIVITY_CLR_PWD ) {
			if( resultCode == RESULT_OK )
			{
				int WidgetId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_WIDGETID));
				// Refresh 1x1 widget
				if( WidgetId !=  ProjectConst.Zero)
				{
					String Title = NotesCursor.getString(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TITLE));
					int RowId = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID));
					int Idx = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_DRAWABLE_ID));
		    	    HelperFunctions.Refresh1x1Widget(this, WidgetId, Title, RowId, Idx, requestCode == ProjectConst.ACTIVITY_SET_PWD);
				}
				RefreshListView();
			}
		}else if( requestCode == ProjectConst.ACTIVITY_CHG_PWD && resultCode == RESULT_OK ) // Needn't refresh
			NotesCursor = NotesDb.GetAllNotes();
		else if( requestCode == ProjectConst.ACTIVITY_SETTING && resultCode == RESULT_OK ) {
			// Set full screen or not
		    WindowManager.LayoutParams attrs = getWindow().getAttributes();  
			if( SysSettings.IsFullScreen() ) {
	 			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;   
				getWindow().setAttributes(attrs);   
				//getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); 
			} else {
				attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);   
				getWindow().setAttributes(attrs);   
				//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			}
			// Set background color
			Main.setBackgroundColor(SysSettings.BgClr);
			// Set orderby in list view
			NoteDbAdapter.SetOrderBy(NoteDbAdapter.OrderByArray[Integer.parseInt(SysSettings.OrderBy)]);
			MainMenu.getItem(1).setIcon(AppSetting.OrderByIcon[Integer.parseInt(SysSettings.OrderBy)]);
			// Refresh view in app
			RefreshListView();	
			// Refresh widget main board
			HelperFunctions.RefreshWidgetNoteList(NotePadPlus.this, NotesCursor);
		} else if( requestCode == ProjectConst.ACTIVITY_FILTER && resultCode == RESULT_OK ) {
			Bundle SelIdxData = intent.getExtras();
			int Idx = SelIdxData.getInt(OneNote.KEY_DRAWABLE_ID);
			RefreshListViewByTagClr(Idx);
		} else if( requestCode == ProjectConst.ACTIVITY_USERDEF && resultCode == RESULT_OK )
			RefreshListView();
		
		if( requestCode == ProjectConst.ACTIVITY_EDIT || requestCode == ProjectConst.ACTIVITY_CREATE )
			if( resultCode == RESULT_OK)
			{	
		        RefreshListView();
		        HelperFunctions.RefreshWidgetNoteList(NotePadPlus.this, NotesCursor);
			}
	}
	
	class MyOnTouchListener implements OnTouchListener
	{
		private View Holder;
		private int[] temp = new int[] { 0, 0 };
		private int ClickCount;
		private long secClick;
		private long firClick;
		private int RowId;
		private boolean IsMove;
		private int ScreenWidth;
		private int ScreenHeight;
	    private int ViewWidth;
	    private int ViewHeight;
	    private int PosIdx;
	    private int LeftX;
	    private int LeftY;
		MyOnTouchListener(View Parent, int Id, int Index, int Width, int Height)
		{
			   Holder = Parent;
			   ClickCount = ProjectConst.Zero;
			   secClick = ProjectConst.Zero;
			   firClick = ProjectConst.Zero;
			   ViewWidth = ProjectConst.Zero;
			   ViewHeight = ProjectConst.Zero;
			   LeftX = ProjectConst.Zero;
			   LeftY = ProjectConst.Zero;
			   RowId = Id;
			   IsMove = false;
			   ScreenWidth = Width;
			   ScreenHeight = Height;
			   PosIdx = Index;
		}
 
		@Override
		public boolean onTouch(View v, MotionEvent event) {
               int eventaction = event.getAction();

               int x = (int) event.getRawX();
               int y = (int) event.getRawY();
               switch (eventaction) 
               {
                    case MotionEvent.ACTION_DOWN: // touch down so check if the
                         temp[0] = (int) event.getX();
                         temp[1] = y - v.getTop();
                         Holder.bringToFront();
                         ViewWidth = Holder.getMeasuredWidth();
                         ViewHeight = Holder.getMeasuredHeight();
                         
                         ClickCount++;  
            	         if( ClickCount == 1 )  
            	             firClick = System.currentTimeMillis();  
            	         else if ( ClickCount == 2 && !IsMove ){  
            	             secClick = System.currentTimeMillis();  
            	             if( secClick - firClick < 500 )
            	             {  
            	            	 Intent ActivityIntent = new Intent(Holder.getContext(), EditNoteActivity.class);
            	         		 ActivityIntent.putExtra(OneNote.KEY_ROWID, RowId);
            	         		 ActivityIntent.putExtra(OneNote.KEY_INDEX, PosIdx);
            	         		 ActivityIntent.putExtra(EditNoteActivity.KEY_SOURCE, ProjectConst.USERDEF_EDIT_ACTION);
            	         		 startActivityForResult(ActivityIntent, ProjectConst.ACTIVITY_EDIT);
            	             }  
            	             ClickCount = 0;  
            	             firClick = 0;  
            	             secClick = 0;  
            	                  
            	          }  
            	            
                            break;
                     case MotionEvent.ACTION_MOVE: // touch drag with the ball
                    	  IsMove = true; 
                    	  LeftX = x - temp[0];
                    	  LeftY = y - temp[1];
                    	  if( LeftX < 0 ) LeftX = 0;
                    	  if( LeftY < 0 ) LeftY = 0;
                    	  
                    	  int rightX = LeftX + ViewWidth;
                  	      int rightY = LeftY + ViewHeight;
                  	      if( rightX > ScreenWidth ) 
                  	      {
                  	    	  rightX = ScreenWidth;
                  	    	  LeftX = rightX - ViewWidth;
                  	      }
                  	      if( rightY > ScreenHeight ) 
                  	      {
                  	    	  rightY = ScreenHeight;
                  	    	  LeftY = rightY - ViewHeight;
                  	      }
                  	 
                  	      v.layout(LeftX, LeftY, rightX, rightY);
                          v.postInvalidate();
                            
                          break;

                    case MotionEvent.ACTION_UP:
                         IsMove = false;
                         MainLayout.SetChildPos(PosIdx, LeftX, LeftY);
                         break;
               }

               return false;
	   }
		
	}
}