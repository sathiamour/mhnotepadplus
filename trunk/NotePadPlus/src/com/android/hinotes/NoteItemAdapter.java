package com.android.hinotes;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class NoteItemAdapter extends SimpleAdapter
{
    private int[] ItemBgColor;
    private int[] NoteType;
    private boolean[] IsLock;
    private boolean[] IsNotify;
    private boolean[] IsRank;
    
    private float FontHeight1;
    private float FontHeight2;
    private static int FontSize1 = 25;
    private static int FontSize2 = 15;
    Context AppContext;
	public NoteItemAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, int[] BgClr, boolean[] Lock, boolean[] Notify, boolean [] Rank, int[] Type) 
	{
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
		// Application context
		AppContext = context;
		// Array
		ItemBgColor = BgClr;
		NoteType = Type;
		IsLock = Lock;
		IsNotify = Notify;
		IsRank = Rank;
		
		// Get height of font   
		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);   
		textPaint.setTextSize(FontSize1); 
		FontMetrics fontMetrics = textPaint.getFontMetrics();   
		FontHeight1 = fontMetrics.bottom-fontMetrics.top;
		textPaint.setTextSize(FontSize2);
		FontMetrics fontMetrics2 = textPaint.getFontMetrics();   
		FontHeight2 = fontMetrics2.bottom-fontMetrics2.top; 
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View Item = super.getView(position, convertView, parent);
		
		ImageView Tag = (ImageView)Item.findViewById(R.id.NoteTag);
		TextView Title = (TextView)Item.findViewById(R.id.NoteTitle);
		TextView Time = (TextView)Item.findViewById(R.id.NoteCreatedTime);
		ImageView Ring = (ImageView)Item.findViewById(R.id.NoteRingImg);
		ImageView Lock = (ImageView)Item.findViewById(R.id.notelock);
		CheckBox Rank = (CheckBox)Item.findViewById(R.id.Rank);
		RelativeLayout Sub = (RelativeLayout)Item.findViewById(R.id.ListItem);
		Rank.setChecked(IsRank[position]);
		Rank.setOnClickListener(new OnClickListener(){
				@Override          
				public void onClick(View Star){ 
                    // Tell main activity we have star on note
				    Intent MainActivity = new Intent(ProjectConst.BROADCAST_RANKNOTE_ACTION);
				    MainActivity.putExtra(OneNote.KEY_INDEX, position);
				    MainActivity.putExtra(OneNote.KEY_RANK, ((CheckBox)Star).isChecked());
				    AppContext.sendBroadcast(MainActivity);	
				}
		});
		
		Tag.setBackgroundColor(ItemBgColor[position]);
		if( NoteType[position] == OneNote.TextNote )
			Tag.setImageResource(R.drawable.ic_item_text);
		else
			Tag.setImageResource(R.drawable.ic_item_list);

		Title.setTextSize(AppSetting.FontSizeArray[Integer.parseInt(NotePadPlus.SysSettings.FontSize)]);

		
		float ItemHeightFactor = AppSetting.ItemHeightFactor[Integer.parseInt(NotePadPlus.SysSettings.ItemHeight)];
		Title.setHeight((int) (FontHeight1*ItemHeightFactor));
		Time.setHeight((int)(FontHeight2));
		Title.setWidth(NotePadPlus.ScreenWidthDip-110);
		Sub.setBackgroundColor(ItemBgColor[position]);
        
		if( !IsLock[position] )
			Lock.setVisibility(View.INVISIBLE);
		else
			Lock.setVisibility(View.VISIBLE);

		if( !IsNotify[position] )
			Ring.setVisibility(View.INVISIBLE);
		else
			Ring.setVisibility(View.VISIBLE);
		
		return Item;
	}
};