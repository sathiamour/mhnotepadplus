package com.android.notepadplus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnKeyListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NotePadPlus extends Activity {

	/** Action */
	public static final String Action_Edit_Note = "com.android.notepadplus.EditNote";;
	/** Dialog id */
	private static final int Del_Prompt_Dlg = 1;
	private static final int About_Dlg = 2;
	private static final int NoteHasLock_Dlg = 3;
	private static final int PwdErr_Dlg = 4;
	private static final int EidtNote_PwdPrompt_Dlg = 5;
	private static final int DelNote_PwdPrompt_Dlg = 6;

	/** Action id for activity redirection */
	public static final int ACTIVITY_CREATE = 0;
	public static final int ACTIVITY_EDIT = 1;
	public static final int ACTIVITY_SET_TAGCLR = 2;
	public static final int ACTIVITY_SET_PWD = 3;
	public static final int ACTIVITY_CHG_PWD = 4;
	public static final int ACTIVITY_CLR_PWD = 5;
	public static final int ACTIVITY_ENTERPWD_EDIT = 6;

	/** Long touch Menu id */
	public static final int ITEM0 = Menu.FIRST;
	public static final int ITEM1 = Menu.FIRST + 1;
	public static final int ITEM2 = Menu.FIRST + 2;
	public static final int ITEM3 = Menu.FIRST + 3;
	public static final int ITEM4 = Menu.FIRST + 4;

	/** Main menu id */
	public static final int SubMenu_AddNote = 0;
	public static final int SubMenu_SysSet = SubMenu_AddNote + 1;
	public static final int SubMenu_NoteView = SubMenu_SysSet + 1;
	public static final int SubMenu_About = SubMenu_NoteView + 1;

	/** Database helper */
	private NoteDbAdapter NotesDb = null;
	private Cursor NotesCursor;

	/** Broadcast action */
	public static final String BROADCAST_REFRESHLIST = "com.android.notepadplus.refreshlist";

	/** Note tag images */
	public static final int[] TagImages = { R.drawable.pin_note_blue,
			R.drawable.pin_note_pink, R.drawable.pin_note_kelly,
			R.drawable.pin_note_greenyellow, R.drawable.pin_note_purple,
			R.drawable.pin_note_claybank, R.drawable.pin_note_lightblue,
			R.drawable.pin_note_deepyellow, R.drawable.pin_note_red };
	public static final int[] TagClr = { 0xff6596cf, 0xfff58f7f, 0xffbaa131,
			0xff4fb248, 0xffa052a0, 0xfff15b22, 0xff90a8d7, 0xffe28f25,
			0xffdb2636 };
	public static final int[] ItemBgClr = { 0xffb3daf4, 0xfff9bebe, 0xffdcde4c,
			0xff96c93d, 0xff9a6daf, 0xfff78e1e, 0xffd6d6ec, 0xffe1e31a,
			0xffc94730 };
	public static final int ClrNum = ItemBgClr.length;

	/** Views */
	private ListView NoteList = null;
	private LayoutAnimationController ListAnimController = null;
	private GridView NoteGrid = null;
	private int EdgeWidth = 0;
	private static final int ScreenPadding = 15;
	private static final int SpacePadding = 10;
	public  static int ScreenWidth;
	public  static int ScreenHeight;

	// Application settings
	AppSetting AppSettings;

	// User defined menu
	View MainMenuView;
	AlertDialog MenuDlg;
	/** Note's index */
	private static final int ErrNoteIndex = -1;
	private int NoteIndex = ErrNoteIndex;

	/** Refresh receiver when end date check finish */
	public BroadcastReceiver RefreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			// Refresh list view on GUI
			RefreshListView();
			// Refresh widget view on desktop
			// HelperFunctions.RefreshWidgetNoteList(context,NotesDb.GetAllNotes());
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Note database
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();

		// Application setting
		AppSettings = new AppSetting(this);

		// ListView
		NoteList = (ListView) findViewById(R.id.nodelist);

		// Add listener
		// List item short click listener
		NoteList.setOnItemClickListener(new OnItemClickListener() {
			/** When user clicks a note, show edit activity */
			public void onItemClick(AdapterView<?> Adapater, View ListView,
					int position, long id) {
				actionClickEditNote(position);
			}
		});
		// List item long click listener
		NoteList
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
					/** When user push the item long time, show menu */
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.setHeaderTitle("日志编辑菜单");
						menu.add(0, ITEM0, 1, "编辑当前日志");
						menu.add(0, ITEM1, 2, "删除当前日志");
						menu.add(0, ITEM2, 3, "增加新日志");
						menu.add(0, ITEM3, 4, "修改日志标签颜色");
						menu.add(0, ITEM4, 5, "锁定当前日志");

					}
				});

		// Get screen's widht & height
		DisplayMetrics ScreenMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);
		ScreenHeight = ScreenMetrics.heightPixels;
		ScreenWidth = ScreenMetrics.widthPixels;

		// GridView
		int Length = Math.min(ScreenHeight, ScreenWidth);
		EdgeWidth = (Length - ScreenPadding * 2 - SpacePadding * 2) / 3;
		NoteGrid = (GridView) findViewById(R.id.notegrid);
		NoteGrid.setPadding(ScreenPadding, ScreenPadding, ScreenPadding, ScreenPadding);
		NoteGrid.setColumnWidth(EdgeWidth);
		NoteGrid.setHorizontalSpacing(SpacePadding);
		NoteGrid.setVerticalSpacing(SpacePadding);
		// Add Listeners
		NoteGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				actionClickEditNote(position);
			}
		});
		NoteGrid
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
					/** When user push the item long time, show menu */
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.setHeaderTitle("日志编辑菜单");
						menu.add(0, ITEM0, 1, "编辑当前日志");
						menu.add(0, ITEM1, 2, "删除当前日志");
						menu.add(0, ITEM2, 3, "增加新日志");
						menu.add(0, ITEM3, 4, "修改日志标签颜色");
						menu.add(0, ITEM4, 5, "锁定当前日志");

					}
				});

		// Refresh the note list
		RefreshListView();

		// Display animation
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(60);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-2.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(150);
		set.addAnimation(animation);

		ListAnimController = new LayoutAnimationController(set, 0.5f);
		NoteList.setLayoutAnimation(ListAnimController);
	}

	/** When the activity is destroyed, close database */
	@Override
	protected void onDestroy() {
		// Log
		Log.d("log", "NotePadPlus onDestroy");
		// Close database
		if (NotesDb != null)
			NotesDb.close();
		// Save application settings
		AppSettings.SaveSetting();
		// Destroy
		super.onDestroy();

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(RefreshReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(RefreshReceiver, new IntentFilter(
				BROADCAST_REFRESHLIST));
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
			if (AppSettings.IsListView()) {
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
		for (int i = 0; i < Count && NotesCursor.moveToNext(); ++i) {
			String Title = NotesCursor.getString(NotesCursor
					.getColumnIndexOrThrow(OneNote.KEY_TITLE));
			String Time = NotesCursor.getString(NotesCursor
					.getColumnIndexOrThrow(OneNote.KEY_CREATED));
			int TagImgIdx = NotesCursor.getInt(NotesCursor
					.getColumnIndexOrThrow(OneNote.KEY_TAGIMG_ID));
			int BgClrIdx = NotesCursor.getInt(NotesCursor
					.getColumnIndexOrThrow(OneNote.KEY_BGCLR));

			HashMap<String, Object> OneNote = new HashMap<String, Object>();
			// Set list item's data
			// OneNote.put("NoteTag", TagImages[TagImgIdx]);
			OneNote.put("NoteTitle", Title);
			OneNote.put("NoteCreatedTime", Time);
			Notes.add(OneNote);
			// Set up colors
			BgColor[i] = ItemBgClr[BgClrIdx];
			TagColor[i] = TagClr[TagImgIdx];
		}

		NoteItemAdapter ListItemAdapter = new NoteItemAdapter(this, Notes,
				R.layout.listitem, new String[] { "NoteTitle",
						"NoteCreatedTime" }, new int[] { R.id.NoteTitle,
						R.id.NoteCreatedTime }, BgColor, TagColor);

		NoteList.setAdapter(ListItemAdapter);
		NoteList.setLayoutAnimation(ListAnimController);

	}

	public void ShowNoteInGridView(int Count) {
		ArrayList<HashMap<String, Object>> GridNotes = new ArrayList<HashMap<String, Object>>();
		int[] ItemColor = new int[Count];
		for (int i = 0; i < Count && NotesCursor.moveToNext(); ++i) {
			String Title = NotesCursor.getString(NotesCursor
					.getColumnIndexOrThrow(OneNote.KEY_TITLE));
			int TagImgIdx = NotesCursor.getInt(NotesCursor
					.getColumnIndexOrThrow(OneNote.KEY_TAGIMG_ID));

			HashMap<String, Object> OneNote = new HashMap<String, Object>();
			// Item
			OneNote.put("NoteTitle", Title);
			GridNotes.add(OneNote);
			// Set up colors
			ItemColor[i] = TagClr[TagImgIdx];
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
			actionClickEditNote(NoteIndex);
			// Reset selected noted index
			//NoteIndex = ErrNoteIndex;
			return true;
		case ITEM1:
			showDialog(Del_Prompt_Dlg);
			return true;
		case ITEM2:
			actionClickAddNote();
			return true;
		case ITEM3: {
			Intent intent = new Intent();
			intent.setClass(NotePadPlus.this, SetTagClrActivity.class);
			startActivityForResult(intent, ACTIVITY_SET_TAGCLR);
			return true;
		}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add("menu");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	/*
	 * Hook menu opened
	 */
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (MenuDlg == null) {
			MenuDlg = new AlertDialog.Builder(this).create();
			MenuDlg.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_MENU)// 监听按键
						dialog.dismiss();
					return false;
				}
			});
			CreateMenuView();
			MenuDlg.setView(MainMenuView);
			// Set window parameters
			WindowManager.LayoutParams WinAttributes = MenuDlg.getWindow()
					.getAttributes();
			WinAttributes.gravity = Gravity.BOTTOM;
			WinAttributes.y = 0;
			WinAttributes.width = LayoutParams.FILL_PARENT;
			WinAttributes.flags = WinAttributes.flags
					| android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
			MenuDlg.getWindow().setAttributes(WinAttributes);
			// Show window
			MenuDlg.show();
		} else
			MenuDlg.show();

		// If return true, it will show system menu
		return false;
	}

	// @Override
	/** Add menu item */
	// public boolean onCreateOptionsMenu(Menu menu) {
	// super.onCreateOptionsMenu(menu);
	// menu.add(0, ITEM0, 1, "添加日志");
	// menu.add(0, ITEM1, 2, "系统设置");
	// if( AppSettings.ViewStyle.compareTo(AppSetting.DefaultViewStyle) == 0 )
	// menu.add(0, ITEM2, 3, "栅格视图");
	// else
	// menu.add(0, ITEM2, 3, "列表视图");
	// menu.add(0, ITEM3, 4, "关于");
	//	
	// return true;
	// }
	/** Action for menu item */
	private void actionClickAddNote() {

		Intent i = new Intent(this, AddNoteActivity.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	private void actionClickEditNote(int Pos) {
		if (Pos != -1) {
			Cursor TmpCursor = NotesCursor;
			/**
			 * BE CAREFULLY, we use SimpleAdapter so Pos == id, but it is not
			 * the same with CusorAdapter
			 */
			TmpCursor.moveToPosition(Pos);
			// Check password
			if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).length() > 0 )
                showDialog(EidtNote_PwdPrompt_Dlg);
			else
				EditNoteHelper(TmpCursor);
		}	
    }
		
	private void EditNoteHelper(Cursor Note)
	{
			Intent OneNoteData = new Intent(NotePadPlus.this, EditNoteActivity.class);
			OneNoteData.putExtra(EditNoteActivity.KEY_SOURCE, Action_Edit_Note);
			OneNoteData.putExtra(OneNote.KEY_ROWID, Note.getInt(Note.getColumnIndexOrThrow(OneNote.KEY_ROWID)));
			OneNoteData.putExtra(OneNote.KEY_TITLE, Note
					.getString(Note
							.getColumnIndexOrThrow(OneNote.KEY_TITLE)));
			OneNoteData.putExtra(OneNote.KEY_PATH, Note
					.getString(Note
							.getColumnIndexOrThrow(OneNote.KEY_PATH)));
			OneNoteData.putExtra(OneNote.KEY_ENDTIME, Note
					.getString(Note
							.getColumnIndexOrThrow(OneNote.KEY_ENDTIME)));
			OneNoteData.putExtra(OneNote.KEY_USE_ENDTIME, Note
					.getString(Note
							.getColumnIndexOrThrow(OneNote.KEY_USE_ENDTIME)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYTIME, Note
					.getString(Note
							.getColumnIndexOrThrow(OneNote.KEY_NOTIFYTIME)));
			OneNoteData
					.putExtra(
							OneNote.KEY_USE_NOTIFYTIME,
							Note
									.getString(Note
											.getColumnIndexOrThrow(OneNote.KEY_USE_NOTIFYTIME)));
			OneNoteData.putExtra(OneNote.KEY_DELNOTE_EXP, Note
					.getString(Note
							.getColumnIndexOrThrow(OneNote.KEY_DELNOTE_EXP)));
			OneNoteData.putExtra(OneNote.KEY_TAGIMG_ID, Note
					.getInt(Note
							.getColumnIndexOrThrow(OneNote.KEY_TAGIMG_ID)));
			OneNoteData.putExtra(OneNote.KEY_BGCLR, Note.getInt(Note
					.getColumnIndexOrThrow(OneNote.KEY_BGCLR)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYDURA, Note
					.getInt(Note
							.getColumnIndexOrThrow(OneNote.KEY_NOTIFYDURA)));
			OneNoteData.putExtra(OneNote.KEY_NOTIFYMETHOD, Note
					.getInt(Note
							.getColumnIndexOrThrow(OneNote.KEY_NOTIFYMETHOD)));
			OneNoteData.putExtra(OneNote.KEY_RINGMUSIC, Note
					.getString(Note
							.getColumnIndexOrThrow(OneNote.KEY_RINGMUSIC)));

			startActivityForResult(OneNoteData, ACTIVITY_EDIT);
	}

	private void actionClickDelNote(int Pos) {
		// Get path
		Cursor TmpCursor = NotesCursor;
		TmpCursor.moveToPosition(Pos);
		// Check password
		if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).length() > 0 )
            showDialog(EidtNote_PwdPrompt_Dlg);
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

	// Build a dialog to prompt user we want to delete the assigned note
	private Dialog BuildDelPromptDialog(Context AppContext, int Title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(AppContext);
		builder.setIcon(R.drawable.alert_dialog_icon);
		builder.setTitle(Title);
		builder.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Delete the selected note
						actionClickDelNote(NoteIndex);
						// Reset selected noted index
						//NoteIndex = ErrNoteIndex;
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
               .setMessage(Msg)
               .setView(PromptView)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
            	      EditText Pwd = (EditText)findViewById(R.id.pwd_edit);
            	      Cursor TmpCursor = NotesCursor;
    			      TmpCursor.moveToPosition(NoteIndex);
    			      // Check password
    			      if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).equals(Pwd.getText().toString()) )
    				      DelNoteHelper(TmpCursor);
    			      else
    				      showDialog(PwdErr_Dlg);	
              }
              })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
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
               .setMessage(Msg)
               .setView(PromptView)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
            	      EditText Pwd = (EditText)findViewById(R.id.pwd_edit);
            	      Cursor TmpCursor = NotesCursor;
    			      TmpCursor.moveToPosition(NoteIndex);
    			      // Check password
    			      if( TmpCursor.getString(TmpCursor.getColumnIndexOrThrow(OneNote.KEY_PWD)).equals(Pwd.getText().toString()) )
    				      EditNoteHelper(TmpCursor);
    			      else
    				      showDialog(PwdErr_Dlg);	
              }
              })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
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
		case PwdErr_Dlg:
			 return HelperFunctions.BuildAltertDialog(NotePadPlus.this, R.string.pwderr_title, R.string.pwderr_prompt);

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
				NotesDb.UpdateNoteTagClr(TmpCursor.getInt(TmpCursor
						.getColumnIndexOrThrow(OneNote.KEY_ROWID)), TagImgIdx,
						BgClrIdx);
			}
		}else if( requestCode == ACTIVITY_SET_PWD || requestCode == ACTIVITY_CHG_PWD || requestCode == ACTIVITY_CLR_PWD ) {
			if( resultCode == RESULT_OK )
				NotesCursor = NotesDb.GetAllNotes();
		}
		if( requestCode == ACTIVITY_SET_TAGCLR || requestCode == ACTIVITY_EDIT || requestCode == ACTIVITY_CREATE ) 
		    RefreshListView();
		
	}

	private void CreateMenuView() {
		MainMenuView = View.inflate(this, R.layout.mainmenu, null);

		GridView MenuGrid = (GridView) MainMenuView.findViewById(R.id.MainMenu);

		ArrayList<HashMap<String, Object>> MenuItems = new ArrayList<HashMap<String, Object>>();
		String[] ItemTitle = { "添加日志", "系统设置", "栅格视图", "关于" };
		for (int i = 0; i <= SubMenu_About; ++i) {
			HashMap<String, Object> OneItem = new HashMap<String, Object>();
			// Item
			if (i == SubMenu_NoteView)
				if (AppSettings.IsListView())
					OneItem.put("ItemTitle", ItemTitle[i]);
				else
					OneItem.put("ItemTitle", "列表视图");
			else
				OneItem.put("ItemTitle", ItemTitle[i]);
			MenuItems.add(OneItem);
		}

		SimpleAdapter ItemsAdapter = new SimpleAdapter(this, MenuItems,
				R.layout.mainmenuitem, new String[] { "ItemTitle" },
				new int[] { R.id.MainMenuItem });

		MenuGrid.setAdapter(ItemsAdapter);

		MenuGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case SubMenu_AddNote:
					actionClickAddNote();
					MenuDlg.dismiss();
					break;
				case SubMenu_SysSet:
					MenuDlg.dismiss();
					break;
				case SubMenu_NoteView:
					TextView Item = (TextView) arg1
							.findViewById(R.id.MainMenuItem);
					if (AppSettings.IsListView()) {
						AppSettings.ViewStyle = AppSetting.ViewStyle_Grid;
						Item.setText("列表视图");
					} else {
						AppSettings.ViewStyle = AppSetting.ViewStyle_List;
						Item.setText("栅格视图");
					}
					RefreshListView();
					MenuDlg.dismiss();
					break;

				case SubMenu_About:
					showDialog(About_Dlg);
					MenuDlg.dismiss();
					break;
				}
			}
		});
	}
}