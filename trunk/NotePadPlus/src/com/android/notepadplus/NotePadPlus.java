package com.android.notepadplus;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


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
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;

public class NotePadPlus extends Activity {

	/** Action */
	public static final String Action_Edit_Note = "com.android.notepadplus.EditNote";
	/** Broadcast action */
	public static final String BROADCAST_REFRESHLIST = "com.android.notepadplus.refreshlist";
	public static final String BROADCAST_RANKNOTE = "com.android.notepadplus.ranknote";
	
	/** Dialog id */
	private static final int Del_Prompt_Dlg = 1;
	private static final int About_Dlg = Del_Prompt_Dlg+1;
	private static final int NoteHasLock_Dlg = About_Dlg+1;
	private static final int PwdErr_Dlg = NoteHasLock_Dlg+1;
	private static final int EidtNote_PwdPrompt_Dlg = PwdErr_Dlg+1;
	private static final int DelNote_PwdPrompt_Dlg = EidtNote_PwdPrompt_Dlg+1;
	private static final int ViewStyle_Dlg = DelNote_PwdPrompt_Dlg+1;
	private static final int SetTag_PwdPrompt_Dlg = ViewStyle_Dlg+1;
	private static final int OrderBySel_Dlg = SetTag_PwdPrompt_Dlg+1;

	/** Action id for activity redirection */
	public static final int ACTIVITY_CREATE = 0;
	public static final int ACTIVITY_EDIT = 1;
	public static final int ACTIVITY_SET_TAGCLR = 2;
	public static final int ACTIVITY_SET_PWD = 3;
	public static final int ACTIVITY_CHG_PWD = 4;
	public static final int ACTIVITY_CLR_PWD = 5;
	public static final int ACTIVITY_ENTERPWD_EDIT = 6;
	public static final int ACTIVITY_SETTING = ACTIVITY_ENTERPWD_EDIT+1;
	public static final int ACTIVITY_FILTER = ACTIVITY_SETTING+1;

	/** Long touch Menu id */
	public static final int ITEM0 = Menu.FIRST;
	public static final int ITEM1 = Menu.FIRST + 1;
	public static final int ITEM2 = Menu.FIRST + 2;
	public static final int ITEM3 = Menu.FIRST + 3;
	public static final int ITEM4 = Menu.FIRST + 4;
	public static final int ITEM5 = Menu.FIRST + 5;

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
	private ListView NoteList = null;
	private LayoutAnimationController ListAnimController = null;
	private GridView NoteGrid = null;
	private LinearLayout Main = null;
	private int EdgeWidth = 0;
	private static final int ScreenPadding = 15;
	private static final int SpacePadding = 10;
	public  static int ScreenWidth;
	public  static int ScreenHeight;

	// Application settings
	public static AppSetting SysSettings;

	/** Note's index */
	private static final int ErrNoteIndex = -1;
	private int NoteIndex = ErrNoteIndex;

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
			   int Index = intent.getIntExtra(OneNote.KEY_INDEX, ErrNoteIndex);
			   int IsCheck = intent.getBooleanExtra(OneNote.KEY_RANK, false)?ProjectConst.One:ProjectConst.Zero;
			   if( Index != ErrNoteIndex )
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
		NoteDbAdapter.SetOrderBy(NoteDbAdapter.OrderByArray[Integer.parseInt(SysSettings.OrderBy)]);
		
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
				   menu.setHeaderTitle("��־�༭�˵�");
				   menu.add(0, ITEM0, 1, "�༭��ǰ��־");
				   menu.add(0, ITEM1, 2, "ɾ����ǰ��־");
				   menu.add(0, ITEM2, 3, "��������־");
				   menu.add(0, ITEM3, 4, "�޸���־��ǩ��ɫ");
				   menu.add(0, ITEM4, 5, "������ǰ��־");
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
			       menu.setHeaderTitle("��־�༭�˵�");
				   menu.add(0, ITEM0, 1, "�༭��ǰ��־");
				   menu.add(0, ITEM1, 2, "ɾ����ǰ��־");
				   menu.add(0, ITEM2, 3, "��������־");
				   menu.add(0, ITEM3, 4, "�޸���־��ǩ��ɫ");
				   menu.add(0, ITEM4, 5, "������ǰ��־");
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
		registerReceiver(RefreshReceiver, new IntentFilter(BROADCAST_REFRESHLIST));
		registerReceiver(RankReceiver, new IntentFilter(BROADCAST_RANKNOTE));
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

