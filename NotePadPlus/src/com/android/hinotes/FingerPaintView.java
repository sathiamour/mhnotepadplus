package com.android.hinotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FingerPaintView extends View {
        
		private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;
        
        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Path    mPath;
        private Paint   mBitmapPaint;
        private Paint   FingerPaint;
        private int BgClr;
        private boolean IsPainted;
        
		public FingerPaintView(Context context) {
	    	super(context);
    	}
	        
		public FingerPaintView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public FingerPaintView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}
		
        public void InitFingerPaintView(Paint Finger, int Width, int Height, int Clr)
        {
        	mBitmap = Bitmap.createBitmap(Width, Height, Bitmap.Config.ARGB_8888);
        	BgClr = Clr;
            mCanvas = new Canvas(mBitmap);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mBitmapPaint.setColor(Clr);
            mCanvas.drawPaint(mBitmapPaint);
            FingerPaint = Finger;
        	IsPainted = false;
        	
        	 
        }
        
        public void InitFingerPaintView(Bitmap Src, Paint Finger, int Clr)
        {
        	mBitmap = Src;
        	BgClr = Clr;
            mCanvas = new Canvas(mBitmap);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mBitmapPaint.setColor(Clr);
            FingerPaint = Finger;
        	IsPainted = false;  	 
        }
        
        public void SetBgColor(int Clr)
        {
            BgClr = Clr;
            invalidate();
        }
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(BgClr);
           
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        	//canvas.drawPaint(mBitmapPaint);
            canvas.drawPath(mPath, FingerPaint);
        }
        
        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;
        
        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our off screen
            mCanvas.drawPath(mPath, FingerPaint);
            // kill this so we don't double draw
            mPath.reset();
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            IsPainted = true;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
        
        public Bitmap getFingerPaint()
        {
        	return mBitmap;
        }
        
        public boolean getPainted()
        {
        	return IsPainted;
        }
}