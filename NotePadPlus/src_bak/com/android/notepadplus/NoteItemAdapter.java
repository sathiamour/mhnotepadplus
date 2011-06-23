package com.android.notepadplus;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class NoteItemAdapter extends SimpleAdapter
{
    private int[] ItemBgColor;
    private int[] TagColor;
    private float FontHeight;
    private static int FontSize = 25;
    private static int TagWidth = 7;
	public NoteItemAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, int[] BgClr, int[] TagClr) 
	{
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
		ItemBgColor = BgClr.clone();
		TagColor = TagClr.clone();
		
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
		
		Tag1.setBackgroundColor(TagColor[position]);
		Tag1.setTextColor(TagColor[position]);
		Tag2.setBackgroundColor(TagColor[position]);
		Tag2.setTextColor(TagColor[position]);
		
		Title.setBackgroundColor(ItemBgColor[position]);
		Time.setBackgroundColor(ItemBgColor[position]);
		
		Tag1.setWidth(TagWidth);
		Tag2.setWidth(TagWidth);
		Tag1.setHeight((int) (FontHeight*14/10));
		Title.setHeight((int) (FontHeight*14/10));
		
		return Item;
	}
	
    

};