	private void RefreshListViewByTagClr(int TagId) {
		// Data
		String Condition = OneNote.KEY_TAGIMG_ID+"="+Integer.toString(TagId);
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
			int TagImgIdx = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_TAGIMG_ID));
			int BgClrIdx = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_BGCLR));
			int Rank = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_RANK));
            // Create one item
			HashMap<String, Object> OneNote = new HashMap<String, Object>();
			// Set list item's data
			OneNote.put("NoteTitle", Title);
			OneNote.put("Time", Time);
			Notes.add(OneNote);
			// Set up parameters
			BgColor[i] = ItemBgClr[BgClrIdx];
			TagColor[i] = TagClr[TagImgIdx];
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
			int ClrIdx = NotesCursor.getInt(NotesCursor.getColumnIndexOrThrow(OneNote.KEY_BGCLR));

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

	/** Menu click callback */
	@Override
	public boolean onContextItemSelected(MenuItem Item) {
		AdapterView.AdapterContextMenuInfo Info = (AdapterView.AdapterContextMenuInfo) Item
				.getMenuInfo();
		NoteIndex = (int) Info.id;
		switch (Item.getItemId()) {
		case ITEM0:
			 OnClickEditNote(NoteIndex);
			 return true;
		case ITEM1:
			 showDialog(Del_Prompt_Dlg);
			 return true;
		case ITEM2:
			 actionClickAddNote();
			 return true;
		case ITEM3: 
			 OnClickSetTag(NoteIndex);
			 return true;
		
		case ITEM4: {
			Cursor TmpCursor = NotesCursor;
			TmpCursor.moveToPosition(NoteIndex);
			String Pwd = TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD));

			if( Pwd.length() == 0 ) {
			    Intent PwdDlgIntent = new Intent(NotePadPlus.this, PwdDlgActivity.class);
			    PwdDlgIntent.putExtra(OneNote.KEY_ROWID, TmpCursor.getInt(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID)));
			    startActivityForResult(PwdDlgIntent, ACTIVITY_SET_PWD);
            } else
			    showDialog(NoteHasLock_Dlg);
			return true;
		}

		}
		return super.onContextItemSelected(Item);
	}

	private Menu MainMenu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add(Menu.NONE, ITEM0, 1, "����").setIcon(android.R.drawable.ic_menu_add);
		menu.add(Menu.NONE, ITEM1, 2, "����").setIcon(AppSetting.OrderByIcon[Integer.parseInt(SysSettings.OrderBy)]);
		menu.add(Menu.NONE, ITEM2, 3, "����").setIcon(android.R.drawable.ic_menu_manage);
		menu.add(Menu.NONE, ITEM3, 4, "��ͼ").setIcon(android.R.drawable.ic_menu_sort_by_size);
		menu.add(Menu.NONE, ITEM4, 5, "��ɫ").setIcon(android.R.drawable.ic_menu_view);
		menu.add(Menu.NONE, ITEM5, 6, "����").setIcon(android.R.drawable.ic_menu_info_details);
		// Hold the menu handler
		MainMenu = menu;
        return true;
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {  
           switch(item.getItemId()) 
           {  
              case ITEM0:
	               actionClickAddNote();
	               break;
              case ITEM1:
            	   showDialog(OrderBySel_Dlg);
	               break;
              case ITEM2:
            	   Intent PrefSetting = new Intent(NotePadPlus.this, SysSettingActivity.class);
            	   startActivityForResult(PrefSetting, ACTIVITY_SETTING);
	               break;
              case ITEM3:
	               showDialog(ViewStyle_Dlg);
	               break;
              case ITEM4:
            	   Intent FilterNote = new Intent(this, FilterNoteByTagActivity.class);
            	   startActivityForResult(FilterNote, ACTIVITY_FILTER);
	               break;
              case ITEM5:
	               showDialog(About_Dlg);
	               break;
           }
           return false;
	}
	
	/** Action for menu item */
	private void actionClickAddNote() {

		Intent i = new Intent(this, AddNoteActivity.class);
		startActivityForResult(i, ACTIVITY_CREATE);
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
                showDialog(EidtNote_PwdPrompt_Dlg);
			}else
				EditNoteHelper(TmpCursor);
		}	
    }
		
	private void EditNoteHelper(Cursor Note)
	{
			Intent OneNoteData = new Intent(NotePadPlus.this, EditNoteActivity.class);
			OneNoteData.putExtra(EditNoteActivity.KEY_SOURCE, Action_Edit_Note);
			OneNoteData.putExtra(OneNote.KEY_ROWID, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_ROWID)));
			OneNoteData.putExtra(OneNote.KEY_TITLE, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_TITLE)));
			OneNoteData.putExtra(OneNote.KEY_PATH, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PATH)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYTIME, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYTIME)));
			OneNoteData.putExtra(OneNote.KEY_USE_NOTIFYTIME, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_USE_NOTIFYTIME)));
			OneNoteData.putExtra(OneNote.KEY_TAGIMG_ID, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_TAGIMG_ID)));
			OneNoteData.putExtra(OneNote.KEY_BGCLR, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_BGCLR)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYDURA, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYDURA)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYMETHOD, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_NOTIFYMETHOD)));
			OneNoteData.putExtra(OneNote.KEY_RINGMUSIC, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_RINGMUSIC)));
		    OneNoteData.putExtra(OneNote.KEY_WIDGETID, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_WIDGETID)));
		    OneNoteData.putExtra(OneNote.KEY_PWD, Note.getString(Note.getColumnIndexOrThrow(OneNote.KEY_PWD)));

			startActivityForResult(OneNoteData, ACTIVITY_EDIT);
	}

	private void OnClickDelNote(int Pos) {
		// Get path
		Cursor TmpCursor = NotesCursor;
		TmpCursor.moveToPosition(Pos);
		// Check password
		if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).length() > 0 )
            showDialog(DelNote_PwdPrompt_Dlg);
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
            showDialog(DelNote_PwdPrompt_Dlg);
		else
			SetTagHelper();
	}
	
	private void SetTagHelper(){
		Intent intent = new Intent();
		intent.setClass(NotePadPlus.this, SetTagClrActivity.class);
		startActivityForResult(intent, ACTIVITY_SET_TAGCLR);
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
    				      showDialog(PwdErr_Dlg);	
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
    				      showDialog(PwdErr_Dlg);
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
    				      showDialog(PwdErr_Dlg);
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
	
	// 
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
						   startActivityForResult(ChgPwdDlgIntent, ACTIVITY_CHG_PWD);
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
						   startActivityForResult(ClrPwdDlgIntent, ACTIVITY_CLR_PWD);
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
        BaseAdapter adapter = new ListItemAdapter(new int[]{R.drawable.ic_menu_listview, R.drawable.ic_menu_gridview},Items);  
        DialogInterface.OnClickListener listener =   
            new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialogInterface, int which) {  
                	        SysSettings.ViewStyle = AppSetting.VieweStyleVal[which];
                    		RefreshListView();
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
        BaseAdapter adapter = new ListItemAdapter(AppSetting.OrderByIcon, Items);  
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
		case Del_Prompt_Dlg:
			 return BuildDelPromptDialog(NotePadPlus.this, R.string.delnote_title);
		case About_Dlg:
			 return HelperFunctions.BuildAltertDialog(NotePadPlus.this, R.string.about_title, R.string.about_content);
		case NoteHasLock_Dlg:
			 return BuildNoteHasLockDialog(NotePadPlus.this, R.string.note_lock_dlg_title, R.string.notehaslock_msg);
		case DelNote_PwdPrompt_Dlg:
			 return BuildDelNotePromptPwdDlg(NotePadPlus.this, R.string.pwdprompt_title, R.string.pwdprompt_tip);
		case EidtNote_PwdPrompt_Dlg:
			 return BuildEditNotePromptPwdDlg(NotePadPlus.this, R.string.pwdprompt_title, R.string.pwdprompt_tip);
		case SetTag_PwdPrompt_Dlg:
			 return BuildSetTagPromptPwdDlg(NotePadPlus.this, R.string.pwdprompt_title, R.string.pwdprompt_tip);
		case PwdErr_Dlg:
			 return HelperFunctions.BuildAltertDialog(NotePadPlus.this, R.string.pwderr_title, R.string.orignalpwd_err_prompt);
		case ViewStyle_Dlg:
			 return BuildSelViewDlg(NotePadPlus.this, R.string.viewstyle_title, R.array.noteviewstyle);
		case OrderBySel_Dlg:
			 return BuildOrderByDlg(NotePadPlus.this, R.string.orderby_title, R.array.orderby);
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == ACTIVITY_SET_TAGCLR) {
			if (resultCode == RESULT_OK) {
				Bundle SelIdxData = intent.getExtras();
				int TagImgIdx = SelIdxData.getInt(OneNote.KEY_TAGIMG_ID);
				int BgClrIdx = TagImgIdx;
				Cursor TmpCursor = NotesCursor;
				TmpCursor.moveToPosition(NoteIndex);
				NotesDb.UpdateNoteTagClr(TmpCursor.getInt(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_ROWID)), TagImgIdx, BgClrIdx);
				RefreshListView();	
			}
		}else if( requestCode == ACTIVITY_SET_PWD ||  requestCode == ACTIVITY_CLR_PWD ) {
			if( resultCode == RESULT_OK )
				RefreshListView();
		}else if( requestCode == ACTIVITY_CHG_PWD && resultCode == RESULT_OK ) // Needn't refresh
			NotesCursor = NotesDb.GetAllNotes();
		else if( requestCode == ACTIVITY_SETTING && resultCode == RESULT_OK ) {
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
		} else if( requestCode == ACTIVITY_FILTER && resultCode == RESULT_OK ) {
			Bundle SelIdxData = intent.getExtras();
			int TagImgIdx = SelIdxData.getInt(OneNote.KEY_TAGIMG_ID);
			RefreshListViewByTagClr(TagImgIdx);
		}
		
		if( requestCode == ACTIVITY_EDIT || requestCode == ACTIVITY_CREATE )
			if( resultCode == RESULT_OK)
			{	
		        RefreshListView();
		        HelperFunctions.RefreshWidgetNoteList(NotePadPlus.this, NotesCursor);
			}
	}
	
	class ListItemAdapter extends BaseAdapter {  
	      
		  private int[] ImgIds;
		  private int Items;
		  ListItemAdapter(int[] Img, int ItemRes){
			     ImgIds = Img;
			     Items = ItemRes;
		  }
	      @Override  
	      public int getCount() {  
	             return ImgIds.length;  
	      }  
	  
	      @Override  
	      public Object getItem(int position) {  
	            return null;  
	      }  
	  
	      @Override  
	      public long getItemId(int position) {  
	            return 0;  
	      }  
	  
	      @Override  
	      public View getView(int position, View contentView, ViewGroup parent) {  
	            TextView textView = new TextView(NotePadPlus.this);  
	            //���array.xml�е�������ԴgetStringArray���ص���һ��String����  
	            String text = getResources().getStringArray(Items)[position];  
	            textView.setText(text);  
	            //���������С  
	            textView.setTextSize(24);  
	            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(  
	                    LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);  
	            textView.setLayoutParams(layoutParams);  
	            //����ˮƽ�����Ͼ���  
	            textView.setGravity(android.view.Gravity.CENTER_VERTICAL);  
	            textView.setMinHeight(65);  
	            //����������ɫ  
	            textView.setTextColor(Color.BLACK);    
	            //����ͼ�������ֵ����  
	            textView.setCompoundDrawablesWithIntrinsicBounds(ImgIds[position], 0, 0, 0);  
	            //����textView���������µ�padding��С  
	            textView.setPadding(15, 0, 15, 0);  
	            //�������ֺ�ͼ��֮���padding��С  
	            textView.setCompoundDrawablePadding(15); 
	            return textView;  
	      }	          
	}  
}