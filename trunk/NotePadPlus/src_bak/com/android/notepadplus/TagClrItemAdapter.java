package com.android.notepadplus;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.graphics.PorterDuff;

public class TagClrItemAdapter extends SimpleAdapter
{
    private int[] ItemBgColor;
    private int ItemWidth;
	public TagClrItemAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, int[] BgClr, int Width) 
	{
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
		ItemBgColor = BgClr.clone();
		ItemWidth = Width;		
       
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View Item = super.getView(position, convertView, parent);
		
		Button OneTag = (Button)Item.findViewById(R.id.OneTagClr);
		//OneTag.setBackgroundColor(ItemBgColor[position]);
		OneTag.setWidth(ItemWidth);
		OneTag.setHeight(ItemWidth);
		OneTag.setTextColor(ItemBgColor[position]);
		OneTag.getBackground().setColorFilter(ItemBgColor[position], PorterDuff.Mode.MULTIPLY);
		return Item;
	}
	
};