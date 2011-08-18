package com.android.hinotes;


import java.io.InputStream;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

public class NoteUserDefViewActivity extends Activity{
	// Database
	private NoteDbAdapter NotesDb;
	private Cursor NotesCursor;
	
	// Main layout container
	MyRelativeLayout MainLayout;

	int ScreenHeight;
	int ScreenWidth;
	
	ArrayList<NotePos> Postions;
	                 
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
         super.onCreate(savedInstanceState);

         // Get screen's widht & height
 		 DisplayMetrics ScreenMetrics = new DisplayMetrics();
 		 getWindowManager().getDefaultDisplay().getMetrics(ScreenMetrics);
 		 ScreenHeight = ScreenMetrics.heightPixels;
 		 ScreenWidth = ScreenMetrics.widthPixels;
 		 
 		 // Database
 		 NotesDb = new NoteDbAdapter(this);
		 NotesDb.open();
		 NotesCursor = NotesDb.GetAllNotes();
		
		 // Config main layout
		 ConfigUserDefLayout();
		 
		 // Set activity view
         setContentView(MainLayout);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Must create one menu
		menu.add(Menu.NONE, ProjectConst.ITEM0, 1, "添加").setIcon(android.R.drawable.ic_menu_add);
		menu.add(Menu.NONE, ProjectConst.ITEM1, 2, "设置").setIcon(android.R.drawable.ic_menu_manage);
		menu.add(Menu.NONE, ProjectConst.ITEM2, 3, "视图").setIcon(android.R.drawable.ic_menu_sort_by_size);
		menu.add(Menu.NONE, ProjectConst.ITEM3, 4, "返回").setIcon(android.R.drawable.ic_menu_revert);

        return true;
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {  
           switch(item.getItemId()) 
           {  
              case ProjectConst.ITEM0:
          		   Intent i = new Intent(this, AddNoteActivity.class);
        		   startActivityForResult(i, ProjectConst.ACTIVITY_CREATE);
	               break;
              case ProjectConst.ITEM1:
            	   Intent PrefSetting = new Intent(this, SysSettingActivity.class);
           	       startActivityForResult(PrefSetting, ProjectConst.ACTIVITY_SETTING);
	               break;
              case ProjectConst.ITEM2:
            	   showDialog(ProjectConst.ViewStyle_Dlg);
	               break;
              case ProjectConst.ITEM3:
            	   setResult(RESULT_CANCELED);
	               finish();
	               break;
           }
           return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		   case ProjectConst.ViewStyle_Dlg:
			    return BuildSelViewDlg(this, R.string.viewstyle_title, R.array.noteviewstyle);
		}
		return null;
	}
	
	@Override 
	public void onBackPressed(){
		ArrayList<NotePos> Positions = MainLayout.GetChildPos();
	    int Count = Positions.size();
	    for( int i = 0; i < Count; ++i )
	    	 NotesDb.SetNotePos(Positions.get(i).NoteRowId, Positions.get(i).LeftX, Positions.get(i).LeftY);
	    finish();
	}
	
    @Override
    protected void onDestroy(){
    	if( NotesDb != null )
    		NotesDb.close(); 
    	super.onDestroy();
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
                	        if( which < 2 )
                	        {
                	            NotePadPlus.SysSettings.ViewStyle = AppSetting.VieweStyleVal[which];
                	            setResult(RESULT_OK);
                    		    finish();
                	        }
                }  
            };  
        builder.setAdapter(adapter, listener);  
        return builder.create();  
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == ProjectConst.ACTIVITY_CREATE && resultCode == RESULT_OK ) {
			// add new one
			String Title = intent.getStringExtra(OneNote.KEY_TITLE);
			int RowId = intent.getIntExtra(OneNote.KEY_ROWID, ProjectConst.Zero);
			int ClrId = intent.getIntExtra(OneNote.KEY_DRAWABLE_ID, ProjectConst.Zero);
			LayoutInflater factory = LayoutInflater.from(this);
			TextView Text = (TextView)factory.inflate(R.layout.userdefitem, null);
            Text.setBackgroundDrawable(new BitmapDrawable(GetAlpha1x1Bg(this, ClrId)));
            Text.setOnTouchListener(new MyOnTouchListener(Text, RowId, Postions.size(), ScreenWidth, ScreenHeight));
            Text.setText(Title);
            Postions.add(new NotePos(RowId, ProjectConst.NegativeOne, ProjectConst.NegativeOne));
            MainLayout.addView(Text);
		} else if( requestCode == ProjectConst.ACTIVITY_EDIT && resultCode == RESULT_OK ) {
			// update one
			String Title = intent.getStringExtra(OneNote.KEY_TITLE);
			int Idx = intent.getIntExtra(OneNote.KEY_INDEX, ProjectConst.NegativeOne);
			int ClrId = intent.getIntExtra(OneNote.KEY_DRAWABLE_ID, ProjectConst.Zero);
			TextView Child = (TextView)MainLayout.getChildAt(Idx);
			Child.setText(Title);
			Child.setBackgroundDrawable(new BitmapDrawable(GetAlpha1x1Bg(this, ClrId)));
			Child.invalidate();
			
		} else if( requestCode == ProjectConst.ACTIVITY_SETTING && resultCode == RESULT_OK ) {
			// show new settings
			
		}
	}
	
	public void ConfigUserDefLayout() {
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
             Text.setBackgroundDrawable(new BitmapDrawable(GetAlpha1x1Bg(this, ClrId)));
             Text.setOnTouchListener(new MyOnTouchListener(Text, RowId, i, ScreenWidth, ScreenHeight));
             Text.setText(Title);
             
             Postions.add(new NotePos(RowId, LeftX, LeftY));
             MainLayout.addView(Text);
    	 }   
        
         MainLayout.SetChildPos(Postions);
	}

	public static Bitmap GetAlpha1x1Bg(Context AppCtx, int Idx){
	    InputStream is = AppCtx.getResources().openRawResource(R.drawable.bg_note_1x1);
	    Bitmap Src = BitmapFactory.decodeStream(is);
	    Bitmap AlphaBg = Src.extractAlpha();
	    Paint p = new Paint();
        p.setColor(NotePadPlus.ItemBgClr[Idx]);
        Bitmap Bg = Bitmap.createBitmap(75, 75, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(Bg);
        canvas.drawBitmap(AlphaBg, 0, 0, p);
        return Bg;
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
            	         		 ActivityIntent.putExtra(ProjectConst.KEY_SOURCE, ProjectConst.USERDEF_EDIT_ACTION);
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
