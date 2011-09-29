package com.android.hinotes;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

public class CommonListItemAdapter extends BaseAdapter {  
	      
		  private int[] ImgIds;
		  private int Items;
		  private Context AppContext;
		  CommonListItemAdapter(int[] Img, int ItemRes, Context AppCtxt){
			     ImgIds = Img;
			     Items = ItemRes;
			     AppContext = AppCtxt;
		  }
	      @Override  
	      public int getCount() {  
	             return ImgIds.length;  
	      }  
	  
	      @Override  
	      public Object getItem(int position) {  
	            return null;  
	      }  
	  
	      @Override  
	      public long getItemId(int position) {  
	            return 0;  
	      }  
	  
	      @Override  
	      public View getView(int position, View contentView, ViewGroup parent) {  

	    	    LinearLayout Item = new LinearLayout(AppContext);
	            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);  
	    	    Item.setLayoutParams(layoutParams);
	            TextView textView = new TextView(AppContext);  
	            String text = AppContext.getResources().getStringArray(Items)[position];  
	            textView.setText(text);  

	            // Set font size  
	            textView.setTextSize(24);  
	            // Set layout
	            textView.setGravity(android.view.Gravity.CENTER_VERTICAL);  
	            textView.setMinHeight(60);  
	            textView.setPadding(5, 0, 0, 0);
	            // Set font color
	            textView.setTextColor(Color.BLACK);  
	            
	            // Set app icon
	            ImageView Tag = new ImageView(AppContext);
	            Tag.setImageResource(ImgIds[position]);
	            Tag.setScaleType(ImageView.ScaleType.FIT_CENTER);
	            Tag.setMinimumHeight(60);
	            Tag.setMinimumWidth(45);
                 
	            // Add icon & name
	            Item.addView(Tag);
	            Item.addView(textView);

	            return Item;  
	      }	          
	}  