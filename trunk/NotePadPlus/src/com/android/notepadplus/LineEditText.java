package com.android.notepadplus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class LineEditText extends EditText {   
	   private Rect mRect;
	   private Paint mPaint;   
       float HeightFactor;
       public LineEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
            HeightFactor = 1.3f;
       }
        
       @Override
       protected void onDraw(Canvas canvas) {
            Rect r = mRect;
            Paint paint = mPaint;
            
            int SingleHeight = getLineHeight();
            int WholeHeight = getHeight();
            int Toppadding = getTotalPaddingTop();
            int Count1 = (int) ((WholeHeight-Toppadding)/SingleHeight*HeightFactor);
            int Count2 = getLineCount();
            int Counter = Math.max(Count1, Count2);
            getLineBounds(0, r);
            for (int i = 0; i < Counter; i++) {
                int Baseline = (i+1)*SingleHeight;
               	Baseline += Toppadding;
                canvas.drawLine(r.left, Baseline + 1, r.right, Baseline + 1, paint);
            }

            super.onDraw(canvas);
       }
}
