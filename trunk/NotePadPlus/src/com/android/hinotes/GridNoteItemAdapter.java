package com.android.hinotes;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

public class GridNoteItemAdapter extends SimpleAdapter {
	 private int[] ItemBodyColor;
	 private int[] NoteType;
	 private boolean[] IsLock;
	 private boolean[] IsNotify;
	 private int EdgeWidth;
	 public GridNoteItemAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource, String[] from,
				int[] to, int[] BodyClr, boolean[] Lock, boolean[] Notify, int[] Type, int Edge) 
		{
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
			ItemBodyColor = BodyClr;
			NoteType = Type;
			IsLock = Lock;
			IsNotify = Notify;
			EdgeWidth = Edge;		
	       
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View Item = super.getView(position, convertView, parent);

			Button Body = (Button)Item.findViewById(R.id.GridNoteBody);
            ImageView Lock = (ImageView)Item.findViewById(R.id.lockimg);
            ImageView Notify = (ImageView)Item.findViewById(R.id.notifyimg);
            ImageView Type = (ImageView)Item.findViewById(R.id.notetypeimg);
            if( IsLock[position] )
                Lock.setImageResource(R.drawable.ic_item_lock);
            if( IsNotify[position] )
            	Notify.setImageResource(R.drawable.ic_griditem_notify);
            if( NoteType[position] == OneNote.ListNote )
            	Type.setImageResource(R.drawable.ic_griditem_mark );

            	
            Body.setWidth(EdgeWidth);
            Body.setHeight(EdgeWidth);
            //Body.setBackgroundColor(ItemBodyColor[position]);
            Body.getBackground().setColorFilter(ItemBodyColor[position], PorterDuff.Mode.SRC);
            
			return Item;
		}
}
