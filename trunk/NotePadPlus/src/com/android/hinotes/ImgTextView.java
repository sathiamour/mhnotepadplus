package com.android.hinotes;
  

import android.content.Context;  
import android.graphics.Bitmap;  
import android.graphics.BitmapFactory;  
import android.graphics.Canvas;  
import android.graphics.Rect;  
import android.util.AttributeSet;  
import android.widget.TextView;  
  
public class ImgTextView extends TextView  
{  
    private boolean MarkFlag;
    private boolean LockFlag;
    private Context AppContext;
    private Bitmap Bg;
	private Bitmap Mark;
	private Bitmap Clock;
	
	
    public ImgTextView(Context context, AttributeSet attrs)
    {  
		super(context, attrs);
		AppContext = context;

    }  

    public void Initialization(int ClrId, boolean IsMark, boolean IsLock) 
    {
		Bg = HelperFunctions.GetAlpha1x1Bg(AppContext, R.drawable.bg_userdef_note, 120, 120, ClrId);
		Mark = BitmapFactory.decodeResource(getResources(), R.drawable.ic_griditem_mark);
		Clock = BitmapFactory.decodeResource(getResources(), R.drawable.ic_item_lock); 
		
		MarkFlag = IsMark;
		LockFlag = IsLock;
    }
    
	@Override
	protected void onDraw(Canvas canvas)
	{
		 //  从原图上截取图像的区域，在本例中为整个图像
		 Rect src = new Rect();
		 //  将截取的图像复制到bitmap上的目标区域，在本例中与复制区域相同
		 // Draw backgroud
		 Rect target = new Rect();
		 src.left = 0;
		 src.top = 0;
		 src.right = Bg.getWidth();
		 src.bottom = Bg.getHeight();

		 target.left = 0;
		 target.top = 0;
		 target.right=getMeasuredWidth();
		 target.bottom=getMeasuredHeight();
			
		 canvas.drawBitmap(Bg, src, target, getPaint());
			
		 // Mark if needed	
		 if( MarkFlag )
		 {
		     src.left = 0;
		     src.top = 0;
		     src.right = Mark.getWidth();
		     src.bottom = Mark.getHeight();
		 
			 target.left = getMeasuredWidth()-Mark.getWidth()*3/2;
			 target.top = 4;
			 target.right=getMeasuredWidth()-Mark.getWidth()/2;
			 target.bottom=Mark.getHeight()+4;

			 canvas.drawBitmap(Mark, src, target, getPaint());
		 }	
			
		 // Lock if needed
		 if( LockFlag )
		 {
			 src.left = 0;
			 src.top = 0;
			 src.right = Clock.getWidth();
			 src.bottom = Clock.getHeight();
 
			 target.left = 10;
			 target.top = 4;
			 target.right=Clock.getWidth()+10;
			 target.bottom=Clock.getHeight()+4;
			 
             canvas.drawBitmap(Clock, src, target, getPaint());
		 }

		 super.onDraw(canvas);
	}
} 



