package com.android.hinotes;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.graphics.PorterDuff;

public class FilterClrItemAdapter extends SimpleAdapter
{
    private int[] ItemBgColor;
    private int[] ClrNum;
    private int ItemWidth;
    private Context AppContext;
    
	public FilterClrItemAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, int[] BgClr, int[] Nums, int Width) 
	{
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
		ItemBgColor = BgClr;
		ClrNum = Nums;
		ItemWidth = Width;	
		AppContext = context;
       
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View Item = super.getView(position, convertView, parent);
		
		Button OneTag = (Button)Item.findViewById(R.id.OneTagClr);
		//OneTag.setBackgroundColor(ItemBgColor[position]);
		OneTag.setWidth(ItemWidth);
		OneTag.setHeight(ItemWidth);
 
		String StrFormat = AppContext.getResources().getString(R.string.clrnum_prompt_format);	    
		OneTag.setText(String.format(StrFormat, ClrNum[position])); 
		OneTag.getBackground().setColorFilter(ItemBgColor[position], PorterDuff.Mode.MULTIPLY);
		return Item;
	}
	
};