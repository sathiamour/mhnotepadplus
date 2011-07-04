package com.android.notepadplus;

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
    private int[] TagColor;
    private boolean[] IsLock;
    private boolean[] IsNotify;
    private boolean[] IsRank;
    private float FontHeight;
    private static int FontSize = 25;
    private static int TagWidth = 7;
    Context AppContext;
	public NoteItemAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, int[] BgClr, int[] TagClr, boolean[] Lock, boolean[] Notify, boolean [] Rank) 
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
		IsRank = Rank;
		
		// Get height of font   
		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);   
		textPaint.setTextSize(FontSize); 
		FontMetrics fontMetrics = textPaint.getFontMetrics();   
		FontHeight = fontMetrics.bottom-fontMetrics.top;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View Item = super.getView(position, convertView, parent);
		
		TextView Tag1 = (TextView)Item.findViewById(R.id.NoteTag1);
		TextView Tag2 = (TextView)Item.findViewById(R.id.NoteTag2);
		TextView Title = (TextView)Item.findViewById(R.id.NoteTitle);
		//TextView Time = (TextView)Item.findViewById(R.id.NoteCreatedTime);
		ImageView Ring = (ImageView)Item.findViewById(R.id.NoteRingImg);
		ImageView Lock = (ImageView)Item.findViewById(R.id.NoteLockImg);
		CheckBox Rank = (CheckBox)Item.findViewById(R.id.Rank);
		RelativeLayout Sub = (RelativeLayout)Item.findViewById(R.id.ListSubItem);

		Rank.setChecked(IsRank[position]);
		Rank.setOnClickListener(new OnClickListener(){
				@Override          
				public void onClick(View Star){ 
                    // Tell main activity we have star on note
				    Intent MainActivity = new Intent(NotePadPlus.BROADCAST_RANKNOTE);
				    MainActivity.putExtra(OneNote.KEY_INDEX, position);
				    MainActivity.putExtra(OneNote.KEY_RANK, ((CheckBox)Star).isChecked());
				    AppContext.sendBroadcast(MainActivity);	
				}
		});
		
		Tag1.setBackgroundColor(TagColor[position]);
		Tag1.setTextColor(TagColor[position]);
		Tag2.setBackgroundColor(TagColor[position]);
		Tag2.setTextColor(TagColor[position]);
		
		//Title.setBackgroundColor(ItemBgColor[position]);
		//Time.setBackgroundColor(ItemBgColor[position]);
		//Ring.setBackgroundColor(ItemBgColor[position]);
		//Lock.setBackgroundColor(ItemBgColor[position]);
		//Rank.setBackgroundColor(ItemBgColor[position]);
		
		Title.setTextSize(AppSetting.FontSizeArray[Integer.parseInt(NotePadPlus.AppSettings.FontSize)]);

		Tag1.setWidth(TagWidth);
		Tag2.setWidth(TagWidth);
		float ItemHeightFactor = AppSetting.ItemHeightFactor[Integer.parseInt(NotePadPlus.AppSettings.ItemHeight)];
		Tag1.setHeight((int) (FontHeight*ItemHeightFactor));
		Title.setHeight((int) (FontHeight*ItemHeightFactor));
		Title.setWidth(NotePadPlus.ScreenWidth-TagWidth-100);
		Sub.setBackgroundColor(ItemBgColor[position]);


		if( !IsLock[position] )
			Lock.setVisibility(View.INVISIBLE);

		if( !IsNotify[position] )
			Ring.setVisibility(View.INVISIBLE);

		//Ring.setMaxWidth(10);
		//Lock.setMaxWidth(24);
		
		return Item;
	}
};