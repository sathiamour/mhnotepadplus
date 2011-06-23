package com.android.notepadplus;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;

public class GridNoteItemAdapter extends SimpleAdapter {
	 private int[] ItemBodyColor;
	 private int EdgeWidth;
	 public GridNoteItemAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource, String[] from,
				int[] to, int[] BodyClr, int Edge) 
		{
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
			ItemBodyColor = BodyClr.clone();
			EdgeWidth = Edge;		
	       
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View Item = super.getView(position, convertView, parent);
			
			Button Body = (Button)Item.findViewById(R.id.GridNoteBody);

            Body.setWidth(EdgeWidth);
            Body.setHeight(EdgeWidth);
            Body.getBackground().setColorFilter(ItemBodyColor[position], PorterDuff.Mode.MULTIPLY);
            
			return Item;
		}
}
