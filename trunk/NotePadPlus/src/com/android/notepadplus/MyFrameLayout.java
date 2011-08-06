package com.android.notepadplus;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
 

public class MyFrameLayout extends FrameLayout {
    private int[] temp = new int[] { 0, 0 };
    
    private int ScreenWidth;
    private int ScreenHeight;
    private int ViewWidth;
    private int ViewHeight;
    private int NoteRowId;
    
    public MyFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        ScreenWidth = ProjectConst.Zero;
        ScreenHeight = ProjectConst.Zero;
        ViewWidth = ProjectConst.Zero;
        ViewHeight = ProjectConst.Zero;
        NoteRowId = ProjectConst.NegativeOne;
    }
    
    public void SetScreenWH(int w, int h)
    {
    	ScreenWidth = w;
    	ScreenHeight = h;   
    }
    
	public void SetNoteRowId(int RowId)
	{
		NoteRowId = RowId;
	}
    
	public int GetNoteRowId()
	{
		return NoteRowId;
	}
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        switch (action) {

        case MotionEvent.ACTION_DOWN:

            //Log.d(TAG, "MyFrameLayout dispatchTouchEvent action:ACTION_DOWN");

            break;

        case MotionEvent.ACTION_MOVE:

            //Log.d(TAG, "MyFrameLayout dispatchTouchEvent action:ACTION_MOVE");

            break;

        case MotionEvent.ACTION_UP:

            //Log.d(TAG, "MyFrameLayout dispatchTouchEvent action:ACTION_UP");

            break;

        case MotionEvent.ACTION_CANCEL:

            //Log.d(TAG, "MyFrameLayout dispatchTouchEvent action:ACTION_CANCEL");

            break;

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int action = ev.getAction();

        switch (action) {

        case MotionEvent.ACTION_DOWN:

            //Log.d(TAG, "MyFrameLayout onInterceptTouchEvent action:ACTION_DOWN");

            break;

        case MotionEvent.ACTION_MOVE:

            //Log.d(TAG, "MyFrameLayout onInterceptTouchEvent action:ACTION_MOVE");

            break;

        case MotionEvent.ACTION_UP:

            //Log.d(TAG, "MyFrameLayout onInterceptTouchEvent action:ACTION_UP");

            break;

        case MotionEvent.ACTION_CANCEL:

            //Log.d(TAG, "MyFrameLayout onInterceptTouchEvent action:ACTION_CANCEL");

            break;

        }

        return true;

    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (action) {

        case MotionEvent.ACTION_DOWN:
        	 temp[0] = (int) event.getX();
             temp[1] = y - getTop();
             bringToFront();
             ViewWidth = getMeasuredWidth();
             ViewHeight = getMeasuredHeight();
             break;

        case MotionEvent.ACTION_MOVE:
        	 int leftX = x - temp[0];
     	     int leftY = y - temp[1];
     	     if( leftX < 0 ) leftX = 0;
     	     if( leftY < 0 ) leftY = 0;
     	     int rightX = leftX + ViewWidth;
     	     int rightY = leftY + ViewHeight;
     	 
     	     if( rightX > ScreenWidth ) {
     	    	 rightX = ScreenWidth;
     	    	 leftX = rightX - ViewWidth;
     	     }
     	     if( rightY > ScreenHeight ) {
     	    	 rightY = ScreenHeight;
     	    	 leftY = rightY - ViewHeight;
     	     }
     	 
             layout(leftX, leftY, rightX, rightY);
             postInvalidate();
             break;

        case MotionEvent.ACTION_UP:
 
            //Log.d(TAG, "MyFrameLayout---onTouchEvent action:ACTION_UP");

            break;

        case MotionEvent.ACTION_CANCEL:

            //Log.d(TAG, "MyFrameLayout---onTouchEvent action:ACTION_CANCEL");

            break;

        }

        return true;
    }

   

}