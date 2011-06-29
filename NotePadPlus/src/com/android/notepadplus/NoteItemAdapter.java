package com.android.notepadplus;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class NoteItemAdapter extends SimpleAdapter
{
    private int[] ItemBgColor;
    private int[] TagColor;
    private boolean[] IsLock;
    private boolean[] IsNotify;
    private float FontHeight;
    private static int FontSize = 25;
    private static int TagWidth = 7;
    Context AppContext;
	public NoteItemAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, int[] BgClr, int[] TagClr, boolean[] Lock, boolean[] Notify) 
	{
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
		// Application context
		AppContext = context;
		// Array
		ItemBgColor = BgClr;
		TagColor = TagClr;
		IsLock = Lock;
		IsNotify = Notify;
		
		// Get height of font   
		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);   
		textPaint.setTextSize(FontSize); 
		FontMetrics fontMetrics = textPaint.getFontMetrics();   
		FontHeight = fontMetrics.bottom-fontMetrics.top;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View Item = super.getView(position, convertView, parent);
		
		TextView Tag1 = (TextView)Item.findViewById(R.id.NoteTag1);
		TextView Tag2 = (TextView)Item.findViewById(R.id.NoteTag2);
		TextView Title = (TextView)Item.findViewById(R.id.NoteTitle);
		TextView Time = (TextView)Item.findViewById(R.id.NoteCreatedTime);
		ImageView Ring = (ImageView)Item.findViewById(R.id.NoteRingImg);
		ImageView Lock = (ImageView)Item.findViewById(R.id.NoteLockImg);
		
		Tag1.setBackgroundColor(TagColor[position]);
		Tag1.setTextColor(TagColor[position]);
		Tag2.setBackgroundColor(TagColor[position]);
		Tag2.setTextColor(TagColor[position]);
		
		Title.setBackgroundColor(ItemBgColor[position]);
		Time.setBackgroundColor(ItemBgColor[position]);
		Ring.setBackgroundColor(ItemBgColor[position]);
		Lock.setBackgroundColor(ItemBgColor[position]);
		
		Title.setTextSize(AppSetting.FontSizeArray[Integer.parseInt(NotePadPlus.AppSettings.FontSize)]);

		Tag1.setWidth(TagWidth);
		Tag2.setWidth(TagWidth);
		Tag1.setHeight((int) (FontHeight*14/10));
		Title.setHeight((int) (FontHeight*14/10));
		Title.setWidth(NotePadPlus.ScreenWidth-TagWidth-64);
		
		if( !IsLock[position] )
		{
			Drawable LockImg = Lock.getDrawable();
			LockImg.mutate().setAlpha(0);
			Lock.setImageDrawable(LockImg);
		}
		
		if( !IsNotify[position] )
		{
			Drawable RingImg = Ring.getDrawable();
			RingImg.mutate().setAlpha(0);
			Ring.setImageDrawable(RingImg);
			
		}
		//Ring.setMaxWidth(10);
		//Lock.setMaxWidth(24);
		
		return Item;
	}
	
    

};