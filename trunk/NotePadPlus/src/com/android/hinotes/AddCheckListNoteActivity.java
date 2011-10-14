package com.android.hinotes;

import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AddCheckListNoteActivity extends Activity {
	
	// Views
	private LinearLayout AddPanel;
	private EditText NoteTitleCtrl;
	private Button  SelectTagClrBtn;
	private TextView NotifyTimeLabel;
	private ListView CheckList;
	private View PromptView;
	private CheckListItemAdapter ItemAdapter;
	// Database
	private NoteDbAdapter NotesDb;
	// New notes
	private OneNote AddOneNote;
	// Width&Height
	private int ScreenHeight;
	private int ScreenWidth;
	// Push front or back
	private boolean FrontOrBack; 
	// Current item's index
	private int CurIdx;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addchecklistnote);
		// Open database
		NotesDb = new NoteDbAdapter(this);
		NotesDb.open();
		// One note
		AddOneNote = new OneNote(OneNote.ListNote);
		// Views
		AddPanel = (LinearLayout)findViewById(R.id.addnote_panel);
		NoteTitleCtrl = (EditText)findViewById(R.id.title_content);
		SelectTagClrBtn = (Button)findViewById(R.id.selnoteclr);
		NotifyTimeLabel = (TextView)findViewById(R.id.notifytime_text);
		CheckList = (ListView)findViewById(R.id.checklist);
		LayoutInflater factory = LayoutInflater.from(this);
		PromptView  = factory.inflate(R.layout.checklistitem_input, null);
		// Get screen resolution
		DisplayMetrics ScreenMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);
		ScreenHeight = ScreenMetrics.heightPixels;
		ScreenWidth = ScreenMetrics.widthPixels;
		
		// Front or back
		FrontOrBack = true;
		
		// Current item's index
		CurIdx = ProjectConst.NegativeOne;
		
		// Randomly select color
    	SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
    	AddPanel.setBackgroundDrawable(new BitmapDrawable(HelperFunctions.CreateBgLinearGraient(ScreenWidth, ScreenHeight, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx], NotePadPlus.TagClr[AddOneNote.DrawableResIdx])));
    	
    	// Set tag color
    	SelectTagClrBtn.setOnClickListener(new OnClickListener(){
    		public void onClick(View v){
		    	Intent intent = new Intent();
				intent.setClass(AddCheckListNoteActivity.this, SetItemClrActivity.class);
				intent.putExtra(SetItemClrActivity.Key_ClrType, SetItemClrActivity.Val_ItemType_Tag);
				startActivityForResult(intent, ProjectConst.ACTIVITY_SET_TAGCLR);				
    		}
    	});
    	
    	// Add list's adapter
    	ItemAdapter = new CheckListItemAdapter(this);
    	CheckList.setAdapter(ItemAdapter);
    	ItemAdapter.PushItem(getString(R.string.checklist_add));
    	ItemAdapter.PushItem(getString(R.string.checklist_add));
 
		// List item short click listener
    	CheckList.setOnItemClickListener(new OnItemClickListener() {
			/** When user clicks a note, show edit activity */
			public void onItemClick(AdapterView<?> Adapater, View ListView, int position, long id) {
				   if( position == ProjectConst.Zero ) {
					   FrontOrBack = true;
					   Intent i = new Intent(AddCheckListNoteActivity.this, AddItemActivity.class);
					   startActivityForResult(i, ProjectConst.ACTIVITY_ADD_ITEM);
				   } else if( position == ItemAdapter.getCount()-1 ) {
					   FrontOrBack = false;
					   Intent i = new Intent(AddCheckListNoteActivity.this, AddItemActivity.class);
					   startActivityForResult(i, ProjectConst.ACTIVITY_ADD_ITEM);
				   } else {
					   CurIdx = position;
					   ((EditText)PromptView.findViewById(android.R.id.content)).setText(ItemAdapter.GetItemAt(position));
					   showDialog(ProjectConst.ItemEdit_Dlg);
				   }						   
			}
		});
    	
    	// Set text change listener
    	NoteTitleCtrl.addTextChangedListener(new TextWatcher(){  	  
	        @Override  
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}  
	  
	        @Override  
	        public void onTextChanged(CharSequence s, int start, int before, int count) {  
	        	   AddOneNote.NoteTitle = s.toString();
	        }

			@Override
			public void afterTextChanged(Editable arg0) {}     
	    });
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ProjectConst.ItemEdit_Dlg:
			 return BuildEditItemDlg(this, R.string.checklist_item_title);
		case ProjectConst.Check_NoteTitle_Dlg:
		     return HelperFunctions.BuildAltertDialog(this, R.string.prompt_title, R.string.notetitle_empty_tip);
	    case ProjectConst.ShareBy_Dlg:
		     return HelperFunctions.BuildTextPlainShareByDlg(this, R.string.shareby_title, AddOneNote.NoteTitle, 
                                                    HelperFunctions.ComposeSharedItems(ItemAdapter.GetItems(), ProjectConst.One, ItemAdapter.getCount()-2));

		}
		return null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add(Menu.NONE, ProjectConst.ITEM0, 1, "放弃").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(Menu.NONE, ProjectConst.ITEM1, 2, "提醒").setIcon(R.drawable.ic_menu_reminder);
		menu.add(Menu.NONE, ProjectConst.ITEM2, 3, "锁定").setIcon(R.drawable.ic_menu_lock);
		menu.add(Menu.NONE, ProjectConst.ITEM3, 4, "分享").setIcon(android.R.drawable.ic_menu_share);
        return true;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {  
           switch(item.getItemId()) 
           {  
              case ProjectConst.ITEM0:
            	   setResult(RESULT_CANCELED);
	               finish();
	               break;
              case ProjectConst.ITEM1:
            	   StartNotifyActivity();
            	   break;
              case ProjectConst.ITEM2:
            	   Intent PwdDlgIntent = new Intent(this, PwdDlgActivity.class);
 			       startActivityForResult(PwdDlgIntent, ProjectConst.ACTIVITY_SET_PWD);
            	   break;
              case ProjectConst.ITEM3:
           	       showDialog(ProjectConst.ShareBy_Dlg);
            	   break;
            
           }
           return false;
	}
	
	@Override 
	public void onBackPressed(){
		// Do trim & check
		EditText Title = (EditText)findViewById(R.id.title_content);
   	    AddOneNote.NoteTitle = Title.getText().toString().trim();
   	    if( AddOneNote.NoteTitle.length() == 0 )
   	    {
   		    // Title is empty, we prompt it to user and then return
   		    showDialog(ProjectConst.Check_NoteTitle_Dlg);
   		    return;
   	    }
   	    // Save it to file
   	    // Get a random file name
   	    AddOneNote.NoteFilePath = UUID.randomUUID().toString()+ ProjectConst.NoteFileExt;
   	    HelperFunctions.WriteCheckListFile(this, ItemAdapter.GetItems(), ProjectConst.One, ItemAdapter.getCount()-2, AddOneNote.NoteFilePath);
   	    // Add database record
   	    NotesDb.CreateOneNote(AddOneNote);   
   	    // If user choose to use notify time, add the alarm
   	    if( AddOneNote.Use_NotifyTime.equals(ProjectConst.Yes) )
   	        Alarms.AddOneAlarm(this);
   	    // Refresh widget note list
   	    HelperFunctions.RefreshWidgetNoteList(this, NotesDb.GetAllNotes());
   	    // Return to main activity
        // Set return code(unable to return row id)
	    Intent ReturnBackData = new Intent();
	    ReturnBackData.putExtra(OneNote.KEY_TITLE, AddOneNote.NoteTitle);
	    ReturnBackData.putExtra(OneNote.KEY_PWD, AddOneNote.Password);
	    ReturnBackData.putExtra(OneNote.KEY_ROWID, NotesDb.GetOneNoteRowId(AddOneNote.NoteFilePath));
	    ReturnBackData.putExtra(OneNote.KEY_DRAWABLE_ID, AddOneNote.DrawableResIdx);
	    ReturnBackData.putExtra(OneNote.KEY_NOTETYPE, AddOneNote.NoteType);
	    setResult(RESULT_OK, ReturnBackData);
   	    finish(); 
	}
	
	
	@Override
	protected void onDestroy() {
		// Close database
		if (NotesDb != null)
			NotesDb.close();
		// Destroy
		super.onDestroy();

	}
    private void StartNotifyActivity()
	{
    	// start notify time activity
    	Intent intent = new Intent();
		intent.setClass(AddCheckListNoteActivity.this, NotifyDateActivity.class);
		// Set time
		Bundle Parameters = new Bundle();
		//Parameters.putString(OneNote.KEY_USE_NOTIFYTIME, AddOneNote.Use_NotifyTime);
		Parameters.putString(OneNote.KEY_NOTIFYTIME, HelperFunctions.Calendar2String(AddOneNote.NotifyTime));
		Parameters.putInt(OneNote.KEY_NOTIFYDURA, AddOneNote.NotifyDura);
		Parameters.putString(OneNote.KEY_RINGMUSIC, AddOneNote.RingMusic);
		Parameters.putInt(OneNote.KEY_NOTIFYMETHOD, AddOneNote.NotifyMethod);
        // Pass it to next activity 
		intent.putExtras(Parameters);
		// Go to next activity(set note's notify time activity)
		startActivityForResult(intent, ProjectConst.ACTIVITY_SET_NOTIFYTIME);		
	}
	
	private Dialog BuildEditItemDlg(Context AppContext, int Title){
		return new AlertDialog.Builder(AppContext)
               .setIcon(R.drawable.ic_dialog_menu_generic)
               .setTitle(Title)
               .setView(PromptView)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
                	   EditText Content = (EditText)PromptView.findViewById(android.R.id.content);
                	   if( Content.getText().toString().length() > 0 )
                		   ItemAdapter.SetItemAt(CurIdx, Content.getText().toString());
              }
              })
              .setNeutralButton(R.string.checklist_delitem_title, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						ItemAdapter.RemoveItemAt(CurIdx);
					}
			  })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                  }
              })
              .create();
	}
	
	// Handler return code
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == ProjectConst.ACTIVITY_SET_NOTIFYTIME ) {
	    	if( resultCode == RESULT_OK ) {
	    		Bundle Result = data.getExtras();
	    		if( Result.isEmpty() )
	    		{
	    			AddOneNote.Use_NotifyTime = ProjectConst.No;
	    			NotifyTimeLabel.setText(ProjectConst.EmptyStr);
	    		} else {
	    			AddOneNote.Use_NotifyTime = ProjectConst.Yes;
	    			AddOneNote.NotifyMethod = Result.getInt(OneNote.KEY_NOTIFYMETHOD);
	    			AddOneNote.NotifyDura = Result.getInt(OneNote.KEY_NOTIFYDURA);
	    			AddOneNote.RingMusic = Result.getString(OneNote.KEY_RINGMUSIC);
	    			AddOneNote.NotifyTime = HelperFunctions.String2Calenar(Result.getString(OneNote.KEY_NOTIFYTIME));
	    		    NotifyTimeLabel.setText(HelperFunctions.FormatCalendar2ReadableStr(AddOneNote.NotifyTime));
	    		}
	    	}
	    } else if ( requestCode == ProjectConst.ACTIVITY_SET_TAGCLR && resultCode == RESULT_OK) {
	    	    Bundle SelIdxData = data.getExtras();
	    	    AddOneNote.DrawableResIdx = SelIdxData.getInt(OneNote.KEY_DRAWABLE_ID);
	    	    SelectTagClrBtn.getBackground().setColorFilter(NotePadPlus.TagClr[AddOneNote.DrawableResIdx], PorterDuff.Mode.MULTIPLY);
	    	    AddPanel.setBackgroundDrawable(new BitmapDrawable(HelperFunctions.CreateBgLinearGraient(ScreenWidth, ScreenHeight, NotePadPlus.ItemBgClr[AddOneNote.DrawableResIdx], NotePadPlus.TagClr[AddOneNote.DrawableResIdx])));
	    } else if( requestCode == ProjectConst.ACTIVITY_SET_PWD && resultCode == RESULT_OK )
	    	    AddOneNote.Password = data.getStringExtra(OneNote.KEY_PWD); 
	    else if( requestCode == ProjectConst.ACTIVITY_ADD_ITEM && resultCode == RESULT_OK ) {
	    	    String Act = data.getStringExtra(AddItemActivity.Action);
	    	    String Item = data.getStringExtra(OneNote.KEY_BODY);
	    	    // Push 
	    	    if( FrontOrBack )
	    	    	ItemAdapter.PushFontItem(Item);
     		    else
     			    ItemAdapter.PushBackItem(Item);
	    	    // Show next if needed
	    	    if( Act.equals(AddItemActivity.NextAction) )
	    	    {
	    	    	Intent i = new Intent(AddCheckListNoteActivity.this, AddItemActivity.class);
					startActivityForResult(i, ProjectConst.ACTIVITY_ADD_ITEM);
	    	    }
	    	    
	    }
	}
}
