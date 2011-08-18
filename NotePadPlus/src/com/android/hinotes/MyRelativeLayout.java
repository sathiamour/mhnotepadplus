package com.android.hinotes;


import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class MyRelativeLayout extends RelativeLayout {

	ArrayList<NotePos> Positions;
    
    public MyRelativeLayout(Context context) {
    	super(context);
    	Positions = null;
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Positions = null;
    }
    
    public void SetChildPos(ArrayList<NotePos> Pos)
    {
    	Positions = Pos;
    }
    
    public void SetChildPos(int Idx, int X, int Y)
    {
    	Positions.get(Idx).LeftX = X;
    	Positions.get(Idx).LeftY = Y;
    }
    
    public ArrayList<NotePos> GetChildPos()
    {
    	return Positions;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {    	
        if( NotePadPlus.SysSettings.IsUserDefView() ) 
        {
        	Random Gen = new Random();
            for( int i = 0; i < getChildCount(); i++ )
            {
                 View child = getChildAt(i);
                 child.setVisibility(View.VISIBLE);
            
                 int LeftX = Positions.get(i).LeftX;
                 int LeftY = Positions.get(i).LeftY;
                 child.measure(right-left, bottom-top);

                 if( LeftX == ProjectConst.NegativeOne )
                 {
                     Positions.get(i).LeftX = LeftX = Gen.nextInt(right-child.getMeasuredWidth());
                     Positions.get(i).LeftY = LeftY = Gen.nextInt(bottom-child.getMeasuredHeight());
                 }

                 child.layout(LeftX, LeftY, LeftX+child.getMeasuredWidth(), LeftY+child.getMeasuredHeight());
            }
        } else {
        	
            for (int i = 0; i < getChildCount(); i++) 
            {
                View child = getChildAt(i);
                child.setVisibility(View.VISIBLE);
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        switch (action) {

        case MotionEvent.ACTION_DOWN:

            //Log.d(TAG, "dispatchTouchEvent action:ACTION_DOWN");

            break;

        case MotionEvent.ACTION_MOVE:

            //Log.d(TAG, "dispatchTouchEvent action:ACTION_MOVE");

            break;

        case MotionEvent.ACTION_UP:

            //Log.d(TAG, "dispatchTouchEvent action:ACTION_UP");

            break;

        case MotionEvent.ACTION_CANCEL:

            //Log.d(TAG, "dispatchTouchEvent action:ACTION_CANCEL");

            break;

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int action = ev.getAction();

        switch (action) {

        case MotionEvent.ACTION_DOWN:

            //Log.d(TAG, "onInterceptTouchEvent action:ACTION_DOWN");

            break;

        case MotionEvent.ACTION_MOVE:

            //Log.d(TAG, "onInterceptTouchEvent action:ACTION_MOVE");

            break;

        case MotionEvent.ACTION_UP:

            //Log.d(TAG, "onInterceptTouchEvent action:ACTION_UP");

            break;

        case MotionEvent.ACTION_CANCEL:

            //Log.d(TAG, "onInterceptTouchEvent action:ACTION_CANCEL");

            break;

        }

        return false;

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int action = ev.getAction();

        switch (action) {

        case MotionEvent.ACTION_DOWN:

            //Log.d(TAG, "---onTouchEvent action:ACTION_DOWN");

            break;

        case MotionEvent.ACTION_MOVE:

            //Log.d(TAG, "---onTouchEvent action:ACTION_MOVE");

            break;

        case MotionEvent.ACTION_UP:

            //Log.d(TAG, "---onTouchEvent action:ACTION_UP");

            break;

        case MotionEvent.ACTION_CANCEL:

            //Log.d(TAG, "---onTouchEvent action:ACTION_CANCEL");

            break;

        }

        return true;
    }

